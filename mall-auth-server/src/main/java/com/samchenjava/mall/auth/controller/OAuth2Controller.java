package com.samchenjava.mall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.samchenjava.common.utils.HttpUtils;
import com.samchenjava.common.utils.R;
import com.samchenjava.mall.auth.feign.MemberFeignService;
import com.samchenjava.common.vo.MemberRespVo;
import com.samchenjava.mall.auth.vo.SocialUserVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;
import javax.websocket.server.PathParam;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller//n.222
public class OAuth2Controller {

    @Autowired
    MemberFeignService memberFeignService;

    @GetMapping("/oauth2.0/weibo/success")//n.223
    public String weibo(@PathParam("code")String code, HttpSession session) throws Exception {
        //exchange access_token by code
        Map<String, String> map = new HashMap<>();
        map.put("client_id","4266789714");
        map.put("client_secret","e6175699a1588fd132b74edc972365db");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://auth.gulimall.com/oauth2.0/weibo/success");
        map.put("code", code);
        HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", new HashMap<>(), null, map);
        if(response.getStatusLine().getStatusCode() == 200){
            //oauth login success
            String responseEntity = EntityUtils.toString(response.getEntity());
            SocialUserVo socialUserVo = JSON.parseObject(responseEntity, SocialUserVo.class);
            //login or register this oauth user
            R r = memberFeignService.oauthLogin(socialUserVo);
            if(r.getCode()==0){
                MemberRespVo memberRespVo = r.getData("data", new TypeReference<MemberRespVo>() {
                });
                log.info("login succeed，{}", memberRespVo);
                session.setAttribute("loginUser", memberRespVo);
                return "redirect:http://gulimall.com";
            }else{
                return "redirect:http://auth.guliamll.com/login.html";
            }
        }else{
            return "redirect:http://oauth.gulimall.com/login.html";
        }
        //redirect to index page if success
    }
}
