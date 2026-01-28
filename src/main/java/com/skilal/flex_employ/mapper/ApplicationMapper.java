package com.skilal.flex_employ.mapper;

import com.skilal.flex_employ.entity.Application;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ApplicationMapper {

        @Select("<script>" +
                        "SELECT a.*, u.account AS userName, p.position_name AS positionName " +
                        "FROM application a " +
                        "LEFT JOIN user u ON a.user_id = u.user_id " +
                        "LEFT JOIN position p ON a.position_id = p.position_id " +
                        "WHERE 1=1 " +
                        "<if test='status != null and status != \"\"'> AND a.status = #{status} </if>" +
                        "<if test='userName != null and userName != \"\"'> AND u.account LIKE CONCAT('%', #{userName}, '%') </if>"
                        +
                        "<if test='positionName != null and positionName != \"\"'> AND p.position_name LIKE CONCAT('%', #{positionName}, '%') </if>"
                        +
                        "ORDER BY a.apply_time DESC" +
                        "</script>")
        List<Application> findAll(@Param("status") String status,
                        @Param("userName") String userName,
                        @Param("positionName") String positionName);

        @Select("SELECT * FROM application WHERE user_id = #{userId} ORDER BY apply_time DESC")
        List<Application> findByUserId(Long userId);

        @Select("SELECT * FROM application WHERE application_id = #{applicationId}")
        Application findById(Long applicationId);

        @Insert("INSERT INTO application (user_id, position_id, resume_pdf_path, status) " +
                        "VALUES (#{userId}, #{positionId}, #{resumePdfPath}, #{status})")
        @Options(useGeneratedKeys = true, keyProperty = "applicationId")
        int insert(Application application);

        @Update("UPDATE application SET status = #{status}, approve_time = NOW() WHERE application_id = #{applicationId}")
        int updateStatus(@Param("applicationId") Long applicationId, @Param("status") String status);

        @Delete("DELETE FROM application WHERE application_id = #{applicationId}")
        int delete(Long applicationId);
}
