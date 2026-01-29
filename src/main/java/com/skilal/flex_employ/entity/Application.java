package com.skilal.flex_employ.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Application {
    private Long applicationId;
    private Long userId;
    private Long positionId;
    private String resumePdfPath;
    private String status;
    private LocalDateTime applyTime;
    private LocalDateTime approveTime;

    // JOIN查询字段
    private String userName;
    private String positionName;

    // 申请说明
    private String applicationNote;
}
