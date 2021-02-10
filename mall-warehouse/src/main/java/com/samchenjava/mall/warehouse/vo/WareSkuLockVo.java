package com.samchenjava.mall.warehouse.vo;

import lombok.Data;

import java.util.List;

@Data//n.280
public class WareSkuLockVo {
    private String orderSn; //订单号
    private List<OrderItemVo> locks;//info of locked stock
}
