package com.samchenjava.mall.coupon.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * spu bonus settings
 *
 * @author samchen
 * @email nan.chen.java@gmail.com
 * @date 2020-12-30 13:29:15
 */
@Data
@TableName("sms_spu_bounds")//spu bonus info
public class SpuBoundsEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private Long spuId;
    /**
     * growth bonus
     */
    private BigDecimal growBounds;
    /**
     * shopping bonus
     */
    private BigDecimal buyBounds;

    /**
     * bonus effective condition[1111(four digits, from right to left); 0 - no discount, offer growBounds( growth bonus) or not,1 - no discount, offer buyBounds(shopping bonus) or not]
     * 2-has discount, offer growBouns( growth bonus) or not, 3-has discount, offer buyBounds(shopping bonus) or not
     */
    private Integer work;

}
