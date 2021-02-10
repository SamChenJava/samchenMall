package com.samchenjava.mall.auth.feign;

import com.samchenjava.common.utils.R;
import com.samchenjava.mall.auth.vo.SocialUserVo;
import com.samchenjava.mall.auth.vo.UserLoginVo;
import com.samchenjava.mall.auth.vo.UserRegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("mall-member")//n.218
public interface MemberFeignService {
    @PostMapping("member/member/register")//n.216
    R register(@RequestBody UserRegisterVo vo);

    @PostMapping("member/member/login")//n.219
    R login(@RequestBody UserLoginVo vo);

    @PostMapping("member/member/oauth2/login")//n.223
    R oauthLogin(@RequestBody SocialUserVo vo) throws Exception;
}
