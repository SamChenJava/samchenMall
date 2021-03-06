package com.samchenjava.mall.coupon.dao;

import com.samchenjava.mall.coupon.entity.SeckillSkuRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 秒杀活动商品关联
 *
 * @author samchen
 * @email nan.chen.java@gmail.com
 * @date 2020-12-30 13:29:15
 */
@Mapper
public interface SeckillSkuRelationDao extends BaseMapper<SeckillSkuRelationEntity> {

}
