package com.samchenjava.common.to.es;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description data structure of sku store inside es
 * @Author Sam Chen
 * @Date 2020/4/10 11:01
 * @Version 1.0
 **/
@Data
public class SkuEsModel {

    private Long skuId;

    private Long spuId;

    private String skuTitle;//"type":"text"

    private BigDecimal skuPrice;//"type": "keyword"

    private String skuImg;

    private Long saleCount;

    private Boolean hasStock; // whether in stock

    private Long hotScore; // popularity

    private Long catalogId;

    private Long brandId;

    private String brandName;

    private String brandImg;

    private String catalogName;

    private List<Attrs> attrs;


    @Data
    public static class Attrs {

        private Long attrId;

        private String attrName;

        private String attrValue;
    }

}
