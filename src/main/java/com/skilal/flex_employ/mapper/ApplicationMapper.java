package com.skilal.flex_employ.mapper;

import com.skilal.flex_employ.entity.Application;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ApplicationMapper {

        @Select("<script>" +
                        "SELECT a.*, COALESCE(u.name, u.account) AS userName, u.gender, u.phone, p.position_name AS positionName, "
                        +
                        "p.work_start_time AS workStartTime, p.work_end_time AS workEndTime, " +
                        "a.application_note AS applicationNote " +
                        "FROM application a " +
                        "LEFT JOIN user u ON a.user_id = u.user_id " +
                        "LEFT JOIN position p ON a.position_id = p.position_id " +
                        "WHERE 1=1 " +
                        "<if test='status != null and status != \"\"'> AND a.status = #{status} </if>" +
                        "<if test='userName != null and userName != \"\"'> AND (u.account LIKE CONCAT('%', #{userName}, '%') OR u.name LIKE CONCAT('%', #{userName}, '%')) </if>"
                        +
                        "<if test='positionName != null and positionName != \"\"'> AND p.position_name LIKE CONCAT('%', #{positionName}, '%') </if>"
                        +
                        "ORDER BY a.apply_time DESC" +
                        "</script>")
        List<Application> findAll(@Param("status") String status,
                        @Param("userName") String userName,
                        @Param("positionName") String positionName);

        @Select("SELECT a.*, COALESCE(u.name, u.account) AS userName, u.gender, u.phone, p.position_name AS positionName, "
                        +
                        "p.work_start_time AS workStartTime, p.work_end_time AS workEndTime " +
                        "FROM application a " +
                        "LEFT JOIN user u ON a.user_id = u.user_id " +
                        "LEFT JOIN position p ON a.position_id = p.position_id " +
                        "WHERE a.user_id = #{userId} ORDER BY a.apply_time DESC")
        List<Application> findByUserId(Long userId);

        @Select("SELECT a.*, COALESCE(u.name, u.account) AS userName, u.gender, u.phone, p.position_name AS positionName, "
                        +
                        "p.work_start_time AS workStartTime, p.work_end_time AS workEndTime " +
                        "FROM application a " +
                        "LEFT JOIN user u ON a.user_id = u.user_id " +
                        "LEFT JOIN position p ON a.position_id = p.position_id " +
                        "WHERE a.application_id = #{applicationId}")
        Application findById(Long applicationId);

        @Insert("INSERT INTO application (user_id, position_id, resume_pdf_path, status, application_note) " +
                        "VALUES (#{userId}, #{positionId}, #{resumePdfPath}, #{status}, #{applicationNote})")
        @Options(useGeneratedKeys = true, keyProperty = "applicationId")
        int insert(Application application);

        @Update("UPDATE application SET status = #{status}, approve_time = NOW() WHERE application_id = #{applicationId}")
        int updateStatus(@Param("applicationId") Long applicationId, @Param("status") String status);

        @Delete("DELETE FROM application WHERE application_id = #{applicationId}")
        int delete(Long applicationId);

        @Select("SELECT COUNT(*) FROM application WHERE position_id = #{positionId} AND status = '已申请'")
        int countUnprocessedByPositionId(Long positionId);

        @Select("SELECT COUNT(*) FROM application WHERE user_id = #{userId} AND position_id = #{positionId}")
        int countByUserIdAndPositionId(@Param("userId") Long userId, @Param("positionId") Long positionId);
}
