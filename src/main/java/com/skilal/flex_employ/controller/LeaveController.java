package com.skilal.flex_employ.controller;

import com.skilal.flex_employ.common.Result;
import com.skilal.flex_employ.entity.LeaveRequest;
import com.skilal.flex_employ.mapper.LeaveRequestMapper;
import com.skilal.flex_employ.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leaves")
public class LeaveController {

    @Autowired
    private LeaveRequestMapper leaveRequestMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public Result<List<LeaveRequest>> getLeaves(@RequestParam(required = false) String status) {
        List<LeaveRequest> leaves = leaveRequestMapper.findAll(status);
        return Result.success(leaves);
    }

    @GetMapping("/my")
    public Result<List<LeaveRequest>> getMyLeaves(@RequestHeader("Authorization") String token) {
        token = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        List<LeaveRequest> leaves = leaveRequestMapper.findByUserId(userId);
        return Result.success(leaves);
    }

    @PostMapping
    public Result<String> createLeave(@RequestBody LeaveRequest leaveRequest,
            @RequestHeader("Authorization") String token) {
        token = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        leaveRequest.setUserId(userId);
        leaveRequest.setStatus("申请中");
        int result = leaveRequestMapper.insert(leaveRequest);
        if (result > 0) {
            return Result.success("请假申请提交成功");
        }
        return Result.error("请假申请提交失败");
    }

    @PutMapping("/{id}/approve")
    public Result<String> approveLeave(@PathVariable Long id, @RequestBody Map<String, String> data) {
        String status = data.get("status");
        int result = leaveRequestMapper.updateStatus(id, status);
        if (result > 0) {
            return Result.success("审批成功");
        }
        return Result.error("审批失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteLeave(@PathVariable Long id) {
        int result = leaveRequestMapper.delete(id);
        if (result > 0) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}
