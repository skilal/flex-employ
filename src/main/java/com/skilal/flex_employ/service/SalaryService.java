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
import java.util.List;

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

    // 每天凌晨2点自动生成薪资记录
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void autoGeneratePaySlips() {
        // 查找所有在岗员工
        List<OnDutyWorker> activeWorkers = workerMapper.findAllActive();
        for (OnDutyWorker worker : activeWorkers) {
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
        // 如果周期结束日还没到，不生成
        if (cycleEnd.isAfter(LocalDate.now()))
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
        int bufferDays = config.getPayCycle().equals("月结") ? 30 : 15;
        slip.setDeadlineDate(cycleEnd.plusDays(bufferDays));

        // 基础工资计算 (简化逻辑：按天/按小时 * 有效时间)
        BigDecimal basePay = config.getBaseRate(); // 这里应根据考勤实际天数/小时数计算，目前取模版值
        slip.setBasePay(basePay); // 暂定为全额

        // 考勤扣款
        BigDecimal lateDeduction = BigDecimal.ZERO;
        BigDecimal earlyDeduction = BigDecimal.ZERO;
        BigDecimal absentDeduction = BigDecimal.ZERO;
        for (Attendance a : attendances) {
            if ("迟到".equals(a.getInStatus()))
                lateDeduction = lateDeduction.add(config.getLatePenalty());
            if ("早退".equals(a.getOutStatus()))
                earlyDeduction = earlyDeduction.add(config.getEarlyPenalty());
            if ("缺勤".equals(a.getInStatus()))
                absentDeduction = absentDeduction.add(config.getAbsentPenalty());
        }
        slip.setLateDeduction(lateDeduction);
        slip.setEarlyLeaveDeduction(earlyDeduction);
        slip.setAbsentDeduction(absentDeduction);

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

        // 五险一金与税 (目前模拟固定值或简单算法)
        slip.setPensionDeduction(basePay.multiply(new BigDecimal("0.08")).setScale(2, RoundingMode.HALF_UP));
        slip.setMedicalDeduction(basePay.multiply(new BigDecimal("0.02")).setScale(2, RoundingMode.HALF_UP));
        slip.setPfDeduction(basePay.multiply(new BigDecimal("0.07")).setScale(2, RoundingMode.HALF_UP));

        // 合计
        BigDecimal totalDeduction = slip.getPensionDeduction().add(slip.getMedicalDeduction())
                .add(slip.getPfDeduction()).add(slip.getLateDeduction())
                .add(slip.getEarlyLeaveDeduction()).add(slip.getAbsentDeduction())
                .add(slip.getLeaveDeduction());

        slip.setGrossPay(basePay); // 这里应该包含加班费等
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
}
