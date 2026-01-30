package com.skilal.flex_employ.controller;

import com.skilal.flex_employ.common.Result;
import com.skilal.flex_employ.entity.Attendance;
import com.skilal.flex_employ.mapper.AttendanceMapper;
import com.skilal.flex_employ.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendances")
public class AttendanceController {

    @Autowired
    private AttendanceMapper attendanceMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private com.skilal.flex_employ.service.AttendanceService attendanceService;

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
}
