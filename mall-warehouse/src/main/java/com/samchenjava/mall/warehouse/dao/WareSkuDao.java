package com.samchenjava.mall.warehouse.dao;

import com.samchenjava.mall.warehouse.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 *
 * @author samchen
 * @email nan.chen.java@gmail.com
 * @date 2020-12-30 14:17:11
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    Long getSkuStock(@Param("skuId") Long skuId);//n.132

    List<Long> listWareIdHasSkuStock(@Param("skuId") Long skuId);//n.281

    Long lockSkuStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("num") Integer num);//n.281

    void unlockStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("quantity") Integer quantity);//n.295
}
