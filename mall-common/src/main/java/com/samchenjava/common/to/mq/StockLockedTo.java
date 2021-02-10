package com.samchenjava.common.to.mq;

import lombok.Data;

import java.util.List;

@Data
public class StockLockedTo {
    private Long id;    //warehouse task id
    private StockDetailTo detail;    //task detail id n.294 1500 1700
}
