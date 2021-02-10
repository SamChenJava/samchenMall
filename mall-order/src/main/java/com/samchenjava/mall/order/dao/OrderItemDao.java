package com.samchenjava.mall.order.dao;

import com.samchenjava.mall.order.entity.OrderItemEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单项信息
 *
 * @author samchen
 * @email nan.chen.java@gmail.com
 * @date 2020-12-30 14:10:54
 */
@Mapper
public interface OrderItemDao extends BaseMapper<OrderItemEntity> {

}
