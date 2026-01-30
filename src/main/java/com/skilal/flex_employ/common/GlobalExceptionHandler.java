package com.skilal.flex_employ.common;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理 JWT 过期异常
     */
    @ExceptionHandler(ExpiredJwtException.class)
    public Result<String> handleExpiredJwtException(ExpiredJwtException e) {
        log.error("Token 已过期: {}", e.getMessage());
        return Result.error(401, "登录已过期，请重新登录");
    }

    /**
     * 处理其他 JWT 异常
     */
    @ExceptionHandler(JwtException.class)
    public Result<String> handleJwtException(JwtException e) {
        log.error("Token 解析失败: {}", e.getMessage());
        return Result.error(401, "解析凭证失败，请重新登录");
    }

    /**
     * 通用异常处理
     */
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("系统未知错误: ", e);
        return Result.error(500, "服务器繁忙，请稍后再试");
    }
}
