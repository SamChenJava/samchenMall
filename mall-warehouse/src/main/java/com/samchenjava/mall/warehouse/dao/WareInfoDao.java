package com.samchenjava.mall.warehouse.dao;

import com.samchenjava.mall.warehouse.entity.WareInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 仓库信息
 *
 * @author samchen
 * @email nan.chen.java@gmail.com
 * @date 2020-12-30 14:17:11
 */
@Mapper
public interface WareInfoDao extends BaseMapper<WareInfoEntity> {

}
