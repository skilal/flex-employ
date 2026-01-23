package com.skilal.flex_employ.mapper;

import com.skilal.flex_employ.entity.Position;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface PositionMapper {

    @Select("<script>" +
            "SELECT * FROM position WHERE 1=1 " +
            "<if test='positionName != null and positionName != \"\"'> AND position_name LIKE CONCAT('%', #{positionName}, '%') </if>"
            +
            "<if test='workLocation != null and workLocation != \"\"'> AND work_location LIKE CONCAT('%', #{workLocation}, '%') </if>"
            +
            "<if test='positionStatus != null'> AND position_status = #{positionStatus} </if>" +
            "ORDER BY created_at DESC" +
            "</script>")
    List<Position> findAll(@Param("positionName") String positionName,
            @Param("workLocation") String workLocation,
            @Param("positionStatus") Integer positionStatus);

    @Select("SELECT * FROM position WHERE position_status = 1 ORDER BY created_at DESC")
    List<Position> findRecruiting();

    @Select("SELECT * FROM position WHERE position_id = #{positionId}")
    Position findById(Long positionId);

    @Insert("INSERT INTO position (position_name, work_location, region_code, duty_desc, work_start_time, " +
            "work_end_time, employment_type, labor_company_id, basic_salary, pay_cycle, salary_desc, " +
            "daily_hours, weekly_freq, position_status, responsible_id, special_note, creator_id) " +
            "VALUES (#{positionName}, #{workLocation}, #{regionCode}, #{dutyDesc}, #{workStartTime}, " +
            "#{workEndTime}, #{employmentType}, #{laborCompanyId}, #{basicSalary}, #{payCycle}, #{salaryDesc}, " +
            "#{dailyHours}, #{weeklyFreq}, #{positionStatus}, #{responsibleId}, #{specialNote}, #{creatorId})")
    @Options(useGeneratedKeys = true, keyProperty = "positionId")
    int insert(Position position);

    @Update("UPDATE position SET position_name = #{positionName}, work_location = #{workLocation}, " +
            "region_code = #{regionCode}, duty_desc = #{dutyDesc}, work_start_time = #{workStartTime}, " +
            "work_end_time = #{workEndTime}, employment_type = #{employmentType}, labor_company_id = #{laborCompanyId}, "
            +
            "basic_salary = #{basicSalary}, pay_cycle = #{payCycle}, salary_desc = #{salaryDesc}, " +
            "daily_hours = #{dailyHours}, weekly_freq = #{weeklyFreq}, position_status = #{positionStatus}, " +
            "responsible_id = #{responsibleId}, special_note = #{specialNote} WHERE position_id = #{positionId}")
    int update(Position position);

    @Delete("DELETE FROM position WHERE position_id = #{positionId}")
    int delete(Long positionId);
}
