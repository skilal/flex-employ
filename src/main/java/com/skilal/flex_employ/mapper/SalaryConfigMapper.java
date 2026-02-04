package com.skilal.flex_employ.mapper;

import com.skilal.flex_employ.entity.SalaryConfig;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface SalaryConfigMapper {

    @Select("SELECT * FROM salary_config ORDER BY created_at DESC")
    List<SalaryConfig> findAll();

    @Select("SELECT * FROM salary_config WHERE config_id = #{id}")
    SalaryConfig findById(Long id);

    @Insert("INSERT INTO salary_config (config_name, pay_cycle, billing_method, base_rate, overtime_rate, " +
            "late_penalty, early_penalty, absent_penalty, sick_leave_rate, personal_leave_rate) " +
            "VALUES (#{configName}, #{payCycle}, #{billingMethod}, #{baseRate}, #{overtimeRate}, " +
            "#{latePenalty}, #{earlyPenalty}, #{absentPenalty}, #{sickLeaveRate}, #{personalLeaveRate})")
    @Options(useGeneratedKeys = true, keyProperty = "configId")
    int insert(SalaryConfig config);

    @Update("UPDATE salary_config SET config_name = #{configName}, pay_cycle = #{payCycle}, " +
            "billing_method = #{billingMethod}, base_rate = #{baseRate}, overtime_rate = #{overtimeRate}, " +
            "late_penalty = #{latePenalty}, early_penalty = #{earlyPenalty}, absent_penalty = #{absentPenalty}, " +
            "sick_leave_rate = #{sickLeaveRate}, personal_leave_rate = #{personalLeaveRate} " +
            "WHERE config_id = #{configId}")
    int update(SalaryConfig config);

    @Delete("DELETE FROM salary_config WHERE config_id = #{id}")
    int delete(Long id);
}
