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
import java.time.LocalDateTime;
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

    // 每天凌晨2点自动生成薪资记录
    @Scheduled(cron = "0 49 15 * * ?")
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

        // 3. 统计考勤与请假
        List<Attendance> attendances = attendanceMapper.findByWorkerAndRange(worker.getOnDutyWorkerId(), cycleStart,
                cycleEnd);
        List<LeaveRequest> leaves = leaveRequestMapper.findApprovedByRange(worker.getUserId(), worker.getPositionId(),
                cycleStart, cycleEnd);

        // 4. 计算各项金额
        PaySlip slip = new PaySlip();
        slip.setOnDutyWorkerId(worker.getOnDutyWorkerId());
        slip.setCycleStart(cycleStart);
        slip.setCycleEnd(cycleEnd);

        // 最晚发放日判定
        slip.setDeadlineDate(calculateDeadlineDate(worker.getOnDutyWorkerId(), cycleEnd));

        // 基础工资计算 (简化逻辑：按天/按小时 * 有效时间)
        BigDecimal basePay = config.getBaseRate(); // 这里应根据考勤实际天数/小时数计算，目前取模版值
        slip.setBasePay(basePay); // 暂定为全额

        // 考勤扣款
        BigDecimal lateDeduction = BigDecimal.ZERO;
        BigDecimal earlyDeduction = BigDecimal.ZERO;
        BigDecimal absentDeduction = BigDecimal.ZERO;
        BigDecimal absenceDeduction = BigDecimal.ZERO;
        for (Attendance a : attendances) {
            String status = a.getAttendanceStatus();
            if (status == null)
                continue;

            if (status.contains("迟到"))
                lateDeduction = lateDeduction.add(config.getLatePenalty());
            if (status.contains("早退"))
                earlyDeduction = earlyDeduction.add(config.getEarlyPenalty());
            if (status.contains("旷工"))
                absentDeduction = absentDeduction.add(config.getAbsentPenalty());
            if (status.contains("缺勤"))
                absenceDeduction = absenceDeduction
                        .add(config.getAbsencePenalty() != null ? config.getAbsencePenalty() : BigDecimal.ZERO);
        }
        slip.setLateDeduction(lateDeduction);
        slip.setEarlyLeaveDeduction(earlyDeduction);
        slip.setAbsentDeduction(absentDeduction);
        slip.setAbsenceDeduction(absenceDeduction);

        // 请假扣款
        BigDecimal leaveDeduction = BigDecimal.ZERO;
        for (LeaveRequest l : leaves) {
            long days = ChronoUnit.DAYS.between(l.getStartDate(), l.getEndDate()) + 1;
            BigDecimal dailyRaw = config.getBaseRate(); // 假设按天
            if ("病假".equals(l.getLeaveType())) {
                leaveDeduction = leaveDeduction.add(dailyRaw.multiply(config.getSickLeaveRate()));
            } else {
                leaveDeduction = leaveDeduction.add(dailyRaw.multiply(config.getPersonalLeaveRate()));
            }
        }
        slip.setLeaveDeduction(leaveDeduction);

        // 5. 绩效与奖金 (Income)
        BigDecimal performancePay = config.getPerformanceBonus() != null ? config.getPerformanceBonus()
                : BigDecimal.ZERO;
        BigDecimal fixedBonus = config.getBonus() != null ? config.getBonus() : BigDecimal.ZERO;
        // 提成目前暂未关联具体业务数据，先取 0，待后续扩展
        slip.setBonusPay(performancePay.add(fixedBonus));

        // 6. 五险一金 (Deductions) - 使用配置的费率
        slip.setPensionDeduction(
                basePay.multiply(config.getPensionRate() != null ? config.getPensionRate() : BigDecimal.ZERO)
                        .setScale(2, RoundingMode.HALF_UP));
        slip.setMedicalDeduction(
                basePay.multiply(config.getMedicalRate() != null ? config.getMedicalRate() : BigDecimal.ZERO)
                        .setScale(2, RoundingMode.HALF_UP));
        slip.setUnemploymentDeduction(
                basePay.multiply(config.getUnemploymentRate() != null ? config.getUnemploymentRate() : BigDecimal.ZERO)
                        .setScale(2, RoundingMode.HALF_UP));
        slip.setInjuryDeduction(
                basePay.multiply(config.getInjuryRate() != null ? config.getInjuryRate() : BigDecimal.ZERO).setScale(2,
                        RoundingMode.HALF_UP));
        slip.setPfDeduction(
                basePay.multiply(config.getHousingFundRate() != null ? config.getHousingFundRate() : BigDecimal.ZERO)
                        .setScale(2, RoundingMode.HALF_UP));

        // 合计
        BigDecimal insuranceDeduction = slip.getPensionDeduction().add(slip.getMedicalDeduction())
                .add(slip.getUnemploymentDeduction()).add(slip.getInjuryDeduction())
                .add(slip.getPfDeduction());

        BigDecimal totalDeduction = insuranceDeduction.add(slip.getLateDeduction())
                .add(slip.getEarlyLeaveDeduction()).add(slip.getAbsentDeduction())
                .add(slip.getLeaveDeduction());

        slip.setGrossPay(basePay.add(slip.getBonusPay())); // 包含绩效奖金
        slip.setTotalDeduction(totalDeduction);
        slip.setNetPay(slip.getGrossPay().subtract(totalDeduction));
        slip.setActualPaymentDate(null); // 初始为空，表示未支付
        slip.setConfirmStatus(1);

        paySlipMapper.insert(slip);
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
}
