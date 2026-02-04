package com.skilal.flex_employ.controller;

import com.skilal.flex_employ.common.Result;
import com.skilal.flex_employ.entity.PaySlip;
import com.skilal.flex_employ.mapper.PaySlipMapper;
import com.skilal.flex_employ.service.SalaryService;
import com.skilal.flex_employ.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public Result<List<PaySlip>> getSalaries() {
        List<PaySlip> salaries = paySlipMapper.findAll();
        return Result.success(salaries);
    }

    @GetMapping("/{id}")
    public Result<PaySlip> getById(@PathVariable Long id) {
        return Result.success(paySlipMapper.findById(id));
    }

    @PostMapping
    public Result<String> createSalary(@RequestBody PaySlip paySlip) {
        // 自动计算最晚支付日期（如果未设置）
        if (paySlip.getDeadlineDate() == null && paySlip.getCycleEnd() != null) {
            // 默认使用周期结束日期 + 15天作为最晚支付日期
            paySlip.setDeadlineDate(paySlip.getCycleEnd().plusDays(15));
        }

        paySlipMapper.insert(paySlip);
        return Result.success("薪资记录补录成功");
    }

    @PutMapping("/{id}")
    public Result<String> updateSalary(@PathVariable Long id, @RequestBody PaySlip paySlip) {
        paySlip.setPayRecordId(id);

        // 自动计算最晚支付日期（如果未设置）
        if (paySlip.getDeadlineDate() == null && paySlip.getCycleEnd() != null) {
            paySlip.setDeadlineDate(paySlip.getCycleEnd().plusDays(15));
        }

        paySlipMapper.update(paySlip);
        return Result.success("更新成功");
    }

    @GetMapping("/my")
    public Result<List<PaySlip>> getMySalaries(@RequestHeader("Authorization") String token) {
        token = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        List<PaySlip> salaries = paySlipMapper.findByUserId(userId);
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
