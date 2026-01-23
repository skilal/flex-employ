package com.skilal.flex_employ.controller;

import com.skilal.flex_employ.common.Result;
import com.skilal.flex_employ.entity.User;
import com.skilal.flex_employ.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class TestController {

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/test/db")
    public Result<Map<String, Object>> testDatabase() {
        Map<String, Object> result = new HashMap<>();
        try {
            User admin = userMapper.findByAccount("admin");
            if (admin != null) {
                admin.setPassword("***"); // 隐藏密码
                result.put("admin", admin);
                result.put("message", "数据库连接成功");
            } else {
                result.put("message", "数据库连接成功，但未找到 admin 用户");
            }
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("message", "数据库连接失败");
        }
        return Result.success(result);
    }

    @PostMapping("/test/login")
    public Result<Map<String, Object>> testLogin(@RequestBody Map<String, String> data) {
        Map<String, Object> result = new HashMap<>();
        String account = data.get("account");
        String password = data.get("password");

        result.put("received_account", account);
        result.put("received_password", password != null ? "***" : "null");

        try {
            User user = userMapper.findByAccount(account);
            if (user != null) {
                result.put("user_found", true);
                result.put("user_role", user.getRole());
                result.put("password_match", user.getPassword().equals(password));
                result.put("account_status", user.getAccountStatus());
            } else {
                result.put("user_found", false);
            }
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }

        return Result.success(result);
    }

    @GetMapping("/test/cors")
    public Result<String> testCors() {
        return Result.success("CORS 测试成功！");
    }
}
