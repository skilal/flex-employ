package com.skilal.flex_employ.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaySlip {
    private Long payRecordId;
    private Long onDutyWorkerId;
    private LocalDate cycleStart;
    private LocalDate cycleEnd;
    private LocalDate paymentDate;
    private BigDecimal basePay;
    private BigDecimal performanceBonus;
    private BigDecimal overtimePay;
    private BigDecimal allowance;
    private BigDecimal actualWorkTime;
    private BigDecimal insuranceTotal;
    private BigDecimal pfContribution;
    private BigDecimal taxAmount;
    private BigDecimal lateDeduction;
    private BigDecimal customAddDeduct;
    private BigDecimal grossPay;
    private BigDecimal totalDeduction;
    private BigDecimal netPay;
    private String paymentStatus;
}
