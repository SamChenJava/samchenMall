package com.samchenjava.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.samchenjava.common.to.mq.SeckillOrderTo;
import com.samchenjava.common.utils.PageUtils;
import com.samchenjava.mall.order.entity.OrderEntity;
import com.samchenjava.mall.order.vo.OrderConfirmVo;
import com.samchenjava.mall.order.vo.OrderSubmitVo;
import com.samchenjava.mall.order.vo.PayVo;
import com.samchenjava.mall.order.vo.SubmitOrderRespVo;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author samchen
 * @email nan.chen.java@gmail.com
 * @date 2020-12-30 14:10:54
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;//n.265 data for order confirm page

    SubmitOrderRespVo submitOrder(OrderSubmitVo orderSubmitVo);//n.275

    OrderEntity getOrderByOrderSn(String orderSn);//n.295

    void closeOrder(OrderEntity orderEntity);//n.298

    PayVo getOrderPay(String orderSn);//n.304

    PageUtils queryPageWithItem(Map<String, Object> params);//n.306

    void createSeckillOrder(SeckillOrderTo seckillOrderTo);//n.323
}

