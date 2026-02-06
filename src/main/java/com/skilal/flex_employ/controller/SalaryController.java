package com.skilal.flex_employ.controller;

import com.skilal.flex_employ.common.Result;
import com.skilal.flex_employ.entity.PaySlip;
import com.skilal.flex_employ.mapper.PaySlipMapper;
import com.skilal.flex_employ.service.SalaryService;
import com.skilal.flex_employ.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/salaries")
public class SalaryController {

    @Autowired
    private PaySlipMapper paySlipMapper;

    @Autowired
    private SalaryService salaryService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public Result<List<PaySlip>> getSalaries(
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String positionName,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        List<PaySlip> salaries = paySlipMapper.findAll(userName, positionName, paymentStatus, startDate, endDate);
        return Result.success(salaries);
    }

    @GetMapping("/suggest-cycle")
    public Result<Map<String, LocalDate>> getSuggestedCycle(@RequestParam Long onDutyWorkerId) {
        return Result.success(salaryService.getSuggestedCycle(onDutyWorkerId));
    }

    @GetMapping("/deadline")
    public Result<LocalDate> getDeadline(@RequestParam Long onDutyWorkerId, @RequestParam String cycleEnd) {
        return Result.success(salaryService.calculateDeadlineDate(onDutyWorkerId, LocalDate.parse(cycleEnd)));
    }

    @GetMapping("/{id}")
    public Result<PaySlip> getById(@PathVariable Long id) {
        return Result.success(paySlipMapper.findById(id));
    }

    @PostMapping
    public Result<String> createSalary(@RequestBody PaySlip paySlip) {
        // 自动计算最晚支付日期
        if (paySlip.getCycleEnd() != null) {
            paySlip.setDeadlineDate(
                    salaryService.calculateDeadlineDate(paySlip.getOnDutyWorkerId(), paySlip.getCycleEnd()));
        }

        paySlipMapper.insert(paySlip);
        return Result.success("薪资记录补录成功");
    }

    @PutMapping("/{id}")
    public Result<String> updateSalary(@PathVariable Long id, @RequestBody PaySlip paySlip) {
        paySlip.setPayRecordId(id);

        // 自动计算最晚支付日期
        if (paySlip.getCycleEnd() != null) {
            paySlip.setDeadlineDate(
                    salaryService.calculateDeadlineDate(paySlip.getOnDutyWorkerId(), paySlip.getCycleEnd()));
        }

        paySlipMapper.update(paySlip);
        return Result.success("更新成功");
    }

    @GetMapping("/my")
    public Result<List<PaySlip>> getMySalaries(
            @RequestParam(required = false) String positionName,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestHeader("Authorization") String token) {
        token = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        List<PaySlip> salaries = paySlipMapper.findByUserId(userId, positionName, paymentStatus, startDate, endDate);
        return Result.success(salaries);
    }

    // 手动触发全员薪资自动生成引擎
    @PostMapping("/generate")
    public Result<String> triggerGenerate() {
        salaryService.autoGeneratePaySlips();
        return Result.success("薪资自动结算任务已启动");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteSalary(@PathVariable Long id) {
        int result = paySlipMapper.delete(id);
        if (result > 0) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}
