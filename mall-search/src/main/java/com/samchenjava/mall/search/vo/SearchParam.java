package com.samchenjava.mall.search.vo;

import lombok.Data;

import java.util.List;

//catalog3Id=225&eyword=小米&sort=saleCount&hasStock=0/1&brandId=1&brandId=2
//&attrs=1_5inch:8inch&attrs=2_16G:32G
@Data
public class SearchParam {
    private String keyword;
    private Long catalog3Id;//level 3 category id
    /**
     * only one in three order type affected
     * sort = saleCount_asc/desc
     * sort = skuPrice_asc/desc
     * sort = hotScore_asc/desc
     */
    private String sort;
    private Integer hasStock;//0 - outOfStock, 1 - inStock
    private String skuPrice;//price range
    private List<Long> brandId;//search by brand, multiple select allowed
    //attrs=2_5inch:6inch(attrs=attrId_attrValue1:attrValue2)
    private List<String> attrs;
    private Integer pageNum = 1;//page number

    private String _queryString;//n.191 original all query parameters
}