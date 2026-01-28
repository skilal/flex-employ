package com.skilal.flex_employ.controller;

import com.skilal.flex_employ.common.Result;
import com.skilal.flex_employ.entity.Position;
import com.skilal.flex_employ.mapper.PositionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/positions")
public class PositionController {

    @Autowired
    private PositionMapper positionMapper;

    @GetMapping
    public Result<List<Position>> getPositions(@RequestParam(required = false) String positionName,
            @RequestParam(required = false) String workLocation,
            @RequestParam(required = false) String employmentType,
            @RequestParam(required = false) Integer positionStatus) {
        List<Position> positions = positionMapper.findAll(positionName, workLocation, employmentType, positionStatus);
        return Result.success(positions);
    }

    @GetMapping("/recruiting")
    public Result<List<Position>> getRecruitingPositions() {
        List<Position> positions = positionMapper.findRecruiting();
        return Result.success(positions);
    }

    @PostMapping
    public Result<String> createPosition(@RequestBody Position position) {
        int result = positionMapper.insert(position);
        if (result > 0) {
            return Result.success("创建成功");
        }
        return Result.error("创建失败");
    }

    @PutMapping("/{id}")
    public Result<String> updatePosition(@PathVariable Long id, @RequestBody Position position) {
        position.setPositionId(id);
        int result = positionMapper.update(position);
        if (result > 0) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> deletePosition(@PathVariable Long id) {
        int result = positionMapper.delete(id);
        if (result > 0) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}
