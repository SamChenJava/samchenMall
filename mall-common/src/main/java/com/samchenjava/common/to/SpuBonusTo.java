package com.samchenjava.common.to;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SpuBonusTo {
    private Long spuId;

    private BigDecimal buyBounds;

    private BigDecimal growBounds;
}
