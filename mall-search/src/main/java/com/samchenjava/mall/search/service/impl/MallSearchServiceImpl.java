package com.samchenjava.mall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.samchenjava.common.to.es.SkuEsModel;
import com.samchenjava.common.utils.R;
import com.samchenjava.mall.search.config.MallElasticSearchConfig;
import com.samchenjava.mall.search.constant.EsConstant;
import com.samchenjava.mall.search.feign.ProductFeignService;
import com.samchenjava.mall.search.service.MallSearchService;
import com.samchenjava.mall.search.vo.AttrResponseVo;
import com.samchenjava.mall.search.vo.BrandVo;
import com.samchenjava.mall.search.vo.SearchParam;
import com.samchenjava.mall.search.vo.SearchResult;
import com.sun.xml.internal.bind.v2.model.core.TypeRef;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private ProductFeignService productFeignService;

    @Override//n.175
    public SearchResult search(SearchParam param) {
        SearchResult result = null;
        //prepare search request
        SearchRequest searchRequest = buildSearchRequest(param);
        try {
            //execute search request
            SearchResponse response = client.search(searchRequest, MallElasticSearchConfig.COMMON_OPTIONS);
            //analyze response data and encapsulating to we need format
            result = buildSearchResult(param, response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    //n.180 prepare search request
    private SearchRequest buildSearchRequest(SearchParam param) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //n.179 step 1: bool query
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //must
        if (StringUtils.isNotEmpty(param.getKeyword())) {
            //bool -> match
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }
        //bool -> filter
        if (param.getCatalog3Id() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }
        //bool -> filter
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }
        //bool - > filter
        if (param.getHasStock() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }
        //bool -> filter -> range , 1_500, _500, 1_
        if (StringUtils.isNotEmpty(param.getSkuPrice())) {
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("skuPrice");
            boolQueryBuilder.filter(rangeQueryBuilder);
            String[] s = param.getSkuPrice().split("_");
            if (s.length == 2) {
                rangeQueryBuilder.gte(s[0]).lte(s[1]);
            } else if (s.length == 1) {
                if (param.getSkuPrice().startsWith("_")) {
                    rangeQueryBuilder.lte(s[0]);
                }
                if (param.getSkuPrice().endsWith("_")) {
                    rangeQueryBuilder.gte(s[0]);
                }
            }
        }
        //bool -> filter
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            for (String attr : param.getAttrs()) {
                //attrs=2_5inch:8inch&attrs=3_2000mah:4000mah
                BoolQueryBuilder nestedBoolQueryBuilder = new BoolQueryBuilder();
                //attrs=2_5inch:8inch
                String[] s = attr.split("_");
                String attrId = s[0];
                String[] attrValues = s[1].split(":");
                nestedBoolQueryBuilder.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedBoolQueryBuilder.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                //wee need to build nested query for each base Attrs
                NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("attrs", nestedBoolQueryBuilder, ScoreMode.None);
                boolQueryBuilder.filter(nestedQueryBuilder);
            }

        }
        sourceBuilder.query(boolQueryBuilder);

        //n.180 step2: order, paging, highlight
        //order
        if (StringUtils.isNotEmpty(param.getSort())) {
            String sort = param.getSort();
            //sort=hotScore_asc/desc
            String[] s = sort.split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            sourceBuilder.sort(s[0], order);
        }
        //paging
        sourceBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);
        //highlight
        if (StringUtils.isNotEmpty(param.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            sourceBuilder.highlighter(highlightBuilder);
        }

        //n.181 aggregations
        //brand aggregation
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(50);
        //sub aggregation of brand aggregation
        brand_agg.subAggregation(AggregationBuilders.terms("bran_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        sourceBuilder.aggregation(brand_agg);
        //category aggregation
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        sourceBuilder.aggregation(catalog_agg);
        //base Attrs aggregation, attr_agg
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        attr_agg.subAggregation(attr_id_agg);
        sourceBuilder.aggregation(attr_agg);

        System.out.println("built DSL: " + sourceBuilder.toString());

        //String[] indices, SearchSourceBuilder source
        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);//build DSL
        return searchRequest;
    }

    //build search result data n.182
    private SearchResult buildSearchResult(SearchParam param, SearchResponse response) {
        SearchResult searchResult = new SearchResult();
        //return all products from current search
        List<SkuEsModel> skuEsModelList = new ArrayList<>();
        SearchHits hits = response.getHits();
        if (hits.getHits() != null && hits.getHits().length > 0) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                if (StringUtils.isNotEmpty(param.getKeyword())) {
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String highlightTitle = skuTitle.fragments()[0].string();
                    skuEsModel.setSkuTitle(highlightTitle);
                }
                skuEsModelList.add(skuEsModel);
            }
        }
        searchResult.setProducts(skuEsModelList);


        //all base attrs relevant to all current products
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            long attrId = bucket.getKeyAsNumber().longValue();
            String attrName = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
            List<String> attrValues = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets().stream().map(item -> {
                String keyAsString = item.getKeyAsString();
                return keyAsString;
            }).collect(Collectors.toList());
            attrVo.setAttrId(attrId);
            attrVo.setAttrName(attrName);
            attrVo.setAttrValue(attrValues);
            //searchResult.getAttrIds().add(attrId);//n.192
            attrVos.add(attrVo);
        }
        searchResult.setAttrs(attrVos);

        //all brands relevant to all current products
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brand_agg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            //private Long brandId;
            //private String brandName;
            //private String brandImg;
            long brandId = bucket.getKeyAsNumber().longValue();
            String brandName = ((ParsedStringTerms) bucket.getAggregations().get("bran_name_agg")).getBuckets().get(0).getKeyAsString();
            String brandImg = ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandId(brandId);
            brandVo.setBrandName(brandName);
            brandVo.setBrandImg(brandImg);
            brandVos.add(brandVo);
        }
        searchResult.setBrandVos(brandVos);
        //all categories info relevant to all current products
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");
        for (Terms.Bucket bucket : catalog_agg.getBuckets()) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            //private String catalogName;
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            String catalogName = catalog_name_agg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalogName);
            //private Long catalogId;
            String keyAsString = bucket.getKeyAsString();
            catalogVo.setCatalogId(Long.parseLong(keyAsString));
            catalogVos.add(catalogVo);
        }
        searchResult.setCatalogs(catalogVos);
//        ====info above from aggregation info

        //paging - pageNumber
        searchResult.setPageNum(param.getPageNum());
        //paging - total record amount
        long totalHits = hits.getTotalHits().value;
        searchResult.setTotal(totalHits);
        //paging - total page amount
        long totalPages = totalHits % EsConstant.PRODUCT_PAGESIZE == 0 ? totalHits / EsConstant.PRODUCT_PAGESIZE : (totalHits / EsConstant.PRODUCT_PAGESIZE + 1);
        searchResult.setTotalPages((int) totalPages);

        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNavs.add(i);
        }
        searchResult.setPageNavs(pageNavs);

        //n.190 build bread crumb navigation
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            List<SearchResult.NavVo> navVos = param.getAttrs().stream().map(attr -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                R r = productFeignService.attrInfo(Long.parseLong(s[0]));
                searchResult.getAttrIds().add(Long.parseLong(s[0]));//n.192 all attributes that exist in param
                if (r.getCode() == 0) {
                    AttrResponseVo data = r.getData("attr", new TypeReference<AttrResponseVo>() {
                    });
                    navVo.setNavName(data.getAttrName());
                } else {
                    navVo.setNavName(s[0]);
                }
                //n.191 set current base attribute in current request empty
                //get all query parameters, remove current
                String replace = replaceQueryString(param, attr, "attrs");
                navVo.setLink("http://search.gulimall.com/list.html?" + replace);
                return navVo;
            }).collect(Collectors.toList());

            searchResult.setNavVos(navVos);
        }

        //n.192 brand, category build to bread crumb
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            List<SearchResult.NavVo> navVos = searchResult.getNavVos();
            SearchResult.NavVo navVo = new SearchResult.NavVo();
            R r = productFeignService.brandsInfo(param.getBrandId());
            if (r.getCode() == 0) {
                List<BrandVo> brands = r.getData("brand", new TypeReference<List<BrandVo>>() {
                });
                StringBuffer buffer = new StringBuffer();
                String replace = "";
                for (BrandVo brand : brands) {
                    buffer.append(brand.getName() + ";");
                    replace = replaceQueryString(param, brand.getBrandId() + "", "brandId");
                }
                navVo.setNavName("brand ");
                navVo.setNavValue(buffer.toString());
                navVo.setLink("http://search.gulimall.com/list.html?" + replace);
            }
            navVos.add(navVo);
            searchResult.setNavVos(navVos);
        }

        return searchResult;
    }

    private String replaceQueryString(SearchParam param, String attr, String key) {
        String encode = null;
        try {
            encode = URLEncoder.encode(attr, "UTF-8");
            encode = encode.replace("+", "%20");
            encode = encode.replace("%28", "(");
            encode = encode.replace("%29", ")");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String replace = param.get_queryString().replace("&" + key + "=" + encode, "");
        return replace;
    }
}
