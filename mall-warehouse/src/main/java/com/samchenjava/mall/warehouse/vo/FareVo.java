package com.samchenjava.mall.warehouse.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data//n.273
public class FareVo {

    private MemberAddressVo addressVo;
    private BigDecimal Fare;
}
