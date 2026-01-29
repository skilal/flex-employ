package com.skilal.flex_employ.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

    // 公司名称（不存储在数据库，由查询join获取）
    private transient String companyName;

    private LocalDateTime createdAt;
    private Long creatorId;
}
