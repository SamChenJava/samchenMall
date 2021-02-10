package com.samchenjava.mall.product.dao;

import com.samchenjava.mall.product.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品属性
 *
 * @author samchen
 * @email nan.chen.java@gmail.com
 * @date 2020-12-29 17:49:08
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {

    List<Long> selectSearhableAttrIds(@Param("attrIds") List<Long> attrIds);//n.131
}
