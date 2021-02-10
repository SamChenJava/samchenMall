package com.samchenjava.mall.order.vo;

import lombok.Data;

import java.util.List;

@Data//n.280
public class WareSkuLockVo {
    private String orderSn; //订单号
    private List<OrderItemVo> locks;//info of locked stock
}
