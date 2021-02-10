package com.samchenjava.mall.warehouse.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**video 238
 * 购物车里的每一项
 */
@Data
public class OrderItemVo {
    private Long skuId;
    private String title;
    private String image;
    private List<String> skuAttr;
    private BigDecimal price;
    private Integer count;
    private BigDecimal totalPrice;
    //private boolean hasStock;
    private BigDecimal weight;

    //param: skuId, stock
    @Setter
    @Getter
    private Map<Long,Boolean> stocks;
}
