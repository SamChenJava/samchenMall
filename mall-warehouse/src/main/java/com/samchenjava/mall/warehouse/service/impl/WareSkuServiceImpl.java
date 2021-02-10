package com.samchenjava.mall.warehouse.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.rabbitmq.client.Channel;
import com.samchenjava.common.to.SkuHasStockTo;
import com.samchenjava.common.to.mq.OrderTo;
import com.samchenjava.common.to.mq.StockDetailTo;
import com.samchenjava.common.to.mq.StockLockedTo;
import com.samchenjava.common.utils.R;
import com.samchenjava.mall.warehouse.entity.WareOrderTaskDetailEntity;
import com.samchenjava.mall.warehouse.entity.WareOrderTaskEntity;
import com.samchenjava.mall.warehouse.exception.NoStockException;
import com.samchenjava.mall.warehouse.feign.OrderFeignService;
import com.samchenjava.mall.warehouse.service.WareOrderTaskDetailService;
import com.samchenjava.mall.warehouse.service.WareOrderTaskService;
import com.samchenjava.mall.warehouse.vo.OrderItemVo;
import com.samchenjava.mall.warehouse.vo.OrderVo;
import com.samchenjava.mall.warehouse.vo.WareSkuLockVo;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.samchenjava.common.utils.PageUtils;
import com.samchenjava.common.utils.Query;

import com.samchenjava.mall.warehouse.dao.WareSkuDao;
import com.samchenjava.mall.warehouse.entity.WareSkuEntity;
import com.samchenjava.mall.warehouse.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;

@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    WareSkuDao wareSkuDao;

    @Autowired//n.294 warehouse order task
    WareOrderTaskService orderTaskService;

    @Autowired//n.294 warehouse order task detail
    WareOrderTaskDetailService orderTaskDetailService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    OrderFeignService orderFeignService;

    //n.96 sku info in certain warehouse
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();

        String skuId = (String) params.get("skuId");
        if (StringUtils.isNotEmpty(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }
        String wareId = (String) params.get("wareId");
        if (StringUtils.isNotEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override//n.132
    public List<SkuHasStockTo> getSkuHasStock(List<Long> skuIds) {
        List<SkuHasStockTo> collect = skuIds.stream().map(skuId -> {
            SkuHasStockTo to = new SkuHasStockTo();
            //get current sku total amount in warehouse
            //select sum(wws.stock-wws.stock_locked) from wms_ware_sku wws where wws.sku_id = 1
            Long count = this.baseMapper.getSkuStock(skuId);
            to.setSkuId(skuId);
            to.setHasStock(count == null ? false : count > 0);
            return to;
        }).collect(Collectors.toList());
        return collect;
    }

    /**
     * Scenarios that stock need to be unlocked:
     * 1) submit order succeed, but no payment or cancelled by customers
     * 2) submit order succeed, locking stock succeed, but following service call failed, result in order rollback
     *
     * @param vo
     * @return
     */
    @Transactional(rollbackFor = NoStockException.class)
    @Override//n.281 lock stock for a order
    public Boolean orderLockStock(WareSkuLockVo vo) {

        WareOrderTaskEntity orderTaskEntity = new WareOrderTaskEntity();
        orderTaskEntity.setOrderSn(vo.getOrderSn());
        orderTaskService.save(orderTaskEntity);

        //find a closest warehouse, according to delivery address (this is too much complicate, I will do this later)
        //find stock of each of item
        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWarehouseHasStock> collect = locks.stream().map(item -> {
            SkuWarehouseHasStock stock = new SkuWarehouseHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            //find out which warehouse has stock
            List<Long> wareIds = wareSkuDao.listWareIdHasSkuStock(skuId);
            stock.setWareId(wareIds);
            stock.setNum(item.getCount());
            return stock;
        }).collect(Collectors.toList());
        //lock stock
        for (SkuWarehouseHasStock hasStock : collect) {
            Boolean skuStocked = false;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareId();
            if (wareIds == null || wareIds.size() == 0) {
                throw new NoStockException(skuId);
            }
            for (Long wareId : wareIds) {
                //return 1 if succeed
                Long count = wareSkuDao.lockSkuStock(skuId, wareId, hasStock.getNum());
                if (count == 0) {
                    //failed to lock current warehouse, try next
                } else {
                    skuStocked = true;
                    WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity(null, skuId, "", hasStock.getNum(), orderTaskEntity.getId(), wareId, 1);
                    orderTaskDetailService.save(wareOrderTaskDetailEntity);
                    StockLockedTo stockLockedTo = new StockLockedTo();
                    stockLockedTo.setId(orderTaskEntity.getId());
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(wareOrderTaskDetailEntity, stockDetailTo);
                    stockLockedTo.setDetail(stockDetailTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", stockLockedTo);
                    break;
                }
            }//end wareIds loop
            if (!skuStocked) {
                //none of warehouse locked current sku
                throw new NoStockException(skuId);
            }
        }
        //all sku locked succeed
        return true;
    }

    @Override
    public void unlockStock(StockLockedTo to) {
        StockDetailTo detail = to.getDetail();
        Long detailId = detail.getId();
        //解锁 v 295
        //1 查询数据库关于这个订单的锁定库存信息
        //  有：证明库存锁定成功了
        //    解锁与否要看订单情况
        //      订单状态 已取消：解锁库存
        //              没取消：不能解锁库存
        //  没有：库存锁定失败了，库存回滚了。这种情况无需解锁
        WareOrderTaskDetailEntity byId = orderTaskDetailService.getById(detailId);
        if (byId != null) {
            //解锁
            Long id = to.getId();//task id
            WareOrderTaskEntity taskEntity = orderTaskService.getById(id);
            String orderSn = taskEntity.getOrderSn();//task's orderSn
            R r = orderFeignService.getOrderStatus(orderSn);
            //order data return succeed
            if (r.getCode() == 0) {
                OrderVo data = r.getData(new TypeReference<OrderVo>() {
                });
                if (data == null || data.getStatus() == 4) {
                    //stock can be unlocked if order doesn't exist, or cancelled
                    if(byId.getLockStatus() == 1){
                        //current warehouse task detail list, status = 1 , which is under locked condition, can be unlocked
                        unlockStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum(), detailId);
                    }
                }
            } else {
                //消息拒绝以后，重新放到队列里，让别人继续消费解锁。
                throw new RuntimeException("remote service failed");
            }
        } else {
            //无需解锁
        }
    }

    //n.298 In case order service freeze, result in order status can not update. warehouse stock lock expired earlier. This freeze order can never unlock stock
    @Transactional
    @Override//n.298
    public void unlockStock(OrderTo orderTo) {
        String orderSn = orderTo.getOrderSn();
        //double check latest stock status, in case same order unlock twice
        WareOrderTaskEntity orderTask = orderTaskService.getOrderTaskByOrderSn(orderSn);
        Long taskId = orderTask.getId();
        List<WareOrderTaskDetailEntity> detailList = orderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>()
                .eq("task_id", taskId).eq("lock_status", 1));
        for (WareOrderTaskDetailEntity entity : detailList) {
            unlockStock(entity.getSkuId(),entity.getWareId(),entity.getSkuNum(),entity.getTaskId());
        }
    }

    @Data
    class SkuWarehouseHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }

    /**
     * n.294 automatic unlock stock
     * 1) submit order succeed, but no payment or cancelled by customers
     * 2) lock stock failed
     *
     * @param to
     * @param message
     */
    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo to, Message message, Channel channel) throws IOException {
        System.out.println("receive unlock stock message");
        StockDetailTo stockDetailTo = to.getDetail();
        Long stockDetailToId = stockDetailTo.getId();
        /**
         * 1 checking stock lock info of current order, no need to unlock stock if return null,
         * because stock locked failed, warehouse lock rollback
         */
        WareOrderTaskDetailEntity byId = orderTaskDetailService.getById(stockDetailToId);
        if (byId != null) {
            //unlock
            Long stockLockedToId = to.getId();
            WareOrderTaskEntity taskEntity = orderTaskService.getById(stockDetailToId);
            String orderSn = taskEntity.getOrderSn();//get order status by orderSn
            R r = orderFeignService.getOrderStatus(orderSn);
            if (r.getCode() == 0) {
                OrderVo orderVo = r.getData(new TypeReference<OrderVo>() {
                });
                if (orderVo == null ||orderVo.getStatus() == 4) {
                    //order cancelled or does not exist, stock need to be unlock
                    unlockStock(stockDetailTo.getSkuId(), stockDetailTo.getWareId(), stockDetailTo.getSkuNum(), stockDetailToId);
                    channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
                }
            }else{
                channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
            }
        } else {
            //no need to unlock
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }
    }

    public void unlockStock(Long skuId, Long wareId, Integer quantity, Long taskDetailId) {
        //unlock warehouse stock for current sku
        wareSkuDao.unlockStock(skuId,wareId,quantity);
        //n.297 update warehouse task list status
        WareOrderTaskDetailEntity orderTaskDetailEntity = new WareOrderTaskDetailEntity();
        orderTaskDetailEntity.setId(taskDetailId);
        orderTaskDetailEntity.setLockStatus(2);//update to unlock
        orderTaskDetailService.updateById(orderTaskDetailEntity);
    }

}