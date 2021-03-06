package com.samchenjava.mall.member.dao;

import com.samchenjava.mall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 *
 * @author samchen
 * @email nan.chen.java@gmail.com
 * @date 2020-12-30 13:39:57
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {

}
