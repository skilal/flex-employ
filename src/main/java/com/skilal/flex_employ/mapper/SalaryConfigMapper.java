package com.skilal.flex_employ.mapper;

import com.skilal.flex_employ.entity.SalaryConfig;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface SalaryConfigMapper {

        @Select("<script>" +
                        "SELECT * FROM salary_config " +
                        "WHERE 1=1 " +
                        "<if test='configName != null and configName != \"\"'> AND config_name LIKE CONCAT('%', #{configName}, '%') </if>"
                        +
                        "<if test='payCycle != null and payCycle != \"\"'> AND pay_cycle = #{payCycle} </if>" +
                        "<if test='billingMethod != null'> AND billing_method = #{billingMethod} </if>" +
                        "ORDER BY created_at DESC" +
                        "</script>")
        List<SalaryConfig> findAll(@Param("configName") String configName,
                        @Param("payCycle") String payCycle,
                        @Param("billingMethod") Integer billingMethod);

        @Select("SELECT * FROM salary_config WHERE config_id = #{id}")
        SalaryConfig findById(Long id);

        @Insert("INSERT INTO salary_config (config_name, pay_cycle, billing_method, base_rate, has_overtime_pay, " +
                        "late_penalty, early_penalty, absent_penalty, absence_penalty, sick_leave_rate, personal_leave_rate, "
                        +
                        "performance_bonus, commission, bonus, allowance, pension_rate, medical_rate, unemployment_rate, injury_rate, housing_fund_rate, "
                        +
                        "pension_rate_ent, medical_rate_ent, unemployment_rate_ent, injury_rate_ent, housing_fund_rate_ent, "
                        +
                        "social_security_base_upper, social_security_base_lower, " +
                        "overtime_weekday_multiplier, overtime_weekend_multiplier, overtime_holiday_multiplier, " +
                        "overtime_calc_mode, overtime_threshold_min, overtime_rounding_unit, " +
                        "late_threshold_min, early_leave_threshold_min) " +
                        "VALUES (#{configName}, #{payCycle}, #{billingMethod}, #{baseRate}, #{hasOvertimePay}, " +
                        "#{latePenalty}, #{earlyPenalty}, #{absentPenalty}, #{absencePenalty}, #{sickLeaveRate}, #{personalLeaveRate}, "
                        +
                        "#{performanceBonus}, #{commission}, #{bonus}, #{allowance}, #{pensionRate}, #{medicalRate}, #{unemploymentRate}, #{injuryRate}, #{housingFundRate}, "
                        +
                        "#{pensionRateEnt}, #{medicalRateEnt}, #{unemploymentRateEnt}, #{injuryRateEnt}, #{housingFundRateEnt}, "
                        +
                        "#{socialSecurityBaseUpper}, #{socialSecurityBaseLower}, " +
                        "#{overtimeWeekdayMultiplier}, #{overtimeWeekendMultiplier}, #{overtimeHolidayMultiplier}, " +
                        "#{overtimeCalcMode}, #{overtimeThresholdMin}, #{overtimeRoundingUnit}, " +
                        "#{lateThresholdMin}, #{earlyLeaveThresholdMin})")
        @Options(useGeneratedKeys = true, keyProperty = "configId")
        int insert(SalaryConfig config);

        @Update("UPDATE salary_config SET config_name = #{configName}, pay_cycle = #{payCycle}, " +
                        "billing_method = #{billingMethod}, base_rate = #{baseRate}, has_overtime_pay = #{hasOvertimePay}, "
                        +
                        "late_penalty = #{latePenalty}, early_penalty = #{earlyPenalty}, absent_penalty = #{absentPenalty}, "
                        +
                        "absence_penalty = #{absencePenalty}, sick_leave_rate = #{sickLeaveRate}, personal_leave_rate = #{personalLeaveRate}, "
                        +
                        "performance_bonus = #{performanceBonus}, commission = #{commission}, bonus = #{bonus}, allowance = #{allowance}, "
                        +
                        "pension_rate = #{pensionRate}, medical_rate = #{medicalRate}, unemployment_rate = #{unemploymentRate}, "
                        +
                        "injury_rate = #{injuryRate}, housing_fund_rate = #{housingFundRate}, " +
                        "pension_rate_ent = #{pensionRateEnt}, medical_rate_ent = #{medicalRateEnt}, unemployment_rate_ent = #{unemploymentRateEnt}, "
                        +
                        "injury_rate_ent = #{injuryRateEnt}, housing_fund_rate_ent = #{housingFundRateEnt}, " +
                        "social_security_base_upper = #{socialSecurityBaseUpper}, social_security_base_lower = #{socialSecurityBaseLower}, "
                        +
                        "overtime_weekday_multiplier = #{overtimeWeekdayMultiplier}, overtime_weekend_multiplier = #{overtimeWeekendMultiplier}, "
                        +
                        "overtime_holiday_multiplier = #{overtimeHolidayMultiplier}, overtime_calc_mode = #{overtimeCalcMode}, "
                        +
                        "overtime_threshold_min = #{overtimeThresholdMin}, overtime_rounding_unit = #{overtimeRoundingUnit}, "
                        +
                        "late_threshold_min = #{lateThresholdMin}, early_leave_threshold_min = #{earlyLeaveThresholdMin} "
                        +
                        "WHERE config_id = #{configId}")
        int update(SalaryConfig config);

        @Delete("DELETE FROM salary_config WHERE config_id = #{id}")
        int delete(Long id);
}
