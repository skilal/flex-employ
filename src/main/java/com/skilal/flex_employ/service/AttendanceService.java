package com.skilal.flex_employ.service;

import com.skilal.flex_employ.entity.Attendance;
import com.skilal.flex_employ.entity.HolidayCalendar;
import com.skilal.flex_employ.entity.OnDutyWorker;
import com.skilal.flex_employ.mapper.AttendanceMapper;
import com.skilal.flex_employ.mapper.HolidayCalendarMapper;
import com.skilal.flex_employ.mapper.LeaveRequestMapper;
import com.skilal.flex_employ.mapper.OnDutyWorkerMapper;
import com.skilal.flex_employ.mapper.SalaryConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class AttendanceService {

    @Autowired
    private HolidayCalendarMapper holidayMapper;

    @Autowired
    private LeaveRequestMapper leaveMapper;

    @Autowired
    private OnDutyWorkerMapper workerMapper;

    @Autowired
    private AttendanceMapper attendanceMapper;

    @Autowired
    private com.skilal.flex_employ.mapper.PositionMapper positionMapper;

    @Autowired
    private SalaryConfigMapper salaryConfigMapper;

    /**
     * 计算考勤状态
     */
    public String calculateStatus(Long onDutyWorkerId, LocalDate date, LocalTime actualCheckIn,
            LocalTime actualCheckOut) {
        OnDutyWorker worker = workerMapper.findById(onDutyWorkerId);
        if (worker == null)
            return "未知";

        // 1. 判断是否请假
        if (leaveMapper.checkLeave(worker.getUserId(), date) > 0) {
            return "请假";
        }

        // 2. 判断是否为工作日
        if (!isWorkDay(worker.getPositionId(), date)) {
            return "假日"; // 非工作日归类为假日
        }

        // 3. 判断缺勤 (完全未签到)
        if (actualCheckIn == null) {
            return "缺勤";
        }

        // 4. 判定打卡状态（从岗位获取标准时间）
        com.skilal.flex_employ.entity.Position position = positionMapper.findById(worker.getPositionId());
        if (position == null || position.getCheckInTime() == null || position.getCheckOutTime() == null) {
            return "未知";
        }

        LocalTime shouldCheckIn = position.getCheckInTime();
        LocalTime shouldCheckOut = position.getCheckOutTime();

        // 获取薪资配置中的阈值
        com.skilal.flex_employ.entity.SalaryConfig config = salaryConfigMapper.findById(position.getSalaryConfigId());
        int lateThreshold = (config != null && config.getLateThresholdMin() != null) ? config.getLateThresholdMin() : 0;
        int earlyThreshold = (config != null && config.getEarlyLeaveThresholdMin() != null)
                ? config.getEarlyLeaveThresholdMin()
                : 0;

        // A. 判定签到状态
        boolean isLate = actualCheckIn.isAfter(shouldCheckIn);
        if (isLate && lateThreshold > 0) {
            long lateMinutes = java.time.Duration.between(shouldCheckIn, actualCheckIn).toMinutes();
            if (lateMinutes > lateThreshold) {
                return "缺勤"; // 严重迟到视为缺勤
            }
        }

        // B. 如果还没有签退
        if (actualCheckOut == null) {
            return isLate ? "迟到" : "正常";
        }

        // C. 判定签退状态
        boolean isEarlyLeave = actualCheckOut.isBefore(shouldCheckOut);
        if (isEarlyLeave && earlyThreshold > 0) {
            long earlyMinutes = java.time.Duration.between(actualCheckOut, shouldCheckOut).toMinutes();
            if (earlyMinutes > earlyThreshold) {
                return "缺勤"; // 严重早退视为缺勤
            }
        }

        // D. 综合判定
        if (isLate && isEarlyLeave) {
            return "迟到且早退";
        } else if (isLate) {
            return "迟到";
        } else if (isEarlyLeave) {
            return "早退";
        }

        return "正常";
    }

    /**
     * 判断某天是否为指定岗位的出勤日
     */
    public boolean isWorkDay(Long positionId, LocalDate date) {
        // A. 优先判断法定节假日/调休
        HolidayCalendar holiday = holidayMapper.findByDate(date);
        if (holiday != null) {
            if (holiday.getDayType() == 1)
                return false; // 法定节假日(休息)
            if (holiday.getDayType() == 2)
                return true; // 法定调休(补班)
        }

        // B. 结合岗位排班判断
        com.skilal.flex_employ.entity.Position position = positionMapper.findById(positionId);
        if (position == null) {
            // 回退到普通判断
            DayOfWeek dow = date.getDayOfWeek();
            return dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;
        }

        String workingDays = position.getWorkingDays();
        // 如果没有设置，默认为 1,2,3,4,5 (周一至周五)
        if (workingDays == null || workingDays.trim().isEmpty()) {
            DayOfWeek dow = date.getDayOfWeek();
            return dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;
        }

        // 检查今日周几是否在设置中 (1=周一, 7=周日)
        int dayOfWeekValue = date.getDayOfWeek().getValue();
        String dayStr = String.valueOf(dayOfWeekValue);

        String[] daysArray = workingDays.split(",");
        for (String d : daysArray) {
            if (d.trim().equals(dayStr)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 为员工补全力考勤记录（从入职至今）
     */
    public void fillMissingAttendance(OnDutyWorker worker) {
        if (worker == null || worker.getOnDutyWorkerId() == null)
            return;

        LocalDate yesterday = LocalDate.now().minusDays(1);

        // 仅检查昨天是否已有记录
        if (attendanceMapper.countByWorkerAndDate(worker.getOnDutyWorkerId(), yesterday) == 0) {
            Attendance attendance = new Attendance();
            attendance.setOnDutyWorkerId(worker.getOnDutyWorkerId());
            attendance.setPositionId(worker.getPositionId());
            attendance.setAttendanceDate(yesterday);

            // 统一调用已实现的判定逻辑
            String status = calculateStatus(worker.getOnDutyWorkerId(), yesterday, null, null);
            attendance.setAttendanceStatus(status);

            attendanceMapper.insert(attendance);
        }
    }
}
