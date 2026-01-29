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
    private String attendanceStatus;

    // JOIN查询字段
    private String userName;
    private String positionName;
}
