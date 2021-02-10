package com.samchenjava.mall.order.controller;

import com.samchenjava.mall.order.entity.OrderEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.UUID;

@Controller
public class HelloController {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @ResponseBody
    @RequestMapping("/test/createOrder")
    public String createOrderTest(){
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(UUID.randomUUID().toString());
        orderEntity.setCreateTime(new Date());
        rabbitTemplate.convertAndSend("order-event-exchange","order.create.order", orderEntity);
        return "ok";
    }

}
