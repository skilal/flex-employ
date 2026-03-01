package com.skilal.flex_employ.controller;

import com.aliyun.oss.AliOssUtil;
import com.skilal.flex_employ.common.Result;
import com.skilal.flex_employ.entity.User;
import com.skilal.flex_employ.mapper.UserMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
//测试阿里云oss

import org.springframework.web.multipart.MultipartFile;

@RestController
public class TestController {

    @Autowired
    private com.aliyun.oss.AliOssUtil aliOssUtil;


    @GetMapping("/test/oss-bean")
    public Result<String> testOssBean() {
        // 只要能返回成功，说明 Bean 被 Spring 自动装配了
        return Result.success("AliOssUtil Bean 注入成功: " + aliOssUtil.getClass().getName());
    }
    //测试阿里云oss
    @PostMapping("/test/oss-upload")
    public Result<String> testOssUpload(@RequestParam("file") MultipartFile file) {
        try {
            // 生成一个随机文件名，保留原文件后缀
            String originalFilename = file.getOriginalFilename();
            String ext = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            String objectName = "test/" + java.util.UUID.randomUUID() + ext;

            // 调用工具类上传
            AliOssUtil ossUtil = new AliOssUtil();
            String url = ossUtil.upload(file.getInputStream(), objectName);

            return Result.success(url); // 返回访问地址
        } catch (Exception e) {
            return Result.error("OSS 上传失败: " + e.getMessage());
        }
    }

















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
