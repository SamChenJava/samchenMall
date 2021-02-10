package com.samchenjava.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.samchenjava.common.utils.PageUtils;
import com.samchenjava.mall.member.entity.MemberEntity;
import com.samchenjava.mall.member.exception.PhoneNumExistException;
import com.samchenjava.mall.member.exception.UsernameExistException;
import com.samchenjava.mall.member.vo.SocialUserVo;
import com.samchenjava.mall.member.vo.UserLoginVo;
import com.samchenjava.mall.member.vo.UserRegisterVo;

import java.util.Map;

/**
 * 会员
 *
 * @author samchen
 * @email nan.chen.java@gmail.com
 * @date 2020-12-30 13:39:57
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(UserRegisterVo vo);//n.216

    void checkPhoneUnique(String phone) throws PhoneNumExistException;//n.216

    void checkUsernameUnique(String username) throws UsernameExistException;//n.216

    MemberEntity login(UserLoginVo vo);//n.219

    MemberEntity login(SocialUserVo vo) throws Exception;//n.223
}

