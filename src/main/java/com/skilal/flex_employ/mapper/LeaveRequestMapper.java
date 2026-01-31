package com.skilal.flex_employ.mapper;

import com.skilal.flex_employ.entity.LeaveRequest;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface LeaveRequestMapper {

        @Select("<script>" +
                        "SELECT l.*, u.account AS userName, p.position_name AS positionName " +
                        "FROM leave_request l " +
                        "LEFT JOIN user u ON l.user_id = u.user_id " +
                        "LEFT JOIN position p ON l.position_id = p.position_id " +
                        "WHERE 1=1 " +
                        "<if test='status != null and status != \"\"'> AND l.status = #{status} </if>" +
                        "<if test='userName != null and userName != \"\"'> AND u.account LIKE CONCAT('%', #{userName}, '%') </if>"
                        +
                        "<if test='positionName != null and positionName != \"\"'> AND p.position_name LIKE CONCAT('%', #{positionName}, '%') </if>"
                        +
                        "ORDER BY l.apply_time DESC" +
                        "</script>")
        List<LeaveRequest> findAll(@Param("status") String status,
                        @Param("userName") String userName,
                        @Param("positionName") String positionName);

        @Select("SELECT l.*, u.account AS userName, p.position_name AS positionName " +
                        "FROM leave_request l " +
                        "LEFT JOIN user u ON l.user_id = u.user_id " +
                        "LEFT JOIN position p ON l.position_id = p.position_id " +
                        "WHERE l.user_id = #{userId} ORDER BY l.apply_time DESC")
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

        // 检查用户在某天是否处于已通过的请假状态
        @Select("SELECT COUNT(*) FROM leave_request " +
                        "WHERE user_id = #{userId} AND status IN ('已通过', '同意') " +
                        "AND #{date} BETWEEN start_date AND end_date")
        int checkLeave(@Param("userId") Long userId, @Param("date") java.time.LocalDate date);

        // 检查用户是否存在状态为“申请中”且日期重叠的记录
        @Select("SELECT COUNT(*) FROM leave_request " +
                        "WHERE user_id = #{userId} AND status = '申请中' " +
                        "AND (start_date <= #{endDate} AND end_date >= #{startDate})")
        int countOverlappingRequests(@Param("userId") Long userId,
                        @Param("startDate") java.time.LocalDate startDate,
                        @Param("endDate") java.time.LocalDate endDate);
}
