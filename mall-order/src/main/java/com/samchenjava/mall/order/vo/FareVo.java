package com.samchenjava.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data//n.277 0955
public class FareVo {
    private MemberAddressVo addressVo;
    private BigDecimal fare;
}
