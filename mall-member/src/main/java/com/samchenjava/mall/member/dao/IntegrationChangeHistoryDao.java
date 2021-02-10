package com.samchenjava.mall.member.dao;

import com.samchenjava.mall.member.entity.IntegrationChangeHistoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 积分变化历史记录
 *
 * @author samchen
 * @email nan.chen.java@gmail.com
 * @date 2020-12-30 13:39:57
 */
@Mapper
public interface IntegrationChangeHistoryDao extends BaseMapper<IntegrationChangeHistoryEntity> {

}
