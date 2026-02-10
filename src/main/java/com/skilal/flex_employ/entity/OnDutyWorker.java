package com.skilal.flex_employ.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class OnDutyWorker {
    private Long onDutyWorkerId;
    private Long userId;
    private Long positionId;
    private LocalDate hireDate;
    private LocalDate leaveDate;
    private String workerStatus;

    // JOIN查询字段
    private String userName;
    private String positionName;
    private LocalDate workStartTime;
    private LocalDate workEndTime;
    private String workingDays;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private Integer billingMethod; // 计费方式
    private java.math.BigDecimal baseRate; // 薪资标准
    private String payCycle; // 结算周期
    private java.math.BigDecimal overtimePay; // 加班费
    private java.math.BigDecimal socialSecurityBase; // 社保缴费基数
    private String laborCompanyName; // 所属用工单位
    private String salaryPayerName; // 劳动关系责任方
    private String responsibleName; // 负责人姓名
    private String responsiblePhone; // 负责人电话
}
