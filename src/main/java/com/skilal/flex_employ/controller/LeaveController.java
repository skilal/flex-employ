package com.skilal.flex_employ.controller;

import com.skilal.flex_employ.common.Result;
import com.skilal.flex_employ.entity.LeaveRequest;
import com.skilal.flex_employ.entity.OnDutyWorker;
import com.skilal.flex_employ.mapper.LeaveRequestMapper;
import com.skilal.flex_employ.mapper.OnDutyWorkerMapper;
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
    private OnDutyWorkerMapper onDutyWorkerMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public Result<List<LeaveRequest>> getLeaves(@RequestParam(required = false) String status,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String positionName) {
        List<LeaveRequest> leaves = leaveRequestMapper.findAll(status, userName, positionName);
        return Result.success(leaves);
    }

    @GetMapping("/my")
    public Result<List<LeaveRequest>> getMyLeaves(
            @RequestParam(required = false) String leaveType,
            @RequestParam(required = false) String status,
            @RequestHeader("Authorization") String token) {
        token = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        List<LeaveRequest> leaves = leaveRequestMapper.findByUserId(userId, leaveType, status);
        return Result.success(leaves);
    }

    @PostMapping
    public Result<String> createLeave(@RequestBody LeaveRequest leaveRequest,
            @RequestHeader("Authorization") String token) {
        token = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);

        // 验证岗位ID：必须有在岗记录且状态为在岗
        List<OnDutyWorker> workerRecords = onDutyWorkerMapper.findByUserId(userId, null, null);
        boolean hasValidPosition = workerRecords.stream()
                .anyMatch(w -> w.getPositionId().equals(leaveRequest.getPositionId())
                        && w.getLeaveDate() == null);

        if (!hasValidPosition) {
            return Result.error("您在该岗位不是在岗状态，无法申请请假");
        }

        // 验证日期重叠：检查是否存在状态为“申请中”且日期有交集的记录
        int overlapCount = leaveRequestMapper.countOverlappingRequests(
                userId,
                leaveRequest.getStartDate(),
                leaveRequest.getEndDate());

        if (overlapCount > 0) {
            return Result.error("提交失败：您在所选日期范围内已存在“申请中”的请假记录，请勿重复申请");
        }

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
