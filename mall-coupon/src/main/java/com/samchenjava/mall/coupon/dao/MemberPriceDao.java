package com.samchenjava.mall.coupon.dao;

import com.samchenjava.mall.coupon.entity.MemberPriceEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品会员价格
 *
 * @author samchen
 * @email nan.chen.java@gmail.com
 * @date 2020-12-30 13:29:15
 */
@Mapper
public interface MemberPriceDao extends BaseMapper<MemberPriceEntity> {

}
