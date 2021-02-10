package com.samchenjava.mall.member.interceptor;

import com.samchenjava.common.constant.AuthServerConstant;
import com.samchenjava.common.vo.MemberRespVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component//n.264 305
public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberRespVo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String requestURI = request.getRequestURI();//n.296
        boolean match = new AntPathMatcher().match("/member/**", requestURI);
        if(match){
            return true;
        }

        MemberRespVo attribute = (MemberRespVo) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        if(attribute!=null){
            //logged in
            loginUser.set(attribute);
            return true;
        }else{
            //not logged in
            request.getSession().setAttribute("msg","Please Login. ");
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        }
    }
}
