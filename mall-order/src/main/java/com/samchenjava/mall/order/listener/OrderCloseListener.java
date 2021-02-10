package com.samchenjava.mall.order.listener;

import com.rabbitmq.client.Channel;
import com.samchenjava.mall.order.entity.OrderEntity;
import com.samchenjava.mall.order.service.OrderService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@RabbitListener(queues = "order.release.order.queue")//n.298
@Service
public class OrderCloseListener {

    @Autowired
    OrderService orderService;

    @RabbitHandler
    public void Listener(OrderEntity orderEntity, Channel channel, Message message) throws IOException {
        System.out.println("receive expired message: " + orderEntity.getOrderSn());
        orderService.closeOrder(orderEntity);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
