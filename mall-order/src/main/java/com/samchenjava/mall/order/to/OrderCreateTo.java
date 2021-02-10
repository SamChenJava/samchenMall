package com.samchenjava.mall.order.to;

import com.samchenjava.mall.order.entity.OrderEntity;
import com.samchenjava.mall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data//n.277 0220 this transfer object after OrderEntity created
public class OrderCreateTo {
    private OrderEntity order;
    private List<OrderItemEntity> orderItems;
    private BigDecimal payPrice;    //payment of this order
    private BigDecimal fare;    //delivery fee
}
