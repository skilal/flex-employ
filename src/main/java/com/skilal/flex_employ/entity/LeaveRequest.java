package com.skilal.flex_employ.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LeaveRequest {
    private Long leaveRequestId;
    private Long userId;
    private Long positionId;
    private String leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal days;
    private String reason;
    private String status;
    private Long approverUserId;
    private LocalDateTime applyTime;
    private LocalDateTime approveTime;

    // JOIN查询字段
    private String userName;
    private String positionName;
}
