package com.samchenjava.mall.search.vo;

import com.samchenjava.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SearchResult {
    private List<SkuEsModel> products;

    //following are paging info
    private Integer pageNum;//current page
    private Long total;//total result count
    private Integer totalPages;//total page number in current search
    private List<Integer> pageNavs;//n.186

    //search query from browser: attrs=1_android&attrs=5_720P:1080P&catalog3Id=255
    private List<BrandVo> brandVos;//all relevant brands in current search
    private List<CatalogVo> catalogs;//all relevant categories in current search
    private List<AttrVo> attrs;//all base attributes of products in current search

    //==== content above are info that return to page ====

    //bread crumb
    private List<NavVo> navVos = new ArrayList<>();
    private List<Long> attrIds = new ArrayList<>();//n.192 all attrIds already exist in request param

    @Data
    public static class NavVo {
        private String navName;
        private String navValue;
        private String link;
    }

    //encapsulating brand info
    @Data
    public static class BrandVo {
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class AttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }

    @Data
    public static class CatalogVo {
        private Long catalogId;
        private String catalogName;
    }
}
