package com.skilal.flex_employ.mapper;

import com.skilal.flex_employ.entity.PaySlip;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface PaySlipMapper {

    @Select("<script>" +
            "SELECT * FROM pay_slip WHERE 1=1 " +
            "<if test='paymentStatus != null and paymentStatus != \"\"'> AND payment_status = #{paymentStatus} </if>" +
            "ORDER BY payment_date DESC" +
            "</script>")
    List<PaySlip> findAll(@Param("paymentStatus") String paymentStatus);

    @Select("SELECT p.* FROM pay_slip p " +
            "INNER JOIN on_duty_worker w ON p.on_duty_worker_id = w.on_duty_worker_id " +
            "WHERE w.user_id = #{userId} ORDER BY p.payment_date DESC")
    List<PaySlip> findByUserId(Long userId);

    @Select("SELECT * FROM pay_slip WHERE pay_record_id = #{payRecordId}")
    PaySlip findById(Long payRecordId);

    @Insert("INSERT INTO pay_slip (on_duty_worker_id, cycle_start, cycle_end, payment_date, base_pay, " +
            "performance_bonus, overtime_pay, allowance, actual_work_time, insurance_total, pf_contribution, " +
            "tax_amount, late_deduction, custom_add_deduct, gross_pay, total_deduction, net_pay, payment_status) " +
            "VALUES (#{onDutyWorkerId}, #{cycleStart}, #{cycleEnd}, #{paymentDate}, #{basePay}, #{performanceBonus}, " +
            "#{overtimePay}, #{allowance}, #{actualWorkTime}, #{insuranceTotal}, #{pfContribution}, #{taxAmount}, " +
            "#{lateDeduction}, #{customAddDeduct}, #{grossPay}, #{totalDeduction}, #{netPay}, #{paymentStatus})")
    @Options(useGeneratedKeys = true, keyProperty = "payRecordId")
    int insert(PaySlip paySlip);

    @Update("UPDATE pay_slip SET cycle_start = #{cycleStart}, cycle_end = #{cycleEnd}, payment_date = #{paymentDate}, "
            +
            "base_pay = #{basePay}, performance_bonus = #{performanceBonus}, overtime_pay = #{overtimePay}, " +
            "allowance = #{allowance}, actual_work_time = #{actualWorkTime}, insurance_total = #{insuranceTotal}, " +
            "pf_contribution = #{pfContribution}, tax_amount = #{taxAmount}, late_deduction = #{lateDeduction}, " +
            "custom_add_deduct = #{customAddDeduct}, gross_pay = #{grossPay}, total_deduction = #{totalDeduction}, " +
            "net_pay = #{netPay}, payment_status = #{paymentStatus} WHERE pay_record_id = #{payRecordId}")
    int update(PaySlip paySlip);

    @Delete("DELETE FROM pay_slip WHERE pay_record_id = #{payRecordId}")
    int delete(Long payRecordId);
}
