package com.samchenjava.mall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.samchenjava.common.exception.BizCodeEnum;
import com.samchenjava.mall.member.exception.PhoneNumExistException;
import com.samchenjava.mall.member.exception.UsernameExistException;
import com.samchenjava.mall.member.service.MemberService;
import com.samchenjava.mall.member.vo.SocialUserVo;
import com.samchenjava.mall.member.vo.UserLoginVo;
import com.samchenjava.mall.member.vo.UserRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.samchenjava.mall.member.entity.MemberEntity;
import com.samchenjava.common.utils.PageUtils;
import com.samchenjava.common.utils.R;


/**
 * 会员
 *
 * @author samchen
 * @email nan.chen.java@gmail.com
 * @date 2020-12-30 13:39:57
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id) {
        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member) {
        memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member) {
        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    @PostMapping("/register")//n.216
    public R register(@RequestBody UserRegisterVo vo){
        try {
            memberService.register(vo);
        }catch (UsernameExistException e){
            return R.error(BizCodeEnum.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnum.PHONE_EXIST_EXCEPTION.getMessage());
        }catch(PhoneNumExistException e){
            return R.error(BizCodeEnum.USER_EXIST_EXCEPTION.getCode(), BizCodeEnum.USER_EXIST_EXCEPTION.getMessage());
        }
        return R.ok();
    }

    @PostMapping("/login")//n.219
    public R login(@RequestBody UserLoginVo vo){
        MemberEntity memberEntity = memberService.login(vo);
        if(memberEntity!=null){

            return R.ok().setData(memberEntity);
        }else{
            return R.error(BizCodeEnum.LOGINACCOUNT_PASSWORD_INVALID_EXCEPTION.getCode(), BizCodeEnum.LOGINACCOUNT_PASSWORD_INVALID_EXCEPTION.getMessage());
        }
    }

    @PostMapping("/oauth2/login")//n.223
    public R oauthLogin(@RequestBody SocialUserVo vo) throws Exception {
        MemberEntity memberEntity = memberService.login(vo);
        if(memberEntity!=null){
            //login success
            return R.ok().setData(memberEntity);
        }else{
            return R.error(BizCodeEnum.LOGINACCOUNT_PASSWORD_INVALID_EXCEPTION.getCode(), BizCodeEnum.LOGINACCOUNT_PASSWORD_INVALID_EXCEPTION.getMessage());
        }
    }

}
