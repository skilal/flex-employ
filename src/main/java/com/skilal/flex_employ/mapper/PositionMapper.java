package com.skilal.flex_employ.mapper;

import com.skilal.flex_employ.entity.Position;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface PositionMapper {

        @Select("<script>" +
                        "SELECT p.*, p.work_start_time AS workStartTime, p.work_end_time AS workEndTime, " +
                        "p.total_positions AS totalPositions, p.remaining_positions AS remainingPositions, c.company_name FROM position p "
                        +
                        "LEFT JOIN company c ON p.labor_company_id = c.company_id " +
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

        @Select("SELECT p.*, p.work_start_time AS workStartTime, p.work_end_time AS workEndTime, " +
                        "p.total_positions AS totalPositions, p.remaining_positions AS remainingPositions, c.company_name FROM position p "
                        +
                        "LEFT JOIN company c ON p.labor_company_id = c.company_id " +
                        "WHERE p.position_status = 1 ORDER BY p.created_at DESC")
        List<Position> findRecruiting();

        @Select("SELECT p.*, p.work_start_time AS workStartTime, p.work_end_time AS workEndTime, " +
                        "p.total_positions AS totalPositions, p.remaining_positions AS remainingPositions FROM position p WHERE position_id = #{positionId}")
        Position findById(Long positionId);

        @Insert("INSERT INTO position (position_name, work_location, region_code, duty_desc, work_start_time, " +
                        "work_end_time, employment_type, labor_company_id, basic_salary, pay_cycle, salary_desc, " +
                        "daily_hours, weekly_freq, position_status, responsible_id, special_note, creator_id, total_positions, remaining_positions) "
                        +
                        "VALUES (#{positionName}, #{workLocation}, #{regionCode}, #{dutyDesc}, #{workStartTime}, " +
                        "#{workEndTime}, #{employmentType}, #{laborCompanyId}, #{basicSalary}, #{payCycle}, #{salaryDesc}, "
                        +
                        "#{dailyHours}, #{weeklyFreq}, #{positionStatus}, #{responsibleId}, #{specialNote}, #{creatorId}, #{totalPositions}, #{remainingPositions})")
        @Options(useGeneratedKeys = true, keyProperty = "positionId")
        int insert(Position position);

        @Update("UPDATE position SET position_name = #{positionName}, work_location = #{workLocation}, " +
                        "region_code = #{regionCode}, duty_desc = #{dutyDesc}, work_start_time = #{workStartTime}, " +
                        "work_end_time = #{workEndTime}, employment_type = #{employmentType}, labor_company_id = #{laborCompanyId}, "
                        +
                        "basic_salary = #{basicSalary}, pay_cycle = #{payCycle}, salary_desc = #{salaryDesc}, " +
                        "daily_hours = #{dailyHours}, weekly_freq = #{weeklyFreq}, position_status = #{positionStatus}, "
                        +
                        "responsible_id = #{responsibleId}, special_note = #{specialNote}, total_positions = #{totalPositions}, remaining_positions = #{remainingPositions} WHERE position_id = #{positionId}")
        int update(Position position);

        @Delete("DELETE FROM position WHERE position_id = #{positionId}")
        int delete(Long positionId);

        // 减少剩余人数
        @Update("UPDATE position SET remaining_positions = remaining_positions - 1 WHERE position_id = #{positionId} AND remaining_positions > 0")
        int decreaseRemainingPositions(Long positionId);

        // 关闭岗位
        @Update("UPDATE position SET position_status = 0 WHERE position_id = #{positionId}")
        int closePosition(Long positionId);
}
