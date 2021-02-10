package com.samchenjava.mall.order;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MallOrderApplicationTests {

    @Autowired
    AmqpAdmin amqpAdmin;

    @Test
    public void contextLoads() {
        System.out.println(amqpAdmin);
        Queue queue = new Queue("haluhalu.queue", true, false, false);
        amqpAdmin.declareQueue(queue);
    }

}
