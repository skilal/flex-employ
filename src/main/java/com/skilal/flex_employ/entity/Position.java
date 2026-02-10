package com.skilal.flex_employ.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class Position {
    private Long positionId;
    private String positionName;
    private String workLocation;
    private String regionCode;
    private String dutyDesc;
    private LocalDate workStartTime;
    private LocalDate workEndTime;
    private String employmentType;
    private Long laborCompanyId;
    private Long salaryPayerId; // 薪水发放主体公司ID，null表示由人力公司直接发放

    // 薪资体系重构：改用配置模版关联
    private Long salaryConfigId;

    private String salaryDesc; // 备注描述
    private BigDecimal dailyHours;
    private Integer weeklyFreq;
    private Integer positionStatus;
    private Long responsibleId;
    private String specialNote;
    private Integer totalPositions; // 招聘总人数
    private Integer remainingPositions; // 剩余招聘人数
    private String workingDays; // 每周工作日，例如 "1,2,3,4,5" 代表周一至周五
    private LocalTime checkInTime; // 应签到时间
    private LocalTime checkOutTime; // 应签退时间

    // 冗余字段（由查询 JOIN 获取）
    private transient String companyName;
    private transient String salaryConfigName;
    private transient String salaryPayerName;
    private transient String responsibleName;
    private transient String responsiblePhone;

    // 冗余显示字段：薪资配置详情
    private transient String payCycle;
    private transient Integer billingMethod;
    private transient BigDecimal baseRate;
    private transient Integer hasOvertimePay;
    private transient BigDecimal overtimeWeekdayMultiplier;
    private transient BigDecimal overtimeWeekendMultiplier;
    private transient BigDecimal overtimeHolidayMultiplier;

    private LocalDateTime createdAt;
    private Long creatorId;
}
