package com.skilal.flex_employ.mapper;

import com.skilal.flex_employ.entity.HolidayCalendar;
import org.apache.ibatis.annotations.*;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface HolidayCalendarMapper {

    @Select("SELECT * FROM holiday_calendar WHERE calendar_date = #{date}")
    HolidayCalendar findByDate(LocalDate date);

    @Select("SELECT * FROM holiday_calendar WHERE calendar_date BETWEEN #{startDate} AND #{endDate}")
    List<HolidayCalendar> findInRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Insert("INSERT INTO holiday_calendar (calendar_date, day_type, description) " +
            "VALUES (#{calendarDate}, #{dayType}, #{description}) " +
            "ON DUPLICATE KEY UPDATE day_type = #{dayType}, description = #{description}")
    int save(HolidayCalendar holidayCalendar);

    @Delete("DELETE FROM holiday_calendar WHERE calendar_date = #{date}")
    int delete(LocalDate date);
}
