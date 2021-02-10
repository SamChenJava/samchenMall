package com.samchenjava.mall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.samchenjava.common.constant.AuthServerConstant;
import com.samchenjava.common.exception.BizCodeEnum;
import com.samchenjava.common.utils.R;
import com.samchenjava.common.vo.MemberRespVo;
import com.samchenjava.mall.auth.feign.MemberFeignService;
import com.samchenjava.mall.auth.feign.ThirdPartyFeignService;
import com.samchenjava.mall.auth.vo.UserLoginVo;
import com.samchenjava.mall.auth.vo.UserRegisterVo;
import com.sun.xml.internal.bind.v2.model.core.TypeRef;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class LoginController {

    @Autowired
    ThirdPartyFeignService thirdPartyFeignService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    MemberFeignService memberFeignService;

    @GetMapping("/sms/sendCode")
    @ResponseBody
    public R sendCode(@RequestParam("phone") String phone) {
        String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (StringUtils.isNotEmpty(redisCode)) {
            long l = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - l < 60000) {
                return R.error(BizCodeEnum.VALID_SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.VALID_SMS_CODE_EXCEPTION.getMessage());
            }
        }
        String code = UUID.randomUUID().toString().substring(0, 5);
        System.out.println("code: " + code);
        //sms code verification, key- sms:code:phoneNumber, value- code
        String codeAndTime = code + "_" + System.currentTimeMillis();
        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
        opsForValue.set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, codeAndTime, 10, TimeUnit.MINUTES);
        thirdPartyFeignService.sendCode(phone, code);
        return R.ok();
    }

    @PostMapping("/register")
    public String register(@Valid UserRegisterVo vo, BindingResult bindingResult, RedirectAttributes redirectAttributes){
        //user submitted form verification
        if(bindingResult.hasErrors()){
            /*bindingResult.getFieldErrors().stream().map(fieldError -> {
                String key = fieldError.getField();
                String value = fieldError.getDefaultMessage();
                errorMap.put(key,value);
            })*/
            Map<String, String> errorMap = bindingResult.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            redirectAttributes.addFlashAttribute("errors", errorMap);
            return "redirect:http://auth.gulimall.com/reg.html";
        }
        //user register form verification passed and verify code
        String stringInCache = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if(StringUtils.isNotEmpty(stringInCache)){
            if(StringUtils.equals(stringInCache.split("_")[0], vo.getCode())){
                //deleting verification code, token mechanism
                stringRedisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
                //verifying verification code passed, not registered user
                R r = memberFeignService.register(vo);
                if(r.getCode() == 0){
                    //succeed
                    return "redirect://auth.gulimall.com/login.html";
                }else{
                    Map<String, String> errorMap = new HashMap<>();
                    errorMap.put("msg", r.getData("msg", new TypeReference<String>(){}));
                    redirectAttributes.addFlashAttribute("errors", errorMap);
                    return "redirect://auth.gulimall.com/reg.html";
                }
            }else{
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("code", "verification code incorrect");
                redirectAttributes.addFlashAttribute("errors", errorMap);
                return "redirect://auth.gulimall.com/reg.html";
            }
        }else{
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("code", "verification code not exist");
            redirectAttributes.addFlashAttribute("errors", errorMap);
            return "redirect://auth.gulimall.com/reg.html";
        }

    }

    @PostMapping("login")//n.219
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes, HttpSession session){
        R r = memberFeignService.login(vo);
        if(r.getCode() == 0){
            MemberRespVo memberRespVo = r.getData("data", new TypeReference<MemberRespVo>() {
            });
            //n.230 login succeed and put in session
            session.setAttribute(AuthServerConstant.LOGIN_USER, memberRespVo);
            return "redirect:http://gulimall.com";
        }else{
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("msg", r.getData("msg", new TypeReference<String>(){}));
            redirectAttributes.addFlashAttribute("errors", errorMap);
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }

    @GetMapping("/login.html")//n.230
    public String loginPage(HttpSession session){
        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
        if(attribute == null){
            return "login";
        }else{
            return "redirect:http://gulimall.com";
        }
    }
}
