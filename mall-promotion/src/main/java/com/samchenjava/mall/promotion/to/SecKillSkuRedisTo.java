package com.samchenjava.mall.promotion.to;

import com.samchenjava.mall.promotion.vo.SkuInfoVo;
import lombok.Data;

import java.math.BigDecimal;
//n.314 1730 copy properties from SeckillSkuVo, and add additional property, SkuInfoVo
//this to will be stored in redis
@Data
public class SecKillSkuRedisTo {
    private Long id;
    /**
     * 活动id
     */
    private Long promotionId;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;

    //sku random code
    private String randomCode;

    //promotion price
    private BigDecimal seckillPrice;

    //total sku quantity in current session
    private BigDecimal seckillCount;
    /**
     * 每人限购数量
     */
    private BigDecimal seckillLimit;

    //sort
    private Integer seckillSort;

    //n.315 current session start time
    private Long startTime;

    //n.315 current session end Time
    private Long endTime;

    //sku detail info
    private SkuInfoVo skuInfo;
}
