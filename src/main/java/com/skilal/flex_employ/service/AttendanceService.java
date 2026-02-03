package com.skilal.flex_employ.service;

import com.skilal.flex_employ.entity.HolidayCalendar;
import com.skilal.flex_employ.entity.OnDutyWorker;
import com.skilal.flex_employ.mapper.HolidayCalendarMapper;
import com.skilal.flex_employ.mapper.LeaveRequestMapper;
import com.skilal.flex_employ.mapper.OnDutyWorkerMapper;
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
    private com.skilal.flex_employ.mapper.PositionMapper positionMapper;

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
            return "正常"; // 非工作日不计缺勤
        }

        // 3. 判断缺勤
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

        boolean isLate = actualCheckIn.isAfter(shouldCheckIn);

        // 如果还没有签退，只判断是否迟到
        if (actualCheckOut == null) {
            return isLate ? "迟到" : "正常";
        }

        // 有签退时间，综合判定
        boolean isEarlyLeave = actualCheckOut.isBefore(shouldCheckOut);

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
}
