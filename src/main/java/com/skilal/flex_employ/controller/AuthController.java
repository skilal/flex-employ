package com.skilal.flex_employ.controller;

import com.skilal.flex_employ.common.Result;
import com.skilal.flex_employ.entity.User;
import com.skilal.flex_employ.mapper.UserMapper;
import com.skilal.flex_employ.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        log.info("收到登录请求：{}", loginData.get("account"));

        String account = loginData.get("account");
        String password = loginData.get("password");

        if (account == null || account.isEmpty()) {
            log.error("账号为空");
            return Result.error("账号不能为空");
        }

        if (password == null || password.isEmpty()) {
            log.error("密码为空");
            return Result.error("密码不能为空");
        }

        User user = userMapper.findByAccount(account);
        if (user == null) {
            log.error("账号不存在：{}", account);
            return Result.error("账号不存在");
        }

        log.info("找到用户：{}, 角色：{}", user.getAccount(), user.getRole());

        if (!user.getPassword().equals(password)) {
            log.error("密码错误");
            return Result.error("密码错误");
        }

        if (user.getAccountStatus() != 1) {
            log.error("账号已被禁用");
            return Result.error("账号已被禁用");
        }

        String token = jwtUtil.generateToken(user.getUserId(), user.getAccount(), user.getRole());
        log.info("登录成功，生成token：{}", token.substring(0, 20) + "...");

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getUserId());
        data.put("account", user.getAccount());
        data.put("role", user.getRole());

        return Result.success(data);
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody User user) {
        User existUser = userMapper.findByAccount(user.getAccount());
        if (existUser != null) {
            return Result.error("账号已存在");
        }

        user.setAccountStatus(1);
        int result = userMapper.insert(user);
        if (result > 0) {
            return Result.success("注册成功");
        }
        return Result.error("注册失败");
    }

    @GetMapping("/current")
    public Result<User> getCurrentUser(@RequestHeader("Authorization") String token) {
        try {
            token = token.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);
            User user = userMapper.findById(userId);
            user.setPassword(null); // 不返回密码
            return Result.success(user);
        } catch (Exception e) {
            return Result.error("获取用户信息失败");
        }
    }

    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success("退出登录成功");
    }
}
