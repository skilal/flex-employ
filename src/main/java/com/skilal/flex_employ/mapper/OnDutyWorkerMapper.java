package com.skilal.flex_employ.mapper;

import com.skilal.flex_employ.entity.OnDutyWorker;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface OnDutyWorkerMapper {

        @Select("<script>" +
                        "SELECT w.*, u.account AS userName, p.position_name AS positionName " +
                        "FROM on_duty_worker w " +
                        "LEFT JOIN user u ON w.user_id = u.user_id " +
                        "LEFT JOIN position p ON w.position_id = p.position_id " +
                        "WHERE 1=1 " +
                        "<if test='workerStatus != null and workerStatus != \"\"'> AND w.worker_status = #{workerStatus} </if>"
                        +
                        "<if test='userName != null and userName != \"\"'> AND u.account LIKE CONCAT('%', #{userName}, '%') </if>"
                        +
                        "<if test='positionName != null and positionName != \"\"'> AND p.position_name LIKE CONCAT('%', #{positionName}, '%') </if>"
                        +
                        "ORDER BY w.hire_date DESC" +
                        "</script>")
        List<OnDutyWorker> findAll(@Param("workerStatus") String workerStatus,
                        @Param("userName") String userName,
                        @Param("positionName") String positionName);

        @Select("SELECT w.*, p.position_name AS positionName " +
                        "FROM on_duty_worker w " +
                        "LEFT JOIN position p ON w.position_id = p.position_id " +
                        "WHERE w.user_id = #{userId}")
        List<OnDutyWorker> findByUserId(Long userId);

        @Select("SELECT * FROM on_duty_worker WHERE on_duty_worker_id = #{onDutyWorkerId}")
        OnDutyWorker findById(Long onDutyWorkerId);

        @Insert("INSERT INTO on_duty_worker (user_id, position_id, check_in_time, check_out_time, hire_date, leave_date, worker_status) "
                        +
                        "VALUES (#{userId}, #{positionId}, #{checkInTime}, #{checkOutTime}, #{hireDate}, #{leaveDate}, #{workerStatus})")
        @Options(useGeneratedKeys = true, keyProperty = "onDutyWorkerId")
        int insert(OnDutyWorker worker);

        @Update("UPDATE on_duty_worker SET check_in_time = #{checkInTime}, check_out_time = #{checkOutTime}, " +
                        "hire_date = #{hireDate}, leave_date = #{leaveDate}, worker_status = #{workerStatus} " +
                        "WHERE on_duty_worker_id = #{onDutyWorkerId}")
        int update(OnDutyWorker worker);

        @Delete("DELETE FROM on_duty_worker WHERE on_duty_worker_id = #{onDutyWorkerId}")
        int delete(Long onDutyWorkerId);
}
