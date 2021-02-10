package com.samchenjava.mall.order.config;

import com.rabbitmq.client.Channel;
import com.samchenjava.mall.order.entity.OrderEntity;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MallMQConfig {

    /*@RabbitListener(queues = "order.release.order.queue")
    public void Listener(OrderEntity orderEntity, Channel channel, Message message) throws IOException {
        System.out.println("receive expired message: " + orderEntity.getOrderSn());
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }*/


    //@Bean Binding, Queue, Exchange
    @Bean
    public Queue OrderDelayQueue(){
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "order-event-exchange");
        arguments.put("x-dead-letter-routing-key","order.release.order");
        arguments.put("x-message-ttl", 60000);
        Queue queue = new Queue("order.delay.queue", true, false, false, arguments);
        return queue;
    }

    @Bean
    public Queue OrderReleaseOrderQueue(){
        Queue queue = new Queue("order.release.order.queue",true,false,false);
        return queue;
    }

    @Bean
    public Exchange orderEventExchange(){
        //String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        return new TopicExchange("order-event-exchange", true, false);
    }

    @Bean
    public Binding OrderCreateOrderBinding(){
        //String destination, DestinationType destinationType, String exchange, String routingKey,
        //			Map<String, Object> arguments
        return new Binding("order.delay.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.create.order", null);
    }

    @Bean
    public Binding OrderReleaseOrderBinding(){
        return new Binding("order.release.order.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.release.order", null);
    }

    /**
     * order release direct bind with stock.release.stock.queue
     * router.key = order.release.other
     * @return
     */
    @Bean
    public Binding OrderReleaseOtherBinding(){
        return new Binding("stock.release.stock.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.release.other.#", null);
    }

    @Bean//n.323
    public Queue orderSeckillOrderQueue() {
        //String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
        return new Queue("order.seckill.order.queue", true, false, false);
    }

    @Bean//n.323
    public Binding orderSeckillOrderQueueBinding() {
        //String destination, DestinationType destinationType, String exchange, String routingKey, Map<String, Object> arguments
        return new Binding("order.seckill.order.queue", Binding.DestinationType.QUEUE,
                "order-event-exchange", "order.seckill.order", null);
    }


}
