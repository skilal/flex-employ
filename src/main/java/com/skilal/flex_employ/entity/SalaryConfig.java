package com.skilal.flex_employ.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SalaryConfig {
    private Long configId;
    private String configName;
    private String payCycle; // 日结, 周结, 15日结, 月结, 一次性结算
    private Integer billingMethod; // 1: 按小时, 2: 按天
    private BigDecimal baseRate; // 基础单价
    private Integer hasOvertimePay; // 是否计加班费: 0-否, 1-是
    private BigDecimal latePenalty; // 迟到扣款
    private BigDecimal earlyPenalty; // 早退扣款
    private BigDecimal absentPenalty; // 旷工扣款
    private BigDecimal sickLeaveRate; // 病假扣款比例
    private BigDecimal personalLeaveRate; // 事假扣款比例

    // 新增字段：绩效、提成、奖金
    private BigDecimal performanceBonus;
    private BigDecimal commission;
    private BigDecimal bonus;

    // 新增字段：五险一金费率（个人部分）
    private BigDecimal pensionRate;
    private BigDecimal medicalRate;
    private BigDecimal unemploymentRate;
    private BigDecimal injuryRate;
    private BigDecimal housingFundRate;

    // 新增字段：五险一金费率（企业部分）
    private BigDecimal pensionRateEnt;
    private BigDecimal medicalRateEnt;
    private BigDecimal unemploymentRateEnt;
    private BigDecimal injuryRateEnt;
    private BigDecimal housingFundRateEnt;

    // 新增字段：社保基数上下限
    private BigDecimal socialSecurityBaseUpper;
    private BigDecimal socialSecurityBaseLower;

    // 新增字段：多倍加班费率与计算规则
    private BigDecimal overtimeWeekdayMultiplier;
    private BigDecimal overtimeWeekendMultiplier;
    private BigDecimal overtimeHolidayMultiplier;
    private Integer overtimeCalcMode; // 1: 起算阈值, 2: 取整计算, 3: 分钟折算
    private Integer overtimeThresholdMin; // 加班起算阈值(分钟)
    private BigDecimal overtimeRoundingUnit; // 取整单位(如1.0或0.5)

    private LocalDateTime createdAt;
}
