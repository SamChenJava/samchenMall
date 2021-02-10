package com.samchenjava.mall.product.vo;

import lombok.Data;

@Data//n.76
public class AttrVo {
    private Long attrId;

    private String attrName;
    /**
     * whether can be search[0-no need，1-need]
     */
    private Integer searchType;

    private String icon;
    /**
     * 可选值列表[用逗号分隔]
     */
    private String valueSelect;
    /**
     * attrbute type: 0-sale attribute 1-base attribute
     */
    private Integer attrType;
    /**
     * active status[0-inactive,1-active]
     */
    private Long enable;
    /**
     * 所属分类
     */
    private Long catelogId;
    /**
     * 快速展示【是否展示在介绍上；0-否 1-是】，在sku中仍然可以调整
     * express display
     */
    private Integer showDesc;
    /**
     * 值类型[0-为单个值，1-可以选择多个值]
     * value type[0-single value 1-multi value]
     */
    private Integer valueType;

    private Long attrGroupId;
}
