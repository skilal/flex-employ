package com.skilal.flex_employ.mapper;

import com.skilal.flex_employ.entity.Attendance;
import org.apache.ibatis.annotations.*;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface AttendanceMapper {

        @Select("<script>" +
                        "SELECT a.*, u.account AS userName, p.position_name AS positionName, " +
                        "p.work_start_time AS workStartTime, p.work_end_time AS workEndTime " +
                        "FROM attendance a " +
                        "INNER JOIN on_duty_worker w ON a.on_duty_worker_id = w.on_duty_worker_id " +
                        "LEFT JOIN user u ON w.user_id = u.user_id " +
                        "LEFT JOIN position p ON a.position_id = p.position_id " +
                        "WHERE 1=1 " +
                        "<if test='attendanceDate != null'> AND a.attendance_date = #{attendanceDate} </if>" +
                        "<if test='attendanceStatus != null and attendanceStatus != \"\"'> AND a.attendance_status = #{attendanceStatus} </if>"
                        +
                        "<if test='userName != null and userName != \"\"'> AND u.account LIKE CONCAT('%', #{userName}, '%') </if>"
                        +
                        "<if test='positionName != null and positionName != \"\"'> AND p.position_name LIKE CONCAT('%', #{positionName}, '%') </if>"
                        +
                        "ORDER BY a.attendance_date DESC" +
                        "</script>")
        List<Attendance> findAll(@Param("attendanceDate") LocalDate attendanceDate,
                        @Param("attendanceStatus") String attendanceStatus,
                        @Param("userName") String userName,
                        @Param("positionName") String positionName);

        @Select("SELECT a.*, u.account AS userName, p.position_name AS positionName, " +
                        "p.work_start_time AS workStartTime, p.work_end_time AS workEndTime " +
                        "FROM attendance a " +
                        "INNER JOIN on_duty_worker w ON a.on_duty_worker_id = w.on_duty_worker_id " +
                        "LEFT JOIN user u ON w.user_id = u.user_id " +
                        "LEFT JOIN position p ON a.position_id = p.position_id " +
                        "WHERE w.user_id = #{userId} ORDER BY a.attendance_date DESC")
        List<Attendance> findByUserId(Long userId);

        @Select("SELECT * FROM attendance WHERE attendance_id = #{attendanceId}")
        Attendance findById(Long attendanceId);

        @Insert("INSERT INTO attendance (on_duty_worker_id, position_id, attendance_date, actual_check_in, " +
                        "actual_check_out, attendance_status) " +
                        "VALUES (#{onDutyWorkerId}, #{positionId}, #{attendanceDate}, #{actualCheckIn}, #{actualCheckOut}, #{attendanceStatus})")
        @Options(useGeneratedKeys = true, keyProperty = "attendanceId")
        int insert(Attendance attendance);

        @Update("UPDATE attendance SET attendance_date = #{attendanceDate}, actual_check_in = #{actualCheckIn}, " +
                        "actual_check_out = #{actualCheckOut}, attendance_status = #{attendanceStatus} " +
                        "WHERE attendance_id = #{attendanceId}")
        int update(Attendance attendance);

        @Delete("DELETE FROM attendance WHERE attendance_id = #{attendanceId}")
        int delete(Long attendanceId);

        @Select("SELECT COUNT(*) FROM attendance WHERE on_duty_worker_id = #{onDutyWorkerId} AND attendance_date = #{date}")
        int countByWorkerAndDate(@Param("onDutyWorkerId") Long onDutyWorkerId, @Param("date") java.time.LocalDate date);

        @Select("SELECT COUNT(*) FROM attendance WHERE on_duty_worker_id = #{onDutyWorkerId} AND attendance_date = #{date} AND attendance_id != #{excludeId}")
        int countByWorkerAndDateExcludeId(@Param("onDutyWorkerId") Long onDutyWorkerId,
                        @Param("date") java.time.LocalDate date, @Param("excludeId") Long excludeId);
}
