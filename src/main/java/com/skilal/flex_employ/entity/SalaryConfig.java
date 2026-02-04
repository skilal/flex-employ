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
    private BigDecimal overtimeRate; // 加班单价
    private BigDecimal latePenalty; // 迟到扣款
    private BigDecimal earlyPenalty; // 早退扣款
    private BigDecimal absentPenalty; // 旷工扣款
    private BigDecimal sickLeaveRate; // 病假扣款比例
    private BigDecimal personalLeaveRate; // 事假扣款比例
    private LocalDateTime createdAt;
}
