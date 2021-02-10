package com.samchenjava.mall.product.dao;

import com.samchenjava.mall.product.entity.CommentReplayEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品评价回复关系
 *
 * @author samchen
 * @email nan.chen.java@gmail.com
 * @date 2020-12-29 17:49:09
 */
@Mapper
public interface CommentReplayDao extends BaseMapper<CommentReplayEntity> {

}
