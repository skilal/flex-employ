package com.skilal.flex_employ.mapper;

import com.skilal.flex_employ.entity.PaySlip;
import org.apache.ibatis.annotations.*;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface PaySlipMapper {

        @Select("SELECT p.*, u.account AS userName, pos.position_name AS positionName FROM pay_slip p " +
                        "LEFT JOIN on_duty_worker w ON p.on_duty_worker_id = w.on_duty_worker_id " +
                        "LEFT JOIN user u ON w.user_id = u.user_id " +
                        "LEFT JOIN position pos ON w.position_id = pos.position_id " +
                        "ORDER BY p.cycle_end DESC")
        List<PaySlip> findAll();

        @Select("SELECT p.*, pos.position_name AS positionName FROM pay_slip p " +
                        "INNER JOIN on_duty_worker w ON p.on_duty_worker_id = w.on_duty_worker_id " +
                        "LEFT JOIN position pos ON w.position_id = pos.position_id " +
                        "WHERE w.user_id = #{userId} ORDER BY p.cycle_end DESC")
        List<PaySlip> findByUserId(Long userId);

        @Select("SELECT * FROM pay_slip WHERE pay_record_id = #{payRecordId}")
        PaySlip findById(Long payRecordId);

        @Select("SELECT * FROM pay_slip WHERE on_duty_worker_id = #{workerId} ORDER BY cycle_end DESC LIMIT 1")
        PaySlip findLatestByWorkerId(Long workerId);

        @Select("SELECT COUNT(*) > 0 FROM pay_slip WHERE on_duty_worker_id = #{workerId} " +
                        "AND cycle_start = #{start} AND cycle_end = #{end}")
        boolean existsByRange(@Param("workerId") Long workerId, @Param("start") LocalDate start,
                        @Param("end") LocalDate end);

        @Insert("INSERT INTO pay_slip (on_duty_worker_id, cycle_start, cycle_end, deadline_date, " +
                        "actual_payment_date, payment_method, base_pay, bonus_pay, overtime_pay, allowance, " +
                        "pension_deduction, medical_deduction, unemployment_deduction, injury_deduction, " +
                        "pf_deduction, tax_amount, late_deduction, early_leave_deduction, absent_deduction, absence_deduction, "
                        +
                        "leave_deduction, gross_pay, total_deduction, net_pay, confirm_status) " +
                        "VALUES (#{onDutyWorkerId}, #{cycleStart}, #{cycleEnd}, #{deadlineDate}, " +
                        "#{actualPaymentDate}, #{paymentMethod}, #{basePay}, #{bonusPay}, #{overtimePay}, " +
                        "#{allowance}, #{pensionDeduction}, #{medicalDeduction}, #{unemploymentDeduction}, " +
                        "#{injuryDeduction}, #{pfDeduction}, #{taxAmount}, #{lateDeduction}, " +
                        "#{earlyLeaveDeduction}, #{absentDeduction}, #{absenceDeduction}, #{leaveDeduction}, #{grossPay}, "
                        +
                        "#{totalDeduction}, #{netPay}, #{confirmStatus})")
        @Options(useGeneratedKeys = true, keyProperty = "payRecordId")
        int insert(PaySlip paySlip);

        @Update("UPDATE pay_slip SET cycle_start = #{cycleStart}, cycle_end = #{cycleEnd}, " +
                        "deadline_date = #{deadlineDate}, actual_payment_date = #{actualPaymentDate}, " +
                        "payment_method = #{paymentMethod}, base_pay = #{basePay}, bonus_pay = #{bonusPay}, " +
                        "overtime_pay = #{overtimePay}, allowance = #{allowance}, pension_deduction = #{pensionDeduction}, "
                        +
                        "medical_deduction = #{medicalDeduction}, unemployment_deduction = #{unemploymentDeduction}, " +
                        "injury_deduction = #{injuryDeduction}, pf_deduction = #{pfDeduction}, " +
                        "tax_amount = #{taxAmount}, late_deduction = #{lateDeduction}, " +
                        "early_leave_deduction = #{earlyLeaveDeduction}, absent_deduction = #{absentDeduction}, " +
                        "absence_deduction = #{absenceDeduction}, " +
                        "leave_deduction = #{leaveDeduction}, gross_pay = #{grossPay}, " +
                        "total_deduction = #{totalDeduction}, net_pay = #{netPay}, " +
                        "confirm_status = #{confirmStatus} WHERE pay_record_id = #{payRecordId}")
        int update(PaySlip paySlip);

        @Delete("DELETE FROM pay_slip WHERE pay_record_id = #{payRecordId}")
        int delete(Long payRecordId);
}
