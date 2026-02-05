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

        @Insert("INSERT INTO salary_config (config_name, pay_cycle, billing_method, base_rate, has_overtime_pay, " +
                        "late_penalty, early_penalty, absent_penalty, sick_leave_rate, personal_leave_rate, " +
                        "performance_bonus, commission, bonus, pension_rate, medical_rate, unemployment_rate, injury_rate, housing_fund_rate, "
                        +
                        "pension_rate_ent, medical_rate_ent, unemployment_rate_ent, injury_rate_ent, housing_fund_rate_ent, "
                        +
                        "social_security_base_upper, social_security_base_lower, " +
                        "overtime_weekday_multiplier, overtime_weekend_multiplier, overtime_holiday_multiplier, " +
                        "overtime_calc_mode, overtime_threshold_min, overtime_rounding_unit) " +
                        "VALUES (#{configName}, #{payCycle}, #{billingMethod}, #{baseRate}, #{hasOvertimePay}, " +
                        "#{latePenalty}, #{earlyPenalty}, #{absentPenalty}, #{sickLeaveRate}, #{personalLeaveRate}, " +
                        "#{performanceBonus}, #{commission}, #{bonus}, #{pensionRate}, #{medicalRate}, #{unemploymentRate}, #{injuryRate}, #{housingFundRate}, "
                        +
                        "#{pensionRateEnt}, #{medicalRateEnt}, #{unemploymentRateEnt}, #{injuryRateEnt}, #{housingFundRateEnt}, "
                        +
                        "#{socialSecurityBaseUpper}, #{socialSecurityBaseLower}, " +
                        "#{overtimeWeekdayMultiplier}, #{overtimeWeekendMultiplier}, #{overtimeHolidayMultiplier}, " +
                        "#{overtimeCalcMode}, #{overtimeThresholdMin}, #{overtimeRoundingUnit})")
        @Options(useGeneratedKeys = true, keyProperty = "configId")
        int insert(SalaryConfig config);

        @Update("UPDATE salary_config SET config_name = #{configName}, pay_cycle = #{payCycle}, " +
                        "billing_method = #{billingMethod}, base_rate = #{baseRate}, has_overtime_pay = #{hasOvertimePay}, "
                        +
                        "late_penalty = #{latePenalty}, early_penalty = #{earlyPenalty}, absent_penalty = #{absentPenalty}, "
                        +
                        "sick_leave_rate = #{sickLeaveRate}, personal_leave_rate = #{personalLeaveRate}, " +
                        "performance_bonus = #{performanceBonus}, commission = #{commission}, bonus = #{bonus}, " +
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
                        "overtime_threshold_min = #{overtimeThresholdMin}, overtime_rounding_unit = #{overtimeRoundingUnit} "
                        +
                        "WHERE config_id = #{configId}")
        int update(SalaryConfig config);

        @Delete("DELETE FROM salary_config WHERE config_id = #{id}")
        int delete(Long id);
}
