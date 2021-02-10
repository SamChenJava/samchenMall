package com.samchenjava.mall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.samchenjava.common.to.SkuHasStockTo;
import com.samchenjava.common.to.mq.OrderTo;
import com.samchenjava.common.to.mq.SeckillOrderTo;
import com.samchenjava.common.utils.R;
import com.samchenjava.common.vo.MemberRespVo;
import com.samchenjava.mall.order.constant.OrderConstant;
import com.samchenjava.mall.order.entity.OrderItemEntity;
import com.samchenjava.mall.order.enume.OrderStatusEnum;
import com.samchenjava.mall.order.feign.CartFeignService;
import com.samchenjava.mall.order.feign.MemberFeignService;
import com.samchenjava.mall.order.feign.ProductFeignService;
import com.samchenjava.mall.order.feign.WmsFeignService;
import com.samchenjava.mall.order.interceptor.LoginUserInterceptor;
import com.samchenjava.mall.order.service.OrderItemService;
import com.samchenjava.mall.order.to.OrderCreateTo;
import com.samchenjava.mall.order.vo.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.samchenjava.common.utils.PageUtils;
import com.samchenjava.common.utils.Query;

import com.samchenjava.mall.order.dao.OrderDao;
import com.samchenjava.mall.order.entity.OrderEntity;
import com.samchenjava.mall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import sun.rmi.runtime.Log;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private ThreadLocal<OrderSubmitVo> orderSubmitVoThreadLocal = new ThreadLocal<>();

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    WmsFeignService wmsFeignService;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    OrderItemService orderItemService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override//n.265 data for order confirm page
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        //get delivery addresses for current user by feign
        CompletableFuture<Void> addressesCompletableFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> addresses = memberFeignService.getAddresses(memberRespVo.getId());
            orderConfirmVo.setAddress(addresses);
        }, threadPoolExecutor);

        //get checked items in shopping cart by feign
        CompletableFuture<Void> cartItemsCompletableFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
            orderConfirmVo.setItems(currentUserCartItems);
        }, threadPoolExecutor).thenRunAsync(() -> {//n.271
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> items = orderConfirmVo.getItems();
            List<Long> skuIdCollect = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            R skusHasStock = wmsFeignService.getSkusHasStock(skuIdCollect);
            List<SkuHasStockTo> data = skusHasStock.getData(new TypeReference<List<SkuHasStockTo>>() {
            });
            if (data != null) {
                Map<Long, Boolean> stockMap = data.stream().collect(Collectors.toMap(SkuHasStockTo::getSkuId, SkuHasStockTo::getHasStock));
                orderConfirmVo.setStocks(stockMap);
            }
        }, threadPoolExecutor);

        //get user credit
        Integer integration = memberRespVo.getIntegration();
        orderConfirmVo.setIntegration(integration);

        //n.275 avoid duplication token
        String token = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + LoginUserInterceptor.loginUser.get().getId(), token, 10, TimeUnit.MINUTES);
        orderConfirmVo.setOrderToken(token);
        CompletableFuture.allOf(addressesCompletableFuture, cartItemsCompletableFuture).get();
        return orderConfirmVo;
    }

    //@GlobalTransactional
    @Transactional
    @Override//n.275
    public SubmitOrderRespVo submitOrder(OrderSubmitVo orderSubmitVo) {
        SubmitOrderRespVo submitOrderRespVo = new SubmitOrderRespVo();
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();
        submitOrderRespVo.setCode(0);
        orderSubmitVoThreadLocal.set(orderSubmitVo);
        //n.276 verifying token with redis comparason and deleting of token should be atomic]
        String script = "if redis.call('get',KEYS[1])==ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String orderToken = orderSubmitVo.getOrderToken();
        //verify token atomic
        Long result = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId()), orderToken);
        if (result == 0L) {
            //verification failed
            submitOrderRespVo.setCode(1);
            return submitOrderRespVo;
        } else {
            //verification succeed
            //create order and order items
            OrderCreateTo order = createOrder();
            //double check payment
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = orderSubmitVo.getPayPrice();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                saveOrder(order);
                //lock stock
                //n.280 orderSn, orderItems(skuId, skuName, num)
                WareSkuLockVo lockVo = new WareSkuLockVo();
                lockVo.setOrderSn(order.getOrder().getOrderSn());
                List<OrderItemVo> orderItemVos = order.getOrderItems().stream().map(item -> {
                    OrderItemVo itemVo = new OrderItemVo();
                    itemVo.setSkuId(item.getSkuId());
                    itemVo.setCount(item.getSkuQuantity());
                    itemVo.setTitle(item.getSkuName());
                    return itemVo;
                }).collect(Collectors.toList());
                lockVo.setLocks(orderItemVos);
                //remote lock stock
                R r = wmsFeignService.orderLockStock(lockVo);
                if (r.getCode() == 0) {
                    //lock succeed
                    submitOrderRespVo.setOrderEntity(order.getOrder());
                    //order create succeed,send message to MQ
                    //String exchange, String routingKey, final Object object, Nullable CorrelationData correlationData
                    rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", order.getOrder());
                    return submitOrderRespVo;
                } else {
                    //lock failed
                    submitOrderRespVo.setCode(3);
                    return submitOrderRespVo;
                }
            } else {
                submitOrderRespVo.setCode(2);
                return submitOrderRespVo;
            }
        }
    }

    @Override//n.295
    public OrderEntity getOrderByOrderSn(String orderSn) {
        OrderEntity order_sn = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        return order_sn;
    }

    @Override//n.298
    public void closeOrder(OrderEntity entity) {
        //get current order latest status
        OrderEntity orderEntity = this.getById(entity.getId());
        if (orderEntity.getStatus().equals(OrderStatusEnum.CREATE_NEW.getCode())) {
            //close order
            OrderEntity update = new OrderEntity();
            update.setId(entity.getId());
            update.setStatus(OrderStatusEnum.CANCELED.getCode());
            this.updateById(update);
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity,orderTo);
            try {
                //making sure messages can be delivered, each message can be logged
                //scan database on schedule. send message again
                rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
            }catch (Exception e){
                //try to redelivery undelivered message
            }
        }
    }

    @Override//n.304
    public PayVo getOrderPay(String orderSn) {
        PayVo payVo = new PayVo();
        OrderEntity order = this.getOrderByOrderSn(orderSn);

        BigDecimal bigDecimal = order.getPayAmount().setScale(2, BigDecimal.ROUND_UP);
        payVo.setTotal_amount(bigDecimal.toString());
        payVo.setOut_trade_no(order.getOrderSn());

        List<OrderItemEntity> order_sn = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));
        OrderItemEntity entity = order_sn.get(0);

        payVo.setSubject(entity.getSkuName());
        payVo.setBody(entity.getSkuAttrsVals());

        return payVo;
    }

    @Override//n.306
    public PageUtils queryPageWithItem(Map<String, Object> params) {
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();
        IPage<OrderEntity> page = this.page(new Query<OrderEntity>().getPage(params)
                , new QueryWrapper<OrderEntity>().eq("member_id", memberRespVo.getId()).orderByDesc("id"));
        List<OrderEntity> orderEntities = page.getRecords().stream().map(order -> {
            List<OrderItemEntity> itemEntities = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", order.getOrderSn()));
            order.setItemEntities(itemEntities);
            return order;
        }).collect(Collectors.toList());
        page.setRecords(orderEntities);
        return new PageUtils(page);
    }

    @Override//v323
    public void createSeckillOrder(SeckillOrderTo seckillOrderTo) {
        //保存订单信息
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(seckillOrderTo.getOrderSn());
        orderEntity.setMemberId(seckillOrderTo.getMemberId());
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        BigDecimal multiply = seckillOrderTo.getSeckillPrice().multiply(new BigDecimal("" + seckillOrderTo.getNum()));
        orderEntity.setPayAmount(multiply);
        this.save(orderEntity);
        //保存订单项信息 秒杀就一件商品，所以创建一个订单项就可以
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderSn(seckillOrderTo.getOrderSn());
        orderItemEntity.setRealAmount(multiply);
        //TODO 获取当前sku的详细信息进行设置 productFeignService.getSpuInfoBySkuId()
        orderItemEntity.setSkuQuantity(Integer.parseInt(seckillOrderTo.getNum().toString()));
        orderItemService.save(orderItemEntity);
    }

    //n.280
    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);

        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItemService.saveBatch(orderItems);
    }

    //n.277
    private OrderCreateTo createOrder() {
        OrderCreateTo to = new OrderCreateTo();
        //generate orderSn
        String orderSn = IdWorker.getTimeId();
        //build a order Entity
        OrderEntity orderEntity = buildOrder(orderSn);
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        //get order items info
        List<OrderItemEntity> itemEntities = buildOrderItems(orderSn);
        //double check payment
        computePrice(orderEntity, itemEntities);
        to.setOrder(orderEntity);
        to.setOrderItems(itemEntities);
        return to;
    }

    //n.278
    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> itemEntities) {
        BigDecimal total = new BigDecimal("0.0");
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal integration = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");
        BigDecimal gift = new BigDecimal("0.0");
        BigDecimal growth = new BigDecimal("0.0");
        for (OrderItemEntity itemEntity : itemEntities) {
            coupon = coupon.add(itemEntity.getCouponAmount());
            integration = integration.add(itemEntity.getIntegrationAmount());
            promotion = promotion.add(itemEntity.getPromotionAmount());
            total = total.add(itemEntity.getRealAmount());
            gift.add(new BigDecimal(itemEntity.getGiftIntegration().toString()));
            growth.add(new BigDecimal(itemEntity.getGiftGrowth().toString()));
        }
        orderEntity.setTotalAmount(total);
        //payment of order
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setCouponAmount(coupon);
        //set credit level info
        orderEntity.setIntegration(gift.intValue());
        orderEntity.setGrowth(growth.intValue());
        orderEntity.setDeleteStatus(0); //未删除
    }

    //n.277
    private OrderEntity buildOrder(String orderSn) {
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderSn);
        orderEntity.setMemberId(memberRespVo.getId());
        //get delivery address info
        OrderSubmitVo orderSubmitVo = orderSubmitVoThreadLocal.get();
        R fare = wmsFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo fareResp = fare.getData(new TypeReference<FareVo>() {
        });
        //set delivery fee
        orderEntity.setFreightAmount(fareResp.getFare());
        //set receiver info
        orderEntity.setReceiverCity(fareResp.getAddressVo().getCity());
        orderEntity.setReceiverDetailAddress(fareResp.getAddressVo().getDetailAddress());
        orderEntity.setReceiverName(fareResp.getAddressVo().getName());
        orderEntity.setReceiverPhone(fareResp.getAddressVo().getPhone());
        orderEntity.setReceiverPostCode(fareResp.getAddressVo().getPostCode());
        orderEntity.setReceiverProvince(fareResp.getAddressVo().getProvince());
        orderEntity.setReceiverRegion(fareResp.getAddressVo().getRegion());

        return orderEntity;
    }

    //n.277
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        if (currentUserCartItems != null && currentUserCartItems.size() > 0) {
            List<OrderItemEntity> itemEntities = currentUserCartItems.stream().map(cartItem -> {
                OrderItemEntity orderItemEntity = buildOrderItem(cartItem);
                orderItemEntity.setOrderSn(orderSn);
                return orderItemEntity;
            }).collect(Collectors.toList());
            return itemEntities;
        }
        return null;
    }

    //n.278
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        //order info: order num
        //spu info
        Long skuId = cartItem.getSkuId();
        R spuInfoBySkuId = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo spuInfoVo = spuInfoBySkuId.getData(new TypeReference<SpuInfoVo>() {
        });
        orderItemEntity.setSpuId(spuInfoVo.getId());
        orderItemEntity.setSpuBrand(spuInfoVo.getBrandId().toString());
        orderItemEntity.setSpuName(spuInfoVo.getSpuName());
        orderItemEntity.setCategoryId(spuInfoVo.getCatalogId());
        //sku info
        orderItemEntity.setSkuId(cartItem.getSkuId());
        orderItemEntity.setSkuName(cartItem.getTitle());
        orderItemEntity.setSkuPic(cartItem.getImage());
        orderItemEntity.setSkuPrice(cartItem.getPrice());
        String skuAttr = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";");
        orderItemEntity.setSkuAttrsVals(skuAttr);
        orderItemEntity.setSkuQuantity(cartItem.getCount());
        //credit info
        orderItemEntity.setGiftGrowth(cartItem.getPrice().intValue());
        orderItemEntity.setGiftIntegration(cartItem.getPrice().intValue());

        orderItemEntity.setPromotionAmount(new BigDecimal("0"));
        orderItemEntity.setCouponAmount(new BigDecimal("0"));
        orderItemEntity.setIntegrationAmount(new BigDecimal("0"));
        //actual payment for current order
        BigDecimal origin = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity().toString()));
        BigDecimal subtract = origin.subtract(orderItemEntity.getCouponAmount()).subtract(orderItemEntity.getPromotionAmount())
                .subtract(orderItemEntity.getIntegrationAmount());
        orderItemEntity.setRealAmount(subtract);
        return orderItemEntity;
    }

}