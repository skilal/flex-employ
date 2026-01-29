package com.skilal.flex_employ.controller;

import com.skilal.flex_employ.common.Result;
import com.skilal.flex_employ.entity.HolidayCalendar;
import com.skilal.flex_employ.mapper.HolidayCalendarMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/holidays")
public class HolidayController {

    @Autowired
    private HolidayCalendarMapper holidayMapper;

    @GetMapping
    public Result<List<HolidayCalendar>> getHolidays(@RequestParam String start, @RequestParam String end) {
        List<HolidayCalendar> holidays = holidayMapper.findInRange(LocalDate.parse(start), LocalDate.parse(end));
        return Result.success(holidays);
    }

    @PostMapping
    public Result<String> saveHoliday(@RequestBody HolidayCalendar holiday) {
        holidayMapper.save(holiday);
        return Result.success("保存成功");
    }

    @DeleteMapping("/{date}")
    public Result<String> deleteHoliday(@PathVariable String date) {
        holidayMapper.delete(LocalDate.parse(date));
        return Result.success("删除成功");
    }
}
