package com.skilal.flex_employ.common;

/**
 * 业务相关的自定义异常
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
