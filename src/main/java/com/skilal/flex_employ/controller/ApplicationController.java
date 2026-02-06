package com.skilal.flex_employ.controller;

import com.skilal.flex_employ.common.Result;
import com.skilal.flex_employ.entity.Application;
import com.skilal.flex_employ.entity.OnDutyWorker;
import com.skilal.flex_employ.mapper.ApplicationMapper;
import com.skilal.flex_employ.mapper.OnDutyWorkerMapper;
import com.skilal.flex_employ.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private OnDutyWorkerMapper onDutyWorkerMapper;

    @Autowired
    private com.skilal.flex_employ.mapper.PositionMapper positionMapper;

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

    // 检查员工是否已在某岗位在岗
    @GetMapping("/check-worker-status")
    public Result<Map<String, Object>> checkWorkerStatus(
            @RequestParam Long positionId,
            @RequestHeader("Authorization") String token) {
        token = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);

        int count = onDutyWorkerMapper.checkWorkerStatus(userId, positionId);
        boolean isOnDuty = count > 0;

        return Result.success(Map.of(
                "isOnDuty", isOnDuty,
                "message", isOnDuty ? "您已经是该岗位的在岗员工，无需再次申请" : "可以申请"));
    }

    // 检查工作时间冲突
    @GetMapping("/check-time-conflict")
    public Result<Map<String, Object>> checkTimeConflict(
            @RequestParam Long positionId,
            @RequestHeader("Authorization") String token) {
        token = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);

        // 获取要申请的岗位信息
        com.skilal.flex_employ.entity.Position targetPosition = positionMapper.findById(positionId);
        if (targetPosition == null) {
            return Result.error("岗位不存在");
        }

        // 获取用户当前所有在岗记录
        java.util.List<com.skilal.flex_employ.entity.OnDutyWorker> onDutyList = onDutyWorkerMapper.findByUserId(userId,
                null, null);

        // 检查每个在岗岗位的时间是否与目标岗位冲突
        for (com.skilal.flex_employ.entity.OnDutyWorker worker : onDutyList) {
            log.info("检查冲突 - 当前在岗记录ID: {}, 岗位ID: {}, 状态: {}",
                    worker.getOnDutyWorkerId(), worker.getPositionId(), worker.getWorkerStatus());
            if (!"在岗".equals(worker.getWorkerStatus())) {
                continue; // 只检查在岗状态的记录
            }

            // 获取在岗岗位的详细信息
            com.skilal.flex_employ.entity.Position currentPosition = positionMapper.findById(worker.getPositionId());
            if (currentPosition == null) {
                continue;
            }

            // 检查日期范围是否重叠
            if (targetPosition.getWorkStartTime() != null && targetPosition.getWorkEndTime() != null
                    && currentPosition.getWorkStartTime() != null && currentPosition.getWorkEndTime() != null) {

                boolean dateOverlap = !targetPosition.getWorkStartTime().isAfter(currentPosition.getWorkEndTime())
                        && !targetPosition.getWorkEndTime().isBefore(currentPosition.getWorkStartTime());

                if (dateOverlap) {
                    // 如果日期有重叠，进一步检查时段是否有交集
                    LocalTime tStart1 = targetPosition.getCheckInTime();
                    LocalTime tEnd1 = targetPosition.getCheckOutTime();
                    LocalTime tStart2 = currentPosition.getCheckInTime();
                    LocalTime tEnd2 = currentPosition.getCheckOutTime();

                    if (tStart1 != null && tEnd1 != null && tStart2 != null && tEnd2 != null) {
                        // 时段冲突判定算法：!(结束1 <= 开始2 || 结束2 <= 开始1)
                        boolean timeOverlap = tStart1.isBefore(tEnd2) && tEnd1.isAfter(tStart2);

                        if (timeOverlap) {
                            return Result.success(Map.of(
                                    "hasConflict", true,
                                    "conflictPosition", currentPosition.getPositionName(),
                                    "message",
                                    "该岗位工作时间与您在岗的【" + currentPosition.getPositionName() + "】岗位存在冲突 (日期重叠且时段交叠)"));
                        }
                    } else {
                        // 如果任一岗位没有设置时段，则保守起见认为凡是日期重叠即为冲突
                        return Result.success(Map.of(
                                "hasConflict", true,
                                "conflictPosition", currentPosition.getPositionName(),
                                "message", "该岗位工作时间与您在岗的【" + currentPosition.getPositionName() + "】岗位在日期上存在冲突"));
                    }
                }
            }
        }

        return Result.success(Map.of(
                "hasConflict", false,
                "message", "无时间冲突"));
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

                worker.setHireDate(LocalDate.parse(hireDateStr));
                worker.setWorkerStatus("在岗"); // 设置在岗状态

                int workerResult = onDutyWorkerMapper.insert(worker);
                if (workerResult <= 0) {
                    return Result.error("创建在岗员工记录失败");
                }

                // 减少岗位剩余人数
                positionMapper.decreaseRemainingPositions(application.getPositionId());

                // 检查剩余人数，如果为0则关闭岗位
                com.skilal.flex_employ.entity.Position position = positionMapper.findById(application.getPositionId());
                if (position != null && position.getRemainingPositions() != null
                        && position.getRemainingPositions() <= 0) {
                    positionMapper.closePosition(application.getPositionId());
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
