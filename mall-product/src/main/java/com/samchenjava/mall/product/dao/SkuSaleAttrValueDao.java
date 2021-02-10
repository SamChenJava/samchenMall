package com.samchenjava.mall.product.dao;

import com.samchenjava.mall.product.entity.SkuSaleAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.samchenjava.mall.product.vo.SkuItemSaleAttrVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * sku销售属性&值
 *
 * @author samchen
 * @email nan.chen.java@gmail.com
 * @date 2020-12-29 17:49:09
 */
@Mapper
public interface SkuSaleAttrValueDao extends BaseMapper<SkuSaleAttrValueEntity> {

    List<SkuItemSaleAttrVo> getSaleAttrBySpuId(@Param("spuId") Long spuId);//n.206

    List<String> getSkuSaleAttrValuesAsStringList(@Param("skuId") Long skuId);//n.241
}
