package com.samchenjava.mall.coupon.dao;

import com.samchenjava.mall.coupon.entity.CouponHistoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券领取历史记录
 *
 * @author samchen
 * @email nan.chen.java@gmail.com
 * @date 2020-12-30 13:29:16
 */
@Mapper
public interface CouponHistoryDao extends BaseMapper<CouponHistoryEntity> {

}
