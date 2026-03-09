package com.skilal.flex_employ.config;

import com.skilal.flex_employ.common.Result;
import com.skilal.flex_employ.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 放行 OPTIONS 请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 获取请求头中的 Token
        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            if (jwtUtil.validateToken(jwt)) {
                // Token 有效，解析用户信息（可选：存入 request 作用域供后续使用）
                request.setAttribute("userId", jwtUtil.getUserIdFromToken(jwt));
                request.setAttribute("userRole", jwtUtil.getRoleFromToken(jwt));
                return true;
            }
        }

        // 校验失败，返回 401 状态码，前端会根据此状态码触发无感刷新或跳转登录
        log.warn("拦截未授权请求: {} {}", request.getMethod(), request.getRequestURI());

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401

        Result<Object> result = Result.error(401, "登录已失效，请尝试刷新令牌或重新登录");
        response.getWriter().write(new ObjectMapper().writeValueAsString(result));

        return false;
    }
}
