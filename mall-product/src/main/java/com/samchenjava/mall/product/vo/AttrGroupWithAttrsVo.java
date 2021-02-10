package com.samchenjava.mall.product.vo;

import com.samchenjava.mall.product.entity.AttrEntity;
import lombok.Data;

import java.util.List;

@Data//n.85
public class AttrGroupWithAttrsVo {
    private Long attrGroupId;

    private String attrGroupName;

    private Integer sort;

    private String descript;

    private String icon;

    private Long catelogId;

    private List<AttrEntity> attrs;
}
