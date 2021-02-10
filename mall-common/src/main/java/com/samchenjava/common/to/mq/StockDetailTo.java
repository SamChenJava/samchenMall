package com.samchenjava.common.to.mq;

import lombok.Data;

@Data//n.294 1910
public class StockDetailTo {
    private Long id;
    /**
     * sku_id
     */
    private Long skuId;
    /**
     * sku_name
     */
    private String skuName;
    /**
     * purchasing quantity
     */
    private Integer skuNum;
    /**
     * warehouse task list id
     */
    private Long taskId;
    /**
     * warehouse id
     */
    private Long wareId;
    /**
     * lock status
     */
    private Integer lockStatus;
}
