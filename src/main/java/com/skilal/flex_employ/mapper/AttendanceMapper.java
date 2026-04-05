package com.skilal.flex_employ.mapper;

import com.skilal.flex_employ.entity.Attendance;
import org.apache.ibatis.annotations.*;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface AttendanceMapper {

        @Select("<script>" +
                        "SELECT a.*, COALESCE(u.name, u.account) AS userName, p.position_name AS positionName, " +
                        "p.work_start_time AS workStartTime, p.work_end_time AS workEndTime, " +
                        "p.check_in_time AS checkInTime, p.check_out_time AS checkOutTime, " +
                        "IFNULL(p.working_days, '(未设定)') AS workingDays, " +
                        "IFNULL(sc.is_piece_work, 0) AS isPieceWork " +
                        "FROM attendance a " +
                        "INNER JOIN on_duty_worker w ON a.on_duty_worker_id = w.on_duty_worker_id " +
                        "LEFT JOIN user u ON w.user_id = u.user_id " +
                        "LEFT JOIN position p ON w.position_id = p.position_id " +
                        "LEFT JOIN salary_config sc ON p.salary_config_id = sc.config_id " +
                        "WHERE 1=1 " +
                        "<if test='attendanceDate != null'> AND a.attendance_date = #{attendanceDate} </if>" +
                        "<if test='attendanceStatus != null and attendanceStatus != \"\"'> AND a.attendance_status = #{attendanceStatus} </if>"
                        +
                        "<if test='userName != null and userName != \"\"'> AND (u.account LIKE CONCAT('%', #{userName}, '%') OR u.name LIKE CONCAT('%', #{userName}, '%')) </if>"
                        +
                        "<if test='positionName != null and positionName != \"\"'> AND p.position_name LIKE CONCAT('%', #{positionName}, '%') </if>"
                        +
                        "<if test='pieceworkOnly != null and pieceworkOnly'> AND IFNULL(sc.is_piece_work, 0) = 1 </if>"
                        +
                        "<if test='unrecordedOnly != null and unrecordedOnly'> AND a.piece_count IS NULL AND IFNULL(sc.is_piece_work, 0) = 1 </if>"
                        +
                        "ORDER BY a.attendance_date DESC" +
                        "</script>")
        List<Attendance> findAll(@Param("attendanceDate") LocalDate attendanceDate,
                        @Param("attendanceStatus") String attendanceStatus,
                        @Param("userName") String userName,
                        @Param("positionName") String positionName,
                        @Param("pieceworkOnly") Boolean pieceworkOnly,
                        @Param("unrecordedOnly") Boolean unrecordedOnly);

        @Select("<script>" +
                        "SELECT a.*, COALESCE(u.name, u.account) AS userName, p.position_name AS positionName, " +
                        "p.work_start_time AS workStartTime, p.work_end_time AS workEndTime, " +
                        "p.check_in_time AS checkInTime, p.check_out_time AS checkOutTime, " +
                        "IFNULL(p.working_days, '(未设定)') AS workingDays, " +
                        "IFNULL(sc.is_piece_work, 0) AS isPieceWork " +
                        "FROM attendance a " +
                        "INNER JOIN on_duty_worker w ON a.on_duty_worker_id = w.on_duty_worker_id " +
                        "LEFT JOIN user u ON w.user_id = u.user_id " +
                        "LEFT JOIN position p ON w.position_id = p.position_id " +
                        "LEFT JOIN salary_config sc ON p.salary_config_id = sc.config_id " +
                        "WHERE w.user_id = #{userId} " +
                        "<if test='startDate != null'> AND a.attendance_date &gt;= #{startDate} </if>" +
                        "<if test='endDate != null'> AND a.attendance_date &lt;= #{endDate} </if>" +
                        "ORDER BY a.attendance_date DESC" +
                        "</script>")
        List<Attendance> findByUserId(@Param("userId") Long userId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        @Select("SELECT * FROM attendance WHERE on_duty_worker_id = #{workerId} AND attendance_date BETWEEN #{start} AND #{end}")
        List<Attendance> findByWorkerAndRange(@Param("workerId") Long workerId, @Param("start") LocalDate start,
                        @Param("end") LocalDate end);

        @Select("SELECT * FROM attendance WHERE attendance_id = #{attendanceId}")
        Attendance findById(Long attendanceId);

        @Insert("INSERT INTO attendance (on_duty_worker_id, position_id, attendance_date, actual_check_in, " +
                        "actual_check_out, attendance_status, piece_count) " +
                        "VALUES (#{onDutyWorkerId}, #{positionId}, #{attendanceDate}, #{actualCheckIn}, #{actualCheckOut}, "
                        +
                        "#{attendanceStatus}, #{pieceCount})")
        @Options(useGeneratedKeys = true, keyProperty = "attendanceId")
        int insert(Attendance attendance);

        @Update("UPDATE attendance SET attendance_date = #{attendanceDate}, actual_check_in = #{actualCheckIn}, " +
                        "actual_check_out = #{actualCheckOut}, " +
                        "attendance_status = #{attendanceStatus}, piece_count = #{pieceCount} WHERE attendance_id = #{attendanceId}")
        int update(Attendance attendance);

        @Update("UPDATE attendance SET piece_count = #{pieceCount} WHERE attendance_id = #{attendanceId}")
        int updatePieceCount(@Param("attendanceId") Long attendanceId, @Param("pieceCount") Integer pieceCount);

        @Delete("DELETE FROM attendance WHERE attendance_id = #{attendanceId}")
        int delete(Long attendanceId);

        @Select("SELECT COUNT(*) FROM attendance WHERE on_duty_worker_id = #{onDutyWorkerId} AND attendance_date = #{date}")
        int countByWorkerAndDate(@Param("onDutyWorkerId") Long onDutyWorkerId, @Param("date") java.time.LocalDate date);

        @Select("SELECT * FROM attendance WHERE on_duty_worker_id = #{onDutyWorkerId} AND attendance_date = #{date} LIMIT 1")
        Attendance findByWorkerAndDate(@Param("onDutyWorkerId") Long onDutyWorkerId,
                        @Param("date") java.time.LocalDate date);

        @Select("SELECT COUNT(*) FROM attendance WHERE on_duty_worker_id = #{onDutyWorkerId} AND attendance_date = #{date} AND attendance_id != #{excludeId}")
        int countByWorkerAndDateExcludeId(@Param("onDutyWorkerId") Long onDutyWorkerId, @Param("date") LocalDate date,
                        @Param("excludeId") Long excludeId);
}
