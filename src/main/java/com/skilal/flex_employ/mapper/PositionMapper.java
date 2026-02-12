package com.skilal.flex_employ.mapper;

import com.skilal.flex_employ.entity.Position;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface PositionMapper {

        @Select("<script>" +
                        "SELECT p.*, p.work_start_time AS workStartTime, p.work_end_time AS workEndTime, p.working_days AS workingDays, "
                        +
                        "p.check_in_time AS checkInTime, p.check_out_time AS checkOutTime, " +
                        "p.salary_config_id AS salaryConfigId, sc.config_name AS salaryConfigName, " +
                        "sc.pay_cycle AS payCycle, sc.billing_method AS billingMethod, sc.base_rate AS baseRate, " +
                        "sc.has_overtime_pay AS hasOvertimePay, sc.overtime_weekday_multiplier AS overtimeWeekdayMultiplier, "
                        +
                        "p.total_positions AS totalPositions, p.remaining_positions AS remainingPositions, c.company_name, cp.company_name AS salaryPayerName, COALESCE(u.name, u.account) AS responsibleName, u.phone AS responsiblePhone FROM position p "
                        +
                        "LEFT JOIN company c ON p.labor_company_id = c.company_id " +
                        "LEFT JOIN company cp ON p.salary_payer_id = cp.company_id " +
                        "LEFT JOIN user u ON p.responsible_id = u.user_id " +
                        "LEFT JOIN salary_config sc ON p.salary_config_id = sc.config_id " +
                        "WHERE 1=1 " +
                        "<if test='positionName != null and positionName != \"\"'> AND p.position_name LIKE CONCAT('%', #{positionName}, '%') </if>"
                        +
                        "<if test='workLocation != null and workLocation != \"\"'> AND p.work_location LIKE CONCAT('%', #{workLocation}, '%') </if>"
                        +
                        "<if test='employmentType != null and employmentType != \"\"'> AND p.employment_type = #{employmentType} </if>"
                        +
                        "<if test='positionStatus != null'> AND p.position_status = #{positionStatus} </if>" +
                        "ORDER BY p.created_at DESC" +
                        "</script>")
        List<Position> findAll(@Param("positionName") String positionName,
                        @Param("workLocation") String workLocation,
                        @Param("employmentType") String employmentType,
                        @Param("positionStatus") Integer positionStatus);

        @Select("SELECT p.*, p.work_start_time AS workStartTime, p.work_end_time AS workEndTime, p.working_days AS workingDays, "
                        +
                        "p.check_in_time AS checkInTime, p.check_out_time AS checkOutTime, " +
                        "p.salary_config_id AS salaryConfigId, sc.config_name AS salaryConfigName, " +
                        "sc.pay_cycle AS payCycle, sc.billing_method AS billingMethod, sc.base_rate AS baseRate, " +
                        "sc.has_overtime_pay AS hasOvertimePay, sc.overtime_weekday_multiplier AS overtimeWeekdayMultiplier, "
                        +
                        "p.total_positions AS totalPositions, p.remaining_positions AS remainingPositions, c.company_name, cp.company_name AS salaryPayerName, COALESCE(u.name, u.account) AS responsibleName, u.phone AS responsiblePhone FROM position p "
                        +
                        "LEFT JOIN company c ON p.labor_company_id = c.company_id " +
                        "LEFT JOIN company cp ON p.salary_payer_id = cp.company_id " +
                        "LEFT JOIN user u ON p.responsible_id = u.user_id " +
                        "LEFT JOIN salary_config sc ON p.salary_config_id = sc.config_id " +
                        "WHERE p.remaining_positions > 0 AND p.position_status != 0 ORDER BY p.created_at DESC")
        List<Position> findRecruiting();

        @Select("SELECT p.*, p.work_start_time AS workStartTime, p.work_end_time AS workEndTime, p.working_days AS workingDays, "
                        +
                        "p.check_in_time AS checkInTime, p.check_out_time AS checkOutTime, " +
                        "p.salary_config_id AS salaryConfigId, sc.config_name AS salaryConfigName, " +
                        "sc.pay_cycle AS payCycle, sc.billing_method AS billingMethod, sc.base_rate AS baseRate, " +
                        "sc.has_overtime_pay AS hasOvertimePay, sc.overtime_weekday_multiplier AS overtimeWeekdayMultiplier, "
                        +
                        "p.total_positions AS totalPositions, p.remaining_positions AS remainingPositions, p.salary_payer_id AS salaryPayerId, cp.company_name AS salaryPayerName, COALESCE(u.name, u.account) AS responsibleName, u.phone AS responsiblePhone FROM position p "
                        +
                        "LEFT JOIN company cp ON p.salary_payer_id = cp.company_id " +
                        "LEFT JOIN user u ON p.responsible_id = u.user_id " +
                        "LEFT JOIN salary_config sc ON p.salary_config_id = sc.config_id " +
                        "WHERE position_id = #{positionId}")
        Position findById(Long positionId);

        @Insert("INSERT INTO position (position_name, work_location, region_code, duty_desc, work_start_time, " +
                        "work_end_time, employment_type, labor_company_id, salary_payer_id, salary_config_id, salary_desc, "
                        +
                        "daily_hours, weekly_freq, working_days, check_in_time, check_out_time, position_status, responsible_id, special_note, creator_id, total_positions, remaining_positions) "
                        +
                        "VALUES (#{positionName}, #{workLocation}, '', #{dutyDesc}, #{workStartTime}, " +
                        "#{workEndTime}, #{employmentType}, #{laborCompanyId}, #{salaryPayerId}, #{salaryConfigId}, #{salaryDesc}, "
                        +
                        "#{dailyHours}, #{weeklyFreq}, #{workingDays}, #{checkInTime}, #{checkOutTime}, #{positionStatus}, #{responsibleId}, #{specialNote}, #{creatorId}, #{totalPositions}, #{remainingPositions})")
        @Options(useGeneratedKeys = true, keyProperty = "positionId")
        int insert(Position position);

        @Update("UPDATE position SET position_name = #{positionName}, work_location = #{workLocation}, " +
                        "region_code = '', duty_desc = #{dutyDesc}, work_start_time = #{workStartTime}, " +
                        "work_end_time = #{workEndTime}, employment_type = #{employmentType}, labor_company_id = #{laborCompanyId}, "
                        +
                        "salary_payer_id = #{salaryPayerId}, salary_config_id = #{salaryConfigId}, salary_desc = #{salaryDesc}, "
                        +
                        "daily_hours = #{dailyHours}, weekly_freq = #{weeklyFreq}, working_days = #{workingDays}, " +
                        "check_in_time = #{checkInTime}, check_out_time = #{checkOutTime}, position_status = #{positionStatus}, "
                        +
                        "responsible_id = #{responsibleId}, special_note = #{specialNote}, total_positions = #{totalPositions}, remaining_positions = #{remainingPositions} WHERE position_id = #{positionId}")
        int update(Position position);

        @Delete("DELETE FROM position WHERE position_id = #{positionId}")
        int delete(Long positionId);

        @Select("SELECT COUNT(*) FROM position WHERE labor_company_id = #{companyId} OR salary_payer_id = #{companyId}")
        int countByCompanyId(Long companyId);

        @Select("SELECT COUNT(*) FROM position WHERE salary_config_id = #{configId}")
        int countBySalaryConfigId(Long configId);

        @Update("UPDATE position SET remaining_positions = remaining_positions - 1 WHERE position_id = #{positionId} AND remaining_positions > 0")
        int decreaseRemainingPositions(Long positionId);

        @Update("UPDATE position SET remaining_positions = remaining_positions + 1 WHERE position_id = #{positionId} AND remaining_positions < total_positions")
        int increaseRemainingPositions(Long positionId);

        @Update("UPDATE position SET position_status = 1 WHERE position_id = #{positionId} AND position_status = 2")
        int openPosition(Long positionId);

        @Update("UPDATE position SET position_status = 2 WHERE position_id = #{positionId}")
        int closePosition(Long positionId);
}
