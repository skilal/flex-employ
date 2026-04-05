package com.skilal.flex_employ.controller;

import com.skilal.flex_employ.common.CheckRole;
import com.skilal.flex_employ.common.Role;
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

    @CheckRole(Role.ADMIN)
    @GetMapping
    public Result<List<Attendance>> getAttendances(@RequestParam(required = false) LocalDate attendanceDate,
            @RequestParam(required = false) String attendanceStatus,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String positionName,
            @RequestParam(required = false) Boolean pieceworkOnly,
            @RequestParam(required = false) Boolean unrecordedOnly) {
        List<Attendance> attendances = attendanceMapper.findAll(attendanceDate, attendanceStatus, userName,
                positionName, pieceworkOnly, unrecordedOnly);
        return Result.success(attendances);
    }

    @CheckRole(Role.EMPLOYEE)
    @GetMapping("/my")
    public Result<List<Attendance>> getMyAttendances(@RequestHeader("Authorization") String token,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        token = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        List<Attendance> attendances = attendanceMapper.findByUserId(userId, startDate, endDate);
        return Result.success(attendances);
    }

    @CheckRole(Role.ADMIN)
    @PostMapping
    public Result<String> createAttendance(@RequestBody Attendance attendance) {
        // 检查是否已经存在该日期的考勤记录
        int existingCount = attendanceMapper.countByWorkerAndDate(
                attendance.getOnDutyWorkerId(),
                attendance.getAttendanceDate());
        if (existingCount > 0) {
            return Result.error("该员工在 " + attendance.getAttendanceDate() + " 已有考勤记录，请勿重复添加");
        }

        // 如果没有被人为设定为“旷工”，则自动计算状态
        if (!"旷工".equals(attendance.getAttendanceStatus())) {
            String status = attendanceService.calculateStatus(
                    attendance.getOnDutyWorkerId(),
                    attendance.getAttendanceDate(),
                    attendance.getActualCheckIn(),
                    attendance.getActualCheckOut());
            attendance.setAttendanceStatus(status);
        }

        int result = attendanceMapper.insert(attendance);
        if (result > 0) {
            return Result.success("创建成功");
        }
        return Result.error("创建失败");
    }

    /**
     * 录入计件数量（轻量接口，仅更新 piece_count 字段）
     */
    @CheckRole(Role.ADMIN)
    @PatchMapping("/{attendanceId}/piece-count")
    public Result<String> updatePieceCount(@PathVariable Long attendanceId,
            @RequestBody java.util.Map<String, Integer> body) {
        Integer pieceCount = body.get("pieceCount");
        if (pieceCount == null || pieceCount < 0) {
            return Result.error("件数不合法，请输入非负整数");
        }
        int rows = attendanceMapper.updatePieceCount(attendanceId, pieceCount);
        return rows > 0 ? Result.success("录入成功") : Result.error("记录不存在");
    }

    @CheckRole(Role.ADMIN)
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

        // 如果没有被人为设定为“旷工”，则自动计算状态
        if (!"旷工".equals(attendance.getAttendanceStatus())) {
            String status = attendanceService.calculateStatus(
                    attendance.getOnDutyWorkerId(),
                    attendance.getAttendanceDate(),
                    attendance.getActualCheckIn(),
                    attendance.getActualCheckOut());
            attendance.setAttendanceStatus(status);
        }

        int result = attendanceMapper.update(attendance);
        if (result > 0) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    @CheckRole(Role.ADMIN)
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
    @CheckRole(Role.EMPLOYEE)
    @PostMapping("/qr-punch")
    public Result<String> qrPunch(@RequestBody Map<String, Object> data, @RequestHeader("Authorization") String token) {
        Long positionId = Long.valueOf(data.get("positionId").toString());
        String punchType = (String) data.get("punchType"); // check-in 或 check-out
        String qrToken = (String) data.get("qrToken");

        // 核心安全校验：验证二维码令牌是否合法且未过期
        if (qrToken == null) {
            return Result.error("打卡失败：未检测到考勤码信息，请扫描二维码打卡");
        }

        try {
            String secret = "flex_punch_2024";
            String decodedToken = new String(java.util.Base64.getDecoder().decode(qrToken));
            String[] parts = decodedToken.split("-");

            // 格式必须为：positionId - timestamp - secret
            if (parts.length != 3 || !parts[0].equals(positionId.toString()) || !parts[2].equals(secret)) {
                return Result.error("打卡失败：考勤二维码无效或被篡改");
            }

            long qrTimestamp = Long.parseLong(parts[1]);
            long currentTimestamp = System.currentTimeMillis() / 1000;

            // 二维码前端每15秒刷新一次，允许15秒误差容载弱网，合记超过 30 秒即视为截图过期作废
            if (currentTimestamp - qrTimestamp > 30 || currentTimestamp - qrTimestamp < -5) {
                return Result.error("打卡失败：二维码已过期，为了防止截图代打卡，请使用现场最新鲜的考勤码");
            }
        } catch (Exception e) {
            return Result.error("打卡失败：考勤码解析异常");
        }

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
            if (result > 0) {
                // 签退成功后同步标记到 Redis Bitmap 中供统计和防重检查使用
                String yearMonth = today.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"));
                String redisKey = "attendance:worker:" + worker.getOnDutyWorkerId() + ":" + yearMonth;
                long offset = today.getDayOfMonth() - 1;
                stringRedisTemplate.opsForValue().setBit(redisKey, offset, true);
                stringRedisTemplate.expire(redisKey, 40, java.util.concurrent.TimeUnit.DAYS);
                return Result.success("签退成功 (" + now.toString().substring(0, 5) + ")");
            } else {
                return Result.error("签退失败");
            }
        }

        return Result.error("未知的打卡类型");
    }

    @Autowired
    private org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;
    @Autowired
    private com.skilal.flex_employ.mapper.PositionMapper positionMapper;

    @CheckRole(Role.ADMIN)
    @PostMapping("/manual-sign")
    public Result<String> manualSign(@RequestBody java.util.List<Long> workerIds) {
        if (workerIds == null || workerIds.isEmpty()) {
            return Result.error("请至少选择一名需要签到的员工");
        }

        LocalDate today = LocalDate.now();
        String yearMonth = today.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"));
        long offset = today.getDayOfMonth() - 1;

        int successCount = 0;
        int duplicateCount = 0;

        for (Long workerId : workerIds) {
            // 1. 获取员工
            com.skilal.flex_employ.entity.OnDutyWorker worker = onDutyWorkerMapper.findById(workerId);
            if (worker == null || !"在岗".equals(worker.getWorkerStatus())) {
                continue; // 忽略异常状态的员工
            }

            // 2. Redis Bitmap 查重
            String redisKey = "attendance:worker:" + workerId + ":" + yearMonth;
            Boolean isSigned = stringRedisTemplate.opsForValue().getBit(redisKey, offset);
            
            if (Boolean.TRUE.equals(isSigned)) {
                duplicateCount++;
                continue; // 今日已签，直接跳过处理下一个
            }

            // 3. 构建数据
            com.skilal.flex_employ.entity.Position position = positionMapper.findById(worker.getPositionId());
            LocalTime checkIn = (position != null && position.getCheckInTime() != null) ? position.getCheckInTime() : LocalTime.of(9, 0);
            LocalTime checkOut = (position != null && position.getCheckOutTime() != null) ? position.getCheckOutTime() : LocalTime.of(18, 0);

            // 清理脏数据
            Attendance existRec = attendanceMapper.findByWorkerAndDate(workerId, today);
            if (existRec != null) {
                attendanceMapper.delete(existRec.getAttendanceId());
            }

            Attendance newPunch = new Attendance();
            newPunch.setOnDutyWorkerId(workerId);
            newPunch.setPositionId(worker.getPositionId());
            newPunch.setAttendanceDate(today);
            newPunch.setActualCheckIn(checkIn);
            newPunch.setActualCheckOut(checkOut);
            newPunch.setInStatus("正常");
            newPunch.setOutStatus("正常");
            newPunch.setAttendanceStatus("正常");

            // 4. 落库并高举 Bitmap 标志
            attendanceMapper.insert(newPunch);
            stringRedisTemplate.opsForValue().setBit(redisKey, offset, true);
            stringRedisTemplate.expire(redisKey, 40, java.util.concurrent.TimeUnit.DAYS);
            successCount++;
        }

        if (successCount == 0 && duplicateCount > 0) {
            return Result.success("选中的员工今日均已签过到，已全部防止重复");
        }
        return Result.success(String.format("批量签到执行完成！成功签到 %d 人，过滤重复签到 %d 人", successCount, duplicateCount));
    }
}
