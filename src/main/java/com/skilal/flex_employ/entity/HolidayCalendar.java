package com.skilal.flex_employ.entity;

import lombok.Data;
import java.time.LocalDate;

/**
 * 节假日日历实体
 */
@Data
public class HolidayCalendar {
    private LocalDate calendarDate; // 日期
    /**
     * 1: 节假日 (休息)
     * 2: 调休 (加班)
     */
    private Integer dayType;
    private String description; // 描述，如：国庆节
}
