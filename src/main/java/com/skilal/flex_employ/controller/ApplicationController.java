package com.skilal.flex_employ.controller;

import com.skilal.flex_employ.common.Result;
import com.skilal.flex_employ.entity.Application;
import com.skilal.flex_employ.entity.OnDutyWorker;
import com.skilal.flex_employ.mapper.ApplicationMapper;
import com.skilal.flex_employ.mapper.OnDutyWorkerMapper;
import com.skilal.flex_employ.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private OnDutyWorkerMapper onDutyWorkerMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public Result<List<Application>> getApplications(@RequestParam(required = false) String status,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String positionName) {
        List<Application> applications = applicationMapper.findAll(status, userName, positionName);
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
    public Result<String> approveApplication(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        String status = (String) data.get("status");

        // 更新申请状态
        int result = applicationMapper.updateStatus(id, status);
        if (result <= 0) {
            return Result.error("审批失败");
        }

        // 如果审批通过，创建在岗员工记录
        if ("已通过".equals(status)) {
            try {
                // 获取申请信息
                Application application = applicationMapper.findById(id);
                if (application == null) {
                    return Result.error("申请不存在");
                }

                // 创建在岗员工记录
                OnDutyWorker worker = new OnDutyWorker();
                worker.setUserId(application.getUserId());
                worker.setPositionId(application.getPositionId());

                // 从请求中获取入职信息
                String hireDateStr = (String) data.get("hireDate");
                String checkInTimeStr = (String) data.get("checkInTime");
                String checkOutTimeStr = (String) data.get("checkOutTime");

                worker.setHireDate(LocalDate.parse(hireDateStr));
                worker.setCheckInTime(LocalTime.parse(checkInTimeStr));
                worker.setCheckOutTime(LocalTime.parse(checkOutTimeStr));
                // 不设置workerStatus，由前端根据leaveDate判断

                int workerResult = onDutyWorkerMapper.insert(worker);
                if (workerResult <= 0) {
                    return Result.error("创建在岗员工记录失败");
                }
            } catch (Exception e) {
                return Result.error("处理入职信息失败: " + e.getMessage());
            }
        }

        return Result.success("审批成功");
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
