package com.skilal.flex_employ.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class Attendance {
    private Long attendanceId;
    private Long onDutyWorkerId;
    private Long positionId;
    private LocalDate attendanceDate;
    private LocalTime actualCheckIn;
    private LocalTime actualCheckOut;
    private String inStatus; // 正常, 迟到, 未打卡
    private String outStatus; // 正常, 早退, 未打卡
    private String attendanceStatus; // 正常, 异常, 缺勤

    // JOIN查询字段
    private String userName;
    private String positionName;
    private java.time.LocalDate workStartTime;
    private java.time.LocalDate workEndTime;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private String workingDays;
    private Integer pieceCount; // 当日完成件数， NULL=未录入或计时岗位
    private Integer isPieceWork; // JOIN关联计件标志，供前端查询展示用
}
