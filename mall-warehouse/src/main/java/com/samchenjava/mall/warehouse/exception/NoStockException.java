package com.samchenjava.mall.warehouse.exception;

//n.281
public class NoStockException extends RuntimeException {
    private Long skuId;

    public NoStockException(Long skuId) {
        super("product skuId: " + skuId + " Insufficient stock exception");
    }
}
