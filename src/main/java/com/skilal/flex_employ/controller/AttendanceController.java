package com.skilal.flex_employ.controller;

import com.skilal.flex_employ.common.Result;
import com.skilal.flex_employ.entity.Attendance;
import com.skilal.flex_employ.mapper.AttendanceMapper;
import com.skilal.flex_employ.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendances")
public class AttendanceController {

    @Autowired
    private AttendanceMapper attendanceMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private com.skilal.flex_employ.service.AttendanceService attendanceService;

    @Autowired
    private com.skilal.flex_employ.mapper.OnDutyWorkerMapper onDutyWorkerMapper;

    @GetMapping
    public Result<List<Attendance>> getAttendances(@RequestParam(required = false) LocalDate attendanceDate,
            @RequestParam(required = false) String attendanceStatus,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String positionName) {
        List<Attendance> attendances = attendanceMapper.findAll(attendanceDate, attendanceStatus, userName,
                positionName);
        return Result.success(attendances);
    }

    @GetMapping("/my")
    public Result<List<Attendance>> getMyAttendances(@RequestHeader("Authorization") String token,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        token = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        List<Attendance> attendances = attendanceMapper.findByUserId(userId, startDate, endDate);
        return Result.success(attendances);
    }

    @PostMapping
    public Result<String> createAttendance(@RequestBody Attendance attendance) {
        // 检查是否已经存在该日期的考勤记录
        int existingCount = attendanceMapper.countByWorkerAndDate(
                attendance.getOnDutyWorkerId(),
                attendance.getAttendanceDate());
        if (existingCount > 0) {
            return Result.error("该员工在 " + attendance.getAttendanceDate() + " 已有考勤记录，请勿重复添加");
        }

        // 自动计算状态
        String status = attendanceService.calculateStatus(
                attendance.getOnDutyWorkerId(),
                attendance.getAttendanceDate(),
                attendance.getActualCheckIn(),
                attendance.getActualCheckOut());
        attendance.setAttendanceStatus(status);

        int result = attendanceMapper.insert(attendance);
        if (result > 0) {
            return Result.success("创建成功");
        }
        return Result.error("创建失败");
    }

    @PutMapping("/{id}")
    public Result<String> updateAttendance(@PathVariable Long id, @RequestBody Attendance attendance) {
        attendance.setAttendanceId(id);

        // 检查修改后的日期是否冲突（排除当前记录）
        int existingCount = attendanceMapper.countByWorkerAndDateExcludeId(
                attendance.getOnDutyWorkerId(),
                attendance.getAttendanceDate(),
                id);
        if (existingCount > 0) {
            return Result.error("修改失败：该员工在 " + attendance.getAttendanceDate() + " 已有其他考勤记录");
        }

        // 自动计算状态
        String status = attendanceService.calculateStatus(
                attendance.getOnDutyWorkerId(),
                attendance.getAttendanceDate(),
                attendance.getActualCheckIn(),
                attendance.getActualCheckOut());
        attendance.setAttendanceStatus(status);

        int result = attendanceMapper.update(attendance);
        if (result > 0) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteAttendance(@PathVariable Long id) {
        int result = attendanceMapper.delete(id);
        if (result > 0) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 二维码扫码打卡接口
     */
    @PostMapping("/qr-punch")
    public Result<String> qrPunch(@RequestBody Map<String, Object> data, @RequestHeader("Authorization") String token) {
        Long positionId = Long.valueOf(data.get("positionId").toString());
        String punchType = (String) data.get("punchType"); // check-in 或 check-out

        token = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);

        // 1. 查找在该岗位的有效在岗记录
        com.skilal.flex_employ.entity.OnDutyWorker worker = onDutyWorkerMapper.findByUserIdAndPositionId(userId,
                positionId);
        if (worker == null) {
            return Result.error("打卡失败：您当前未处于该岗位的在岗状态");
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // 2. 查找今日考勤记录
        Attendance attendance = attendanceMapper.findByWorkerAndDate(worker.getOnDutyWorkerId(), today);

        if ("check-in".equals(punchType)) {
            if (attendance != null && attendance.getActualCheckIn() != null) {
                return Result.error("您今日已完成签到");
            }
            if (attendance == null) {
                attendance = new Attendance();
                attendance.setOnDutyWorkerId(worker.getOnDutyWorkerId());
                attendance.setPositionId(positionId);
                attendance.setAttendanceDate(today);
            }
            attendance.setActualCheckIn(now);
            // 重新判定状态
            attendance.setAttendanceStatus(attendanceService.calculateStatus(
                    worker.getOnDutyWorkerId(), today, attendance.getActualCheckIn(), attendance.getActualCheckOut()));

            int result = (attendance.getAttendanceId() == null) ? attendanceMapper.insert(attendance)
                    : attendanceMapper.update(attendance);
            return result > 0 ? Result.success("签到成功 (" + now.toString().substring(0, 5) + ")") : Result.error("签到失败");

        } else if ("check-out".equals(punchType)) {
            if (attendance == null) {
                return Result.error("签退失败：未找到今日签到记录，请先签到");
            }
            attendance.setActualCheckOut(now);
            // 重新判定状态
            attendance.setAttendanceStatus(attendanceService.calculateStatus(
                    worker.getOnDutyWorkerId(), today, attendance.getActualCheckIn(), attendance.getActualCheckOut()));

            int result = attendanceMapper.update(attendance);
            return result > 0 ? Result.success("签退成功 (" + now.toString().substring(0, 5) + ")") : Result.error("签退失败");
        }

        return Result.error("未知的打卡类型");
    }
}
