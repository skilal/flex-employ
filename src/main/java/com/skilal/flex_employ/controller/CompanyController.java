package com.skilal.flex_employ.controller;

import com.skilal.flex_employ.common.Result;
import com.skilal.flex_employ.entity.Company;
import com.skilal.flex_employ.mapper.CompanyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    @Autowired
    private CompanyMapper companyMapper;

    @GetMapping
    public Result<List<Company>> getCompanies(@RequestParam(required = false) String companyName,
            @RequestParam(required = false) Integer companyStatus) {
        List<Company> companies = companyMapper.findAll(companyName, companyStatus);
        return Result.success(companies);
    }

    @GetMapping("/{id}")
    public Result<Company> getCompanyById(@PathVariable Long id) {
        Company company = companyMapper.findById(id);
        if (company != null) {
            return Result.success(company);
        }
        return Result.error("公司不存在");
    }

    @PostMapping
    public Result<String> createCompany(@RequestBody Company company) {
        int rows = companyMapper.insert(company);
        if (rows > 0) {
            return Result.success("创建成功");
        }
        return Result.error("创建失败");
    }

    @PutMapping("/{id}")
    public Result<String> updateCompany(@PathVariable Long id, @RequestBody Company company) {
        company.setCompanyId(id);
        int rows = companyMapper.update(company);
        if (rows > 0) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteCompany(@PathVariable Long id) {
        int rows = companyMapper.delete(id);
        if (rows > 0) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}
