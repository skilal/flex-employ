package com.skilal.flex_employ.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class OnDutyWorker {
    private Long onDutyWorkerId;
    private Long userId;
    private Long positionId;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private LocalDate hireDate;
    private LocalDate leaveDate;
    private String workerStatus;

    // JOIN查询字段
    private String userName;
    private String positionName;
    private LocalDate workStartTime;
    private LocalDate workEndTime;
    private String workingDays;
}
