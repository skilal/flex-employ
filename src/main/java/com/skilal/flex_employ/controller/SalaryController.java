package com.skilal.flex_employ.controller;

import com.skilal.flex_employ.common.Result;
import com.skilal.flex_employ.entity.PaySlip;
import com.skilal.flex_employ.mapper.PaySlipMapper;
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
    private JwtUtil jwtUtil;

    @GetMapping
    public Result<List<PaySlip>> getSalaries(@RequestParam(required = false) String paymentStatus) {
        List<PaySlip> salaries = paySlipMapper.findAll(paymentStatus);
        return Result.success(salaries);
    }

    @GetMapping("/my")
    public Result<List<PaySlip>> getMySalaries(@RequestHeader("Authorization") String token) {
        token = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        List<PaySlip> salaries = paySlipMapper.findByUserId(userId);
        return Result.success(salaries);
    }

    @PostMapping
    public Result<String> createSalary(@RequestBody PaySlip paySlip) {
        int result = paySlipMapper.insert(paySlip);
        if (result > 0) {
            return Result.success("创建成功");
        }
        return Result.error("创建失败");
    }

    @PutMapping("/{id}")
    public Result<String> updateSalary(@PathVariable Long id, @RequestBody PaySlip paySlip) {
        paySlip.setPayRecordId(id);
        int result = paySlipMapper.update(paySlip);
        if (result > 0) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
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
