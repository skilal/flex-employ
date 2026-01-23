package com.skilal.flex_employ.mapper;

import com.skilal.flex_employ.entity.Attendance;
import org.apache.ibatis.annotations.*;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface AttendanceMapper {

    @Select("<script>" +
            "SELECT * FROM attendance WHERE 1=1 " +
            "<if test='attendanceDate != null'> AND attendance_date = #{attendanceDate} </if>" +
            "<if test='attendanceStatus != null and attendanceStatus != \"\"'> AND attendance_status = #{attendanceStatus} </if>"
            +
            "ORDER BY attendance_date DESC" +
            "</script>")
    List<Attendance> findAll(@Param("attendanceDate") LocalDate attendanceDate,
            @Param("attendanceStatus") String attendanceStatus);

    @Select("SELECT a.* FROM attendance a " +
            "INNER JOIN on_duty_worker w ON a.on_duty_worker_id = w.on_duty_worker_id " +
            "WHERE w.user_id = #{userId} ORDER BY a.attendance_date DESC")
    List<Attendance> findByUserId(Long userId);

    @Select("SELECT * FROM attendance WHERE attendance_id = #{attendanceId}")
    Attendance findById(Long attendanceId);

    @Insert("INSERT INTO attendance (on_duty_worker_id, position_id, attendance_date, actual_check_in, " +
            "actual_check_out, attendance_status) " +
            "VALUES (#{onDutyWorkerId}, #{positionId}, #{attendanceDate}, #{actualCheckIn}, #{actualCheckOut}, #{attendanceStatus})")
    @Options(useGeneratedKeys = true, keyProperty = "attendanceId")
    int insert(Attendance attendance);

    @Update("UPDATE attendance SET actual_check_in = #{actualCheckIn}, actual_check_out = #{actualCheckOut}, " +
            "attendance_status = #{attendanceStatus} WHERE attendance_id = #{attendanceId}")
    int update(Attendance attendance);

    @Delete("DELETE FROM attendance WHERE attendance_id = #{attendanceId}")
    int delete(Long attendanceId);
}
