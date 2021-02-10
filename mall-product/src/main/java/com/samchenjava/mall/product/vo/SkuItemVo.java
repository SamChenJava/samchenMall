package com.samchenjava.mall.product.vo;

import com.samchenjava.mall.product.entity.SkuImagesEntity;
import com.samchenjava.mall.product.entity.SkuInfoEntity;
import com.samchenjava.mall.product.entity.SpuInfoDescEntity;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data//n.205
public class SkuItemVo {
    //1 sku基本信息获取 pms_sku_info
    SkuInfoEntity skuInfoEntity;
    boolean hasStock = true;
    //2 sku图片信息 pms_sku_images
    List<SkuImagesEntity> skuImages;
    //3 获取spu销售属性组合
    List<SkuItemSaleAttrVo> saleAttrs;
    //4 获取spu介绍
    SpuInfoDescEntity desp;
    //5 获取spu规格参数信息
    List<SpuItemAttrGroupVo> attrGroups;
    //v319 当前商品的秒杀优惠信息
    SeckillInfoVo seckillInfo;
}
