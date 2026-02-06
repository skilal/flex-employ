package com.skilal.flex_employ.service;

import com.skilal.flex_employ.entity.*;
import com.skilal.flex_employ.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SalaryService {

    @Autowired
    private PaySlipMapper paySlipMapper;

    @Autowired
    private SalaryConfigMapper salaryConfigMapper;

    @Autowired
    private AttendanceMapper attendanceMapper;

    @Autowired
    private LeaveRequestMapper leaveRequestMapper;

    @Autowired
    private OnDutyWorkerMapper workerMapper; // 改用 OnDutyWorkerMapper

    @Autowired
    private PositionMapper positionMapper;

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private HolidayCalendarMapper holidayCalendarMapper;

    // 每天凌晨2点自动生成薪资记录
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void autoGeneratePaySlips() {
        // 查找所有在岗员工
        List<OnDutyWorker> activeWorkers = workerMapper.findAllActive();
        for (OnDutyWorker worker : activeWorkers) {
            // 首先补全该员工的缺失考勤（缺勤、休假等）
            attendanceService.fillMissingAttendance(worker);
            // 然后尝试生成薪资条
            generateForWorker(worker);
        }
    }

    public void generateForWorker(OnDutyWorker worker) {
        // 1. 获取岗位和薪资配置
        Position position = positionMapper.findById(worker.getPositionId());
        if (position == null || position.getSalaryConfigId() == null)
            return;

        SalaryConfig config = salaryConfigMapper.findById(position.getSalaryConfigId());
        if (config == null)
            return;

        // 2. 确定计薪周期
        PaySlip lastSlip = paySlipMapper.findLatestByWorkerId(worker.getOnDutyWorkerId());
        LocalDate cycleStart;
        if (lastSlip != null) {
            cycleStart = lastSlip.getCycleEnd().plusDays(1);
        } else {
            cycleStart = worker.getHireDate() != null ? worker.getHireDate() : LocalDate.now().minusMonths(1);
        }

        LocalDate cycleEnd = calculateCycleEnd(cycleStart, config.getPayCycle());
        // 确保周期结束日已过（即至少到了结束日的次日）且考勤已记录
        if (!cycleEnd.isBefore(LocalDate.now()))
            return;

        // 检查是否已经生成过（防止重复）
        if (paySlipMapper.existsByRange(worker.getOnDutyWorkerId(), cycleStart, cycleEnd))
            return;

        // 3. 统计考勤与请假有效天数
        List<Attendance> attendances = attendanceMapper.findByWorkerAndRange(worker.getOnDutyWorkerId(), cycleStart,
                cycleEnd);
        List<LeaveRequest> leaves = leaveRequestMapper.findApprovedByRange(worker.getUserId(), worker.getPositionId(),
                cycleStart, cycleEnd);

        // A. 统计合格考勤天数 (正常、迟到、早退、迟到且早退)
        long qualifiedAttendanceDays = attendances.stream()
                .filter(a -> a.getAttendanceStatus() != null &&
                        (a.getAttendanceStatus().equals("正常") ||
                                a.getAttendanceStatus().equals("迟到") ||
                                a.getAttendanceStatus().equals("早退") ||
                                a.getAttendanceStatus().equals("迟到且早退")))
                .count();

        // B. 统计合格请假天数 (包含在周期内的病假、带薪假)
        long qualifiedLeaveDays = 0;
        for (LeaveRequest l : leaves) {
            String type = l.getLeaveType();
            if (type != null && (type.equals("病假") || type.equals("带薪假（年假、婚假、丧假、产假、陪产假、育儿假）"))) {
                // 计算该请假单在当前计薪周期间隔内的实际天数
                LocalDate overlapStart = l.getStartDate().isBefore(cycleStart) ? cycleStart : l.getStartDate();
                LocalDate overlapEnd = l.getEndDate().isAfter(cycleEnd) ? cycleEnd : l.getEndDate();
                if (!overlapStart.isAfter(overlapEnd)) {
                    qualifiedLeaveDays += ChronoUnit.DAYS.between(overlapStart, overlapEnd) + 1;
                }
            }
        }

        BigDecimal effectiveDays = new BigDecimal(qualifiedAttendanceDays + qualifiedLeaveDays);

        // 4. 计算各项金额
        PaySlip slip = new PaySlip();
        slip.setOnDutyWorkerId(worker.getOnDutyWorkerId());
        slip.setCycleStart(cycleStart);
        slip.setCycleEnd(cycleEnd);
        slip.setDeadlineDate(calculateDeadlineDate(worker.getOnDutyWorkerId(), cycleEnd));

        // 5. 基础工资计算
        BigDecimal basePay = BigDecimal.ZERO;
        BigDecimal unitPrice = config.getBaseRate() != null ? config.getBaseRate() : BigDecimal.ZERO;

        if (config.getBillingMethod() != null && config.getBillingMethod() == 1) {
            // 按小时计费
            BigDecimal dailyHours = calculateStandardDailyHours(position);
            basePay = effectiveDays.multiply(dailyHours).multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
        } else {
            // 默认按天计费 (Method=2)
            basePay = effectiveDays.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
        }
        slip.setBasePay(basePay);

        // 考勤扣款
        // 4. 考勤扣款核算
        BigDecimal lateDeduction = BigDecimal.ZERO;
        BigDecimal earlyDeduction = BigDecimal.ZERO;
        BigDecimal absentDeduction = BigDecimal.ZERO;
        BigDecimal absenceDeduction = BigDecimal.ZERO;

        for (Attendance a : attendances) {
            String status = a.getAttendanceStatus();
            if (status == null)
                continue;

            // 迟到计数 (包含“迟到且早退”)
            if (status.contains("迟到")) {
                lateDeduction = lateDeduction
                        .add(config.getLatePenalty() != null ? config.getLatePenalty() : BigDecimal.ZERO);
            }
            // 早退计数 (包含“迟到且早退”)
            if (status.contains("早退")) {
                earlyDeduction = earlyDeduction
                        .add(config.getEarlyPenalty() != null ? config.getEarlyPenalty() : BigDecimal.ZERO);
            }
            // 旷工计次
            if (status.contains("旷工")) {
                absentDeduction = absentDeduction
                        .add(config.getAbsentPenalty() != null ? config.getAbsentPenalty() : BigDecimal.ZERO);
            }
            // 缺勤计次
            if (status.contains("缺勤")) {
                absenceDeduction = absenceDeduction
                        .add(config.getAbsencePenalty() != null ? config.getAbsencePenalty() : BigDecimal.ZERO);
            }
        }
        slip.setLateDeduction(lateDeduction);
        slip.setEarlyLeaveDeduction(earlyDeduction);
        slip.setAbsentDeduction(absentDeduction);
        slip.setAbsenceDeduction(absenceDeduction);

        // 请假扣款 (病假/事假)
        BigDecimal leaveDeductionTotal = BigDecimal.ZERO;
        // 确定日薪基数：按天计费即为baseRate；按小时计费则为 每日标准工时 * baseRate
        BigDecimal dailySalaryBase = config.getBaseRate();
        if (config.getBillingMethod() != null && config.getBillingMethod() == 1) {
            BigDecimal dailyHours = calculateStandardDailyHours(position);
            dailySalaryBase = dailySalaryBase.multiply(dailyHours);
        }

        for (LeaveRequest l : leaves) {
            if ("病假".equals(l.getLeaveType())) {
                BigDecimal rate = config.getSickLeaveRate() != null ? config.getSickLeaveRate() : BigDecimal.ZERO;
                leaveDeductionTotal = leaveDeductionTotal.add(dailySalaryBase.multiply(rate));
            } else if ("事假".equals(l.getLeaveType())) {
                BigDecimal rate = config.getPersonalLeaveRate() != null ? config.getPersonalLeaveRate()
                        : BigDecimal.ZERO;
                leaveDeductionTotal = leaveDeductionTotal.add(dailySalaryBase.multiply(rate));
            }
        }
        slip.setLeaveDeduction(leaveDeductionTotal.setScale(2, RoundingMode.HALF_UP));

        // 5. 加班费计算
        BigDecimal totalOvertimePay = BigDecimal.ZERO;
        if (config.getHasOvertimePay() != null && config.getHasOvertimePay() == 1) {
            for (Attendance a : attendances) {
                totalOvertimePay = totalOvertimePay.add(calculateDailyOvertimePay(a, position, config));
            }
        }
        slip.setOvertimePay(totalOvertimePay.setScale(2, RoundingMode.HALF_UP));

        // 6. 绩效、补贴与提成 (Income)
        BigDecimal performancePay = config.getPerformanceBonus() != null ? config.getPerformanceBonus()
                : BigDecimal.ZERO;
        BigDecimal fixedBonus = config.getBonus() != null ? config.getBonus() : BigDecimal.ZERO;
        BigDecimal allowance = config.getAllowance() != null ? config.getAllowance() : BigDecimal.ZERO;
        // 提成目前暂未关联具体业务，取配置的默认提成（如有）
        BigDecimal commission = config.getCommission() != null ? config.getCommission() : BigDecimal.ZERO;

        slip.setBonusPay(performancePay.add(fixedBonus).add(commission));
        slip.setAllowance(allowance);

        // 6. 应发工资总额 (Gross Pay) - 计算社保基数的前置条件
        BigDecimal grossPay = (basePay != null ? basePay : BigDecimal.ZERO)
                .add(slip.getBonusPay() != null ? slip.getBonusPay() : BigDecimal.ZERO)
                .add(slip.getOvertimePay() != null ? slip.getOvertimePay() : BigDecimal.ZERO)
                .add(slip.getAllowance() != null ? slip.getAllowance() : BigDecimal.ZERO);
        slip.setGrossPay(grossPay);

        // 7. 社保基数确定逻辑
        BigDecimal socialBase = worker.getSocialSecurityBase();
        boolean isFirstMonth = (socialBase == null);
        if (isFirstMonth) {
            // 首月核定：使用应发工资总额
            socialBase = grossPay;
        }

        // 兜底：确保 socialBase 绝不为 null，防止后续 compareTo 抛出 NPE
        if (socialBase == null) {
            socialBase = BigDecimal.ZERO;
        }

        // 统一应用上下限 (无论是首月自动核定还是手动预设，均需符合岗位配置边界)
        BigDecimal lower = config.getSocialSecurityBaseLower() != null ? config.getSocialSecurityBaseLower()
                : BigDecimal.ZERO;
        BigDecimal upper = config.getSocialSecurityBaseUpper() != null ? config.getSocialSecurityBaseUpper()
                : new BigDecimal("999999");

        if (socialBase.compareTo(lower) < 0)
            socialBase = lower;
        if (socialBase.compareTo(upper) > 0)
            socialBase = upper;

        // 若基数发生变化（如首次核定，或手动值非法被强制纠偏），则同步持久化回员工表
        BigDecimal originalBase = worker.getSocialSecurityBase();
        if (originalBase == null || originalBase.compareTo(socialBase) != 0) {
            worker.setSocialSecurityBase(socialBase);
            workerMapper.update(worker);
        }

        // 8. 五险一金 (Deductions) - 使用核定后的社保基数
        slip.setPensionDeduction(
                socialBase.multiply(config.getPensionRate() != null ? config.getPensionRate() : BigDecimal.ZERO)
                        .setScale(2, RoundingMode.HALF_UP));
        slip.setMedicalDeduction(
                socialBase.multiply(config.getMedicalRate() != null ? config.getMedicalRate() : BigDecimal.ZERO)
                        .setScale(2, RoundingMode.HALF_UP));
        slip.setUnemploymentDeduction(
                socialBase
                        .multiply(config.getUnemploymentRate() != null ? config.getUnemploymentRate() : BigDecimal.ZERO)
                        .setScale(2, RoundingMode.HALF_UP));
        slip.setInjuryDeduction(
                socialBase.multiply(config.getInjuryRate() != null ? config.getInjuryRate() : BigDecimal.ZERO).setScale(
                        2,
                        RoundingMode.HALF_UP));
        slip.setPfDeduction(
                socialBase.multiply(config.getHousingFundRate() != null ? config.getHousingFundRate() : BigDecimal.ZERO)
                        .setScale(2, RoundingMode.HALF_UP));

        // 合计
        BigDecimal insuranceDeduction = slip.getPensionDeduction().add(slip.getMedicalDeduction())
                .add(slip.getUnemploymentDeduction()).add(slip.getInjuryDeduction())
                .add(slip.getPfDeduction());

        BigDecimal totalDeduction = insuranceDeduction.add(slip.getLateDeduction())
                .add(slip.getEarlyLeaveDeduction()).add(slip.getAbsentDeduction())
                .add(slip.getAbsenceDeduction())
                .add(slip.getLeaveDeduction());

        // 9. 个人所得税 (PIT) 核算 - 累计预扣预缴法
        // 考勤扣款合计 (这些不应计入纳税基数)
        BigDecimal attendanceDeductionTotal = slip.getLateDeduction()
                .add(slip.getEarlyLeaveDeduction())
                .add(slip.getAbsentDeduction())
                .add(slip.getAbsenceDeduction())
                .add(slip.getLeaveDeduction());

        // 获取本年度该员工的所有历史薪资记录，用于累计核算
        int currentYear = slip.getCycleEnd().getYear();
        int currentMonthCount = slip.getCycleEnd().getMonthValue();
        List<PaySlip> yearSlips = paySlipMapper.findByUserId(worker.getUserId(), null, null, null, null);

        BigDecimal cumulativeGross = grossPay;
        BigDecimal cumulativeAttendanceDeduction = attendanceDeductionTotal;
        BigDecimal cumulativeInsurance = insuranceDeduction;
        BigDecimal cumulativeTaxPaid = BigDecimal.ZERO;

        if (yearSlips != null) {
            for (PaySlip history : yearSlips) {
                // 仅统计本年度且在当前周期结束日期之前的已生成记录
                if (history.getCycleEnd().getYear() == currentYear
                        && history.getCycleEnd().isBefore(slip.getCycleEnd())) {
                    cumulativeGross = cumulativeGross
                            .add(history.getGrossPay() != null ? history.getGrossPay() : BigDecimal.ZERO);

                    // 汇总历史记录的考勤扣款
                    BigDecimal histAttendance = (history.getLateDeduction() != null ? history.getLateDeduction()
                            : BigDecimal.ZERO)
                            .add(history.getEarlyLeaveDeduction() != null ? history.getEarlyLeaveDeduction()
                                    : BigDecimal.ZERO)
                            .add(history.getAbsentDeduction() != null ? history.getAbsentDeduction() : BigDecimal.ZERO)
                            .add(history.getAbsenceDeduction() != null ? history.getAbsenceDeduction()
                                    : BigDecimal.ZERO)
                            .add(history.getLeaveDeduction() != null ? history.getLeaveDeduction() : BigDecimal.ZERO);

                    cumulativeAttendanceDeduction = cumulativeAttendanceDeduction.add(histAttendance);

                    // 汇总历史记录的个人五险一金
                    BigDecimal histInsurance = (history.getPensionDeduction() != null ? history.getPensionDeduction()
                            : BigDecimal.ZERO)
                            .add(history.getMedicalDeduction() != null ? history.getMedicalDeduction()
                                    : BigDecimal.ZERO)
                            .add(history.getUnemploymentDeduction() != null ? history.getUnemploymentDeduction()
                                    : BigDecimal.ZERO)
                            .add(history.getInjuryDeduction() != null ? history.getInjuryDeduction() : BigDecimal.ZERO)
                            .add(history.getPfDeduction() != null ? history.getPfDeduction() : BigDecimal.ZERO);

                    cumulativeInsurance = cumulativeInsurance.add(histInsurance);
                    cumulativeTaxPaid = cumulativeTaxPaid
                            .add(history.getTaxAmount() != null ? history.getTaxAmount() : BigDecimal.ZERO);
                }
            }
        }

        // 累计减除费用 (5000 * 累计发薪月数)
        BigDecimal cumulativeStandardDeduction = new BigDecimal("5000").multiply(new BigDecimal(currentMonthCount));

        // 累计预扣缴所得额 = 累计收入 - 累计考勤缴罚 - 累计减除费用 - 累计自缴五险一金
        BigDecimal cumulativeTaxableIncome = cumulativeGross
                .subtract(cumulativeAttendanceDeduction)
                .subtract(cumulativeStandardDeduction)
                .subtract(cumulativeInsurance);

        if (cumulativeTaxableIncome.compareTo(BigDecimal.ZERO) < 0) {
            cumulativeTaxableIncome = BigDecimal.ZERO;
        }

        // 计算截止本月的年度应缴预扣税总额
        BigDecimal totalTaxDueYTD = calculatePersonalIncomeTax(cumulativeTaxableIncome);

        // 本月预扣额 = 截止本月总额 - 以前月份已预缴额
        BigDecimal currentMonthTax = totalTaxDueYTD.subtract(cumulativeTaxPaid);
        if (currentMonthTax.compareTo(BigDecimal.ZERO) < 0) {
            currentMonthTax = BigDecimal.ZERO;
        }
        slip.setTaxAmount(currentMonthTax);

        // 最终汇总
        slip.setTotalDeduction(totalDeduction.add(currentMonthTax));

        BigDecimal netPay = grossPay.subtract(slip.getTotalDeduction());
        // 兜底逻辑：实发工资最低为 0
        if (netPay.compareTo(BigDecimal.ZERO) < 0) {
            netPay = BigDecimal.ZERO;
        }
        slip.setNetPay(netPay);
        slip.setActualPaymentDate(null); // 初始为空，表示未支付
        slip.setConfirmStatus(1);

        paySlipMapper.insert(slip);
    }

    /**
     * 手动/定时触发：根据上一年度平均工资调整员工社保基数
     */
    @Transactional
    public void adjustAnnualSocialSecurityBase(Long onDutyWorkerId, int lastYear) {
        OnDutyWorker worker = workerMapper.findById(onDutyWorkerId);
        if (worker == null)
            return;

        Position position = positionMapper.findById(worker.getPositionId());
        if (position == null || position.getSalaryConfigId() == null)
            return;
        SalaryConfig config = salaryConfigMapper.findById(position.getSalaryConfigId());
        if (config == null)
            return;

        // 获取该员工去年的所有薪资单
        List<PaySlip> lastYearSlips = paySlipMapper.findByUserId(worker.getUserId(), null, null, null, null); // 简化逻辑，实际应过滤
                                                                                                              // workerId
                                                                                                              // 和年份

        BigDecimal totalGross = BigDecimal.ZERO;
        int count = 0;
        for (PaySlip s : lastYearSlips) {
            if (s.getCycleStart().getYear() == lastYear) {
                totalGross = totalGross.add(s.getGrossPay());
                count++;
            }
        }

        if (count > 0) {
            BigDecimal avgGross = totalGross.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP);

            // 应用上下限
            BigDecimal lower = config.getSocialSecurityBaseLower() != null ? config.getSocialSecurityBaseLower()
                    : BigDecimal.ZERO;
            BigDecimal upper = config.getSocialSecurityBaseUpper() != null ? config.getSocialSecurityBaseUpper()
                    : new BigDecimal("999999");

            if (avgGross.compareTo(lower) < 0)
                avgGross = lower;
            if (avgGross.compareTo(upper) > 0)
                avgGross = upper;

            worker.setSocialSecurityBase(avgGross);
            workerMapper.update(worker);
        }
    }

    private LocalDate calculateCycleEnd(LocalDate start, String cycle) {
        switch (cycle) {
            case "日结":
                return start;
            case "周结":
                return start.plusDays(6);
            case "15日结":
                return start.plusDays(14);
            case "月结":
                return start.plusMonths(1).minusDays(start.getDayOfMonth()).plusDays(start.getDayOfMonth() - 1); // 简化为自然月
            default:
                return start.plusMonths(1).minusDays(1);
        }
    }

    public LocalDate calculateDeadlineDate(Long onDutyWorkerId, LocalDate cycleEnd) {
        if (onDutyWorkerId == null || cycleEnd == null)
            return null;

        OnDutyWorker worker = workerMapper.findById(onDutyWorkerId);
        if (worker == null)
            return cycleEnd.plusDays(15); // 兜底逻辑

        Position position = positionMapper.findById(worker.getPositionId());
        if (position == null || position.getSalaryConfigId() == null)
            return cycleEnd.plusDays(15);

        SalaryConfig config = salaryConfigMapper.findById(position.getSalaryConfigId());
        if (config == null)
            return cycleEnd.plusDays(15);

        if ("月结".equals(config.getPayCycle())) {
            return cycleEnd.plusMonths(1);
        } else {
            return cycleEnd.plusDays(15);
        }
    }

    public Map<String, LocalDate> getSuggestedCycle(Long onDutyWorkerId) {
        Map<String, LocalDate> result = new HashMap<>();
        OnDutyWorker worker = workerMapper.findById(onDutyWorkerId);
        if (worker == null)
            return result;

        // 1. 确定开始日期
        PaySlip lastSlip = paySlipMapper.findLatestByWorkerId(onDutyWorkerId);
        LocalDate start;
        if (lastSlip != null) {
            start = lastSlip.getCycleEnd().plusDays(1);
        } else {
            start = worker.getHireDate() != null ? worker.getHireDate() : LocalDate.now();
        }
        result.put("cycleStart", start);

        // 2. 查找配置并确定结束日期
        Position position = positionMapper.findById(worker.getPositionId());
        if (position != null && position.getSalaryConfigId() != null) {
            SalaryConfig config = salaryConfigMapper.findById(position.getSalaryConfigId());
            if (config != null) {
                result.put("cycleEnd", calculateCycleEnd(start, config.getPayCycle()));
            }
        }

        if (!result.containsKey("cycleEnd")) {
            result.put("cycleEnd", start);
        }

        return result;
    }

    /**
     * 计算岗位的每日标准工作小时数
     */
    private BigDecimal calculateStandardDailyHours(Position position) {
        if (position.getCheckInTime() == null || position.getCheckOutTime() == null) {
            return new BigDecimal("8"); // 兜底 8 小时
        }
        long minutes = java.time.Duration.between(position.getCheckInTime(), position.getCheckOutTime()).toMinutes();
        if (minutes < 0) {
            // 处理跨天情况 (如果后续有夜班需求)
            minutes += 24 * 60;
        }
        return new BigDecimal(minutes).divide(new BigDecimal("60"), 2, RoundingMode.HALF_UP);
    }

    /**
     * 计算单日加班费
     */
    private BigDecimal calculateDailyOvertimePay(Attendance a, Position p, SalaryConfig config) {
        if (a.getActualCheckIn() == null || a.getActualCheckOut() == null) {
            return BigDecimal.ZERO;
        }

        // 1. 计算总加班分钟数
        // 签到早于应签到时间 -> 加班
        long beforeMins = java.time.Duration.between(a.getActualCheckIn(), p.getCheckInTime()).toMinutes();
        // 签退晚于应签退时间 -> 加班
        long afterMins = java.time.Duration.between(p.getCheckOutTime(), a.getActualCheckOut()).toMinutes();

        long totalMins = Math.max(0, beforeMins) + Math.max(0, afterMins);
        if (totalMins <= 0)
            return BigDecimal.ZERO;

        // 2. 确定加班倍率
        BigDecimal multiplier = config.getOvertimeWeekdayMultiplier() != null ? config.getOvertimeWeekdayMultiplier()
                : new BigDecimal("1.5");
        HolidayCalendar holiday = holidayCalendarMapper.findByDate(a.getAttendanceDate());

        if (holiday != null) {
            if ("法定节假日".equals(holiday.getDayType())) {
                multiplier = config.getOvertimeHolidayMultiplier() != null ? config.getOvertimeHolidayMultiplier()
                        : new BigDecimal("3.0");
            } else if ("休息日".equals(holiday.getDayType())) {
                multiplier = config.getOvertimeWeekendMultiplier() != null ? config.getOvertimeWeekendMultiplier()
                        : new BigDecimal("2.0");
            }
        } else {
            // 无节日配置时，判断是否为周末
            int dayOfWeek = a.getAttendanceDate().getDayOfWeek().getValue();
            if (dayOfWeek == 6 || dayOfWeek == 7) {
                multiplier = config.getOvertimeWeekendMultiplier() != null ? config.getOvertimeWeekendMultiplier()
                        : new BigDecimal("2.0");
            }
        }

        // 3. 应用计算模式
        int mode = config.getOvertimeCalcMode() != null ? config.getOvertimeCalcMode() : 3;
        int threshold = config.getOvertimeThresholdMin() != null ? config.getOvertimeThresholdMin() : 0;
        BigDecimal roundingUnit = config.getOvertimeRoundingUnit() != null ? config.getOvertimeRoundingUnit()
                : BigDecimal.ONE;

        double finalHours = 0;
        switch (mode) {
            case 1: // 起算阈值模式 (满 threshold 分钟才计费)
                if (totalMins >= threshold) {
                    finalHours = totalMins / 60.0;
                }
                break;
            case 2: // 取整计算模式
                // 强制按单位向下取整。例：单位0.5，加班75分(1.25h) -> 1.0h
                double rawHours = totalMins / 60.0;
                double unit = roundingUnit.doubleValue();
                finalHours = Math.floor(rawHours / unit) * unit;
                break;
            case 3: // 分钟折算模式 (默认)
            default:
                finalHours = totalMins / 60.0;
                break;
        }

        if (finalHours <= 0)
            return BigDecimal.ZERO;

        // 4. 确定加班时薪基数
        BigDecimal hourlyBase = config.getBaseRate() != null ? config.getBaseRate() : BigDecimal.ZERO;
        if (config.getBillingMethod() != null && config.getBillingMethod() == 2) {
            // 按日计费：加班时薪 = 日薪 / 每日标准工时
            BigDecimal dailyHours = calculateStandardDailyHours(p);
            if (dailyHours.compareTo(BigDecimal.ZERO) > 0) {
                hourlyBase = hourlyBase.divide(dailyHours, 4, RoundingMode.HALF_UP);
            }
        }

        // 加班费 = 加班时长(时) * 加班时薪基数 * 倍率
        return new BigDecimal(finalHours)
                .multiply(hourlyBase)
                .multiply(multiplier);
    }

    /**
     * 年度累计超额累进个税换算算法 (基于年度累计应纳税所得额)
     */
    private BigDecimal calculatePersonalIncomeTax(BigDecimal taxableIncome) {
        if (taxableIncome.compareTo(BigDecimal.ZERO) <= 0)
            return BigDecimal.ZERO;

        BigDecimal tax;
        // 年度级数阈值
        BigDecimal b1 = new BigDecimal("36000");
        BigDecimal b2 = new BigDecimal("144000");
        BigDecimal b3 = new BigDecimal("300000");
        BigDecimal b4 = new BigDecimal("420000");
        BigDecimal b5 = new BigDecimal("660000");
        BigDecimal b6 = new BigDecimal("960000");

        if (taxableIncome.compareTo(b1) <= 0) {
            tax = taxableIncome.multiply(new BigDecimal("0.03"));
        } else if (taxableIncome.compareTo(b2) <= 0) {
            tax = taxableIncome.multiply(new BigDecimal("0.10")).subtract(new BigDecimal("2520"));
        } else if (taxableIncome.compareTo(b3) <= 0) {
            tax = taxableIncome.multiply(new BigDecimal("0.20")).subtract(new BigDecimal("16920"));
        } else if (taxableIncome.compareTo(b4) <= 0) {
            tax = taxableIncome.multiply(new BigDecimal("0.25")).subtract(new BigDecimal("31920"));
        } else if (taxableIncome.compareTo(b5) <= 0) {
            tax = taxableIncome.multiply(new BigDecimal("0.30")).subtract(new BigDecimal("52920"));
        } else if (taxableIncome.compareTo(b6) <= 0) {
            tax = taxableIncome.multiply(new BigDecimal("0.35")).subtract(new BigDecimal("85920"));
        } else {
            tax = taxableIncome.multiply(new BigDecimal("0.45")).subtract(new BigDecimal("181920"));
        }

        return tax.setScale(2, RoundingMode.HALF_UP);
    }
}
