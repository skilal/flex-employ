package com.skilal.flex_employ.mapper;

import com.skilal.flex_employ.entity.Application;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ApplicationMapper {

    @Select("<script>" +
            "SELECT * FROM application WHERE 1=1 " +
            "<if test='status != null and status != \"\"'> AND status = #{status} </if>" +
            "ORDER BY apply_time DESC" +
            "</script>")
    List<Application> findAll(@Param("status") String status);

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
