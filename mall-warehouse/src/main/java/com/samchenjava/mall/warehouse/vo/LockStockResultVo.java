package com.samchenjava.mall.warehouse.vo;

import lombok.Data;

//video 280 1057
@Data
public class LockStockResultVo {
    private Long skuId;//
    private Integer num;
    private Boolean locked;
}
