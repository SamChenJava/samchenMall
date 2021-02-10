package com.samchenjava.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data//n.275
public class OrderSubmitVo {
    private long addrId;
    private Integer payType;//payment method
    private BigDecimal payPrice;//double check payPrice
    private String note;//order notes
    //infos relevant to user, get info of logged in user from session
    private String orderToken;//token for avoid duplication
    //we get selected item from shopping cart later, instead of submit by this VO
}
