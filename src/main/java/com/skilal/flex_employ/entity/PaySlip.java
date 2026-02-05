package com.skilal.flex_employ.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PaySlip {
    private Long payRecordId;
    private Long onDutyWorkerId;
    private LocalDate cycleStart;
    private LocalDate cycleEnd;
    private LocalDate deadlineDate; // 最晚发放日期
    private LocalDate actualPaymentDate; // 实际支付日期（NULL=未支付）

    private String paymentMethod;

    private BigDecimal basePay; // 底薪实发
    private BigDecimal bonusPay; // 绩效/奖金
    private BigDecimal overtimePay;
    private BigDecimal allowance; // 补贴

    private BigDecimal pensionDeduction; // 养老
    private BigDecimal medicalDeduction; // 医疗
    private BigDecimal unemploymentDeduction; // 失业
    private BigDecimal injuryDeduction; // 工伤
    private BigDecimal pfDeduction; // 公积金
    private BigDecimal taxAmount; // 所得税

    private BigDecimal lateDeduction;
    private BigDecimal earlyLeaveDeduction;
    private BigDecimal absentDeduction;
    private BigDecimal absenceDeduction;
    private BigDecimal leaveDeduction; // 请假扣款

    private BigDecimal grossPay; // 应发
    private BigDecimal totalDeduction; // 扣款合计
    private BigDecimal netPay; // 实发

    private Integer confirmStatus; // 1: 待确认, 2: 已确认

    private LocalDateTime createdAt;

    // 冗余显示字段
    private transient String userName;
    private transient String positionName;
}
