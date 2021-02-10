package com.samchenjava.mall.order.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class OrderConfirmVo {//n.265
    //delivery address, ums_member_receive_address
    @Getter
    @Setter
    List<MemberAddressVo> address;

    //all selected items in shopping cart
    @Getter
    @Setter
    List<OrderItemVo> items;

    //check recored

    //credit info
    @Getter
    @Setter
    Integer integration;

    //param: skuId, stock
    @Setter @Getter
    Map<Long,Boolean> stocks;

    //avoid duplication
    @Getter
    @Setter
    String orderToken;

    public Integer getCount(){
        Integer i = 0;
        if (items != null) {
            for (OrderItemVo item : items) {
                i += item.getCount();
            }
        }
        return i;
    }

    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal("0");
        if (items != null) {
            for (OrderItemVo item : items) {
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                sum = sum.add(multiply);
            }
        }
        return sum;
    }

    BigDecimal payPrice;//应付价格

    public BigDecimal getPayPrice() {
        BigDecimal sum = new BigDecimal("0");
        if (items != null) {
            for (OrderItemVo item : items) {
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                sum = sum.add(multiply);
            }
        }
        return sum;
    }
}
