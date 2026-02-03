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
    private BigDecimal basicSalary;
    private String payCycle;
    private String salaryDesc;
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
    private Integer billingMethod; // 计费方式: 1-按小时, 2-按天
    private BigDecimal overtimePay; // 加班费 (元/小时)

    // 公司名称（不存储在数据库，由查询join获取）
    private transient String companyName;

    private LocalDateTime createdAt;
    private Long creatorId;
}
