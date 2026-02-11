package com.skilal.flex_employ.controller;

import com.skilal.flex_employ.common.Result;
import com.skilal.flex_employ.entity.SalaryConfig;
import com.skilal.flex_employ.mapper.SalaryConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/salary-configs")
public class SalaryConfigController {

    @Autowired
    private SalaryConfigMapper salaryConfigMapper;

    @GetMapping
    public Result<List<SalaryConfig>> getAll(
            @RequestParam(required = false) String configName,
            @RequestParam(required = false) String payCycle,
            @RequestParam(required = false) Integer billingMethod) {
        return Result.success(salaryConfigMapper.findAll(configName, payCycle, billingMethod));
    }

    @GetMapping("/{id}")
    public Result<SalaryConfig> getById(@PathVariable Long id) {
        return Result.success(salaryConfigMapper.findById(id));
    }

    @PostMapping
    public Result<String> create(@RequestBody SalaryConfig config) {
        // 1. 检查重名
        if (salaryConfigMapper.countByName(config.getConfigName(), null) > 0) {
            return Result.error("创建失败：薪资配置名称已存在");
        }
        salaryConfigMapper.insert(config);
        return Result.success("创建成功");
    }

    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody SalaryConfig config) {
        // 1. 检查重名
        if (salaryConfigMapper.countByName(config.getConfigName(), id) > 0) {
            return Result.error("更新失败：薪资配置名称已冲突");
        }
        config.setConfigId(id);
        salaryConfigMapper.update(config);
        return Result.success("更新成功");
    }

    @Autowired
    private com.skilal.flex_employ.mapper.PositionMapper positionMapper;

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        // 1. 检查关联
        if (positionMapper.countBySalaryConfigId(id) > 0) {
            return Result.error("删除失败：该薪资配置已被岗位引用，无法删除");
        }
        salaryConfigMapper.delete(id);
        return Result.success("删除成功");
    }
}
