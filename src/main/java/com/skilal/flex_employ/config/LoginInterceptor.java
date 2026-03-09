package com.skilal.flex_employ.config;

import com.skilal.flex_employ.common.CheckRole;
import com.skilal.flex_employ.common.Role;
import com.skilal.flex_employ.common.Result;
import com.skilal.flex_employ.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull Object handler) throws Exception {
        // 放行 OPTIONS 请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 获取请求头中的 Token
        String token = request.getHeader("Authorization");
        String jwt = null;

        if (token != null) {
            if (token.startsWith("Bearer ")) {
                jwt = token.substring(7);
            } else {
                jwt = token; // 兼容没有 Bearer 前缀的情况
            }
        }

        if (jwt != null && jwtUtil.validateToken(jwt)) {
            Long userId = jwtUtil.getUserIdFromToken(jwt);
            String userRoleStr = jwtUtil.getRoleFromToken(jwt);

            request.setAttribute("userId", userId);
            request.setAttribute("userRole", userRoleStr);

            // --- 角色权限校验 (RBAC) ---
            if (handler instanceof HandlerMethod) {
                HandlerMethod hm = (HandlerMethod) handler;
                // 先查找方法上的注解，再查找类上的注解
                CheckRole checkRole = hm.getMethodAnnotation(CheckRole.class);
                if (checkRole == null) {
                    checkRole = hm.getBeanType().getAnnotation(CheckRole.class);
                }

                if (checkRole != null) {
                    // 该接口有角色权限要求
                    Role[] allowedRoles = checkRole.value();
                    boolean hasRole = Arrays.stream(allowedRoles)
                            .anyMatch(r -> r.name().equals(userRoleStr));

                    if (!hasRole) {
                        log.warn("拒绝访问: 用户 {} (角色 {}) 尝试访问受限接口 {} {}",
                                userId, userRoleStr, request.getMethod(), request.getRequestURI());

                        sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "权限不足，无法访问该资源");
                        return false;
                    }
                }
            }
            return true;
        }

        // 校验失败，返回 401 状态码
        log.warn("拦截未授权请求: {} {}", request.getMethod(), request.getRequestURI());
        sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "登录已失效，请重新登录");
        return false;
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String msg) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status);

        Result<Object> result = Result.error(status, msg);
        response.getWriter().write(new ObjectMapper().writeValueAsString(result));
    }
}
