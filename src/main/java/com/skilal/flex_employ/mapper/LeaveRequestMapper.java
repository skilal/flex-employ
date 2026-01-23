package com.skilal.flex_employ.mapper;

import com.skilal.flex_employ.entity.LeaveRequest;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface LeaveRequestMapper {

    @Select("<script>" +
            "SELECT * FROM leave_request WHERE 1=1 " +
            "<if test='status != null and status != \"\"'> AND status = #{status} </if>" +
            "ORDER BY apply_time DESC" +
            "</script>")
    List<LeaveRequest> findAll(@Param("status") String status);

    @Select("SELECT * FROM leave_request WHERE user_id = #{userId} ORDER BY apply_time DESC")
    List<LeaveRequest> findByUserId(Long userId);

    @Select("SELECT * FROM leave_request WHERE leave_request_id = #{leaveRequestId}")
    LeaveRequest findById(Long leaveRequestId);

    @Insert("INSERT INTO leave_request (user_id, position_id, leave_type, start_date, end_date, days, reason, status) "
            +
            "VALUES (#{userId}, #{positionId}, #{leaveType}, #{startDate}, #{endDate}, #{days}, #{reason}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "leaveRequestId")
    int insert(LeaveRequest leaveRequest);

    @Update("UPDATE leave_request SET status = #{status}, approve_time = NOW() WHERE leave_request_id = #{leaveRequestId}")
    int updateStatus(@Param("leaveRequestId") Long leaveRequestId, @Param("status") String status);

    @Delete("DELETE FROM leave_request WHERE leave_request_id = #{leaveRequestId}")
    int delete(Long leaveRequestId);
}
