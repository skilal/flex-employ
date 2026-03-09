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

    @Autowired
    private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

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

        // 生成双 Token
        String accessToken = jwtUtil.generateAccessToken(user.getUserId(), user.getAccount(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId(), user.getAccount(), user.getRole());

        // 将 Refresh Token 存入 Redis，Key: user:login:userId, Value: refreshToken, 有效期 7 天
        String redisKey = "user:login:" + user.getUserId();
        redisTemplate.opsForValue().set(redisKey, refreshToken, 7, java.util.concurrent.TimeUnit.DAYS);

        log.info("登录成功，userId: {}, AT: {}..., RT: {}...", user.getUserId(), accessToken.substring(0, 10),
                refreshToken.substring(0, 10));

        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", accessToken);
        data.put("refreshToken", refreshToken);
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

    @PostMapping("/refresh")
    public Result<Map<String, Object>> refreshToken(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || !jwtUtil.validateToken(refreshToken)) {
            return Result.error("无效或已过期的刷新令牌，请重新登录");
        }

        try {
            Long userId = jwtUtil.getUserIdFromToken(refreshToken);
            String redisKey = "user:login:" + userId;
            String storedToken = redisTemplate.opsForValue().get(redisKey);

            // 校验 Redis 中是否存在且一致
            if (storedToken == null || !storedToken.equals(refreshToken)) {
                return Result.error("刷新令牌已失效，请重新登录");
            }

            // 生成新的 Access Token
            String account = jwtUtil.parseToken(refreshToken).getSubject();
            String role = jwtUtil.getRoleFromToken(refreshToken);
            String newAccessToken = jwtUtil.generateAccessToken(userId, account, role);

            Map<String, Object> data = new HashMap<>();
            data.put("accessToken", newAccessToken);
            log.info("生成新AT");
            return Result.success(data);
        } catch (Exception e) {
            log.error("刷新令牌失败", e);
            return Result.error("刷新令牌失败");
        }
    }

    @PostMapping("/logout")
    public Result<String> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token != null && token.startsWith("Bearer ")) {
            try {
                String jwt = token.substring(7);
                Long userId = jwtUtil.getUserIdFromToken(jwt);
                // 清除 Redis 中的刷新令牌
                redisTemplate.delete("user:login:" + userId);
                log.info("用户 {} 已退出登录并清除 Redis 凭证", userId);
            } catch (Exception e) {
                log.warn("退出登录处理异常: {}", e.getMessage());
            }
        }
        return Result.success("退出登录成功");
    }
}
