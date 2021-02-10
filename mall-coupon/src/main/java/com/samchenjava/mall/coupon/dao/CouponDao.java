package com.samchenjava.mall.coupon.dao;

import com.samchenjava.mall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 *
 * @author samchen
 * @email nan.chen.java@gmail.com
 * @date 2020-12-30 13:29:16
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {

}
