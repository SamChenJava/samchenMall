package com.samchenjava.mall.product.vo;

import lombok.Data;

@Data//n.77
public class AttrRespVo extends AttrVo {
    private String catelogName;
    private String groupName;
    private Long[] catelogPath;//n.78
}
