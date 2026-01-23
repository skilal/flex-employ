package com.skilal.flex_employ.controller;

import com.skilal.flex_employ.common.Result;
import com.skilal.flex_employ.entity.Application;
import com.skilal.flex_employ.mapper.ApplicationMapper;
import com.skilal.flex_employ.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public Result<List<Application>> getApplications(@RequestParam(required = false) String status) {
        List<Application> applications = applicationMapper.findAll(status);
        return Result.success(applications);
    }

    @GetMapping("/my")
    public Result<List<Application>> getMyApplications(@RequestHeader("Authorization") String token) {
        token = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        List<Application> applications = applicationMapper.findByUserId(userId);
        return Result.success(applications);
    }

    @PostMapping
    public Result<String> createApplication(@RequestBody Application application,
            @RequestHeader("Authorization") String token) {
        token = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        application.setUserId(userId);
        application.setStatus("已申请");
        int result = applicationMapper.insert(application);
        if (result > 0) {
            return Result.success("申请提交成功");
        }
        return Result.error("申请提交失败");
    }

    @PutMapping("/{id}/approve")
    public Result<String> approveApplication(@PathVariable Long id, @RequestBody Map<String, String> data) {
        String status = data.get("status");
        int result = applicationMapper.updateStatus(id, status);
        if (result > 0) {
            return Result.success("审批成功");
        }
        return Result.error("审批失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteApplication(@PathVariable Long id) {
        int result = applicationMapper.delete(id);
        if (result > 0) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}
