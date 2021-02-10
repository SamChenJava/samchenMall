package com.samchenjava.mall.warehouse.listener;

import com.alibaba.fastjson.TypeReference;
import com.rabbitmq.client.Channel;
import com.samchenjava.common.to.mq.OrderTo;
import com.samchenjava.common.to.mq.StockDetailTo;
import com.samchenjava.common.to.mq.StockLockedTo;
import com.samchenjava.common.utils.R;
import com.samchenjava.mall.warehouse.entity.WareOrderTaskDetailEntity;
import com.samchenjava.mall.warehouse.entity.WareOrderTaskEntity;
import com.samchenjava.mall.warehouse.service.WareSkuService;
import com.samchenjava.mall.warehouse.vo.OrderVo;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RabbitListener(queues = "stock.release.stock.queue")
public class StockReleaseListener {

    @Autowired
    WareSkuService wareSkuService;

    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo to, Message message, Channel channel) throws IOException {

        System.out.println("receive unlock stock message");
        try{
            wareSkuService.unlockStock(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }

    @RabbitHandler//n.298
    public void handleOrderCloseRelease(OrderTo orderTo, Message message, Channel channel) throws IOException {
        System.out.println("order close message, prepare to unlock stock");
        try{
            wareSkuService.unlockStock(orderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
