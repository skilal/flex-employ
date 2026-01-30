package com.skilal.flex_employ.controller;

import com.skilal.flex_employ.common.Result;
import com.skilal.flex_employ.entity.OnDutyWorker;
import com.skilal.flex_employ.mapper.OnDutyWorkerMapper;
import com.skilal.flex_employ.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workers")
public class WorkerController {

    @Autowired
    private OnDutyWorkerMapper workerMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public Result<List<OnDutyWorker>> getWorkers(@RequestParam(required = false) String workerStatus,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String positionName) {
        List<OnDutyWorker> workers = workerMapper.findAll(workerStatus, userName, positionName);
        return Result.success(workers);
    }

    @GetMapping("/my")
    public Result<List<OnDutyWorker>> getMyWorkerRecord(@RequestHeader("Authorization") String token) {
        token = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        List<OnDutyWorker> workers = workerMapper.findByUserId(userId);
        return Result.success(workers);
    }

    @PostMapping
    public Result<String> createWorker(@RequestBody OnDutyWorker worker) {
        deduceWorkerStatus(worker);
        int result = workerMapper.insert(worker);
        if (result > 0) {
            return Result.success("添加成功");
        }
        return Result.error("添加失败");
    }

    @PutMapping("/{id}")
    public Result<String> updateWorker(@PathVariable Long id, @RequestBody OnDutyWorker worker) {
        worker.setOnDutyWorkerId(id);
        deduceWorkerStatus(worker);
        int result = workerMapper.update(worker);
        if (result > 0) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    private void deduceWorkerStatus(OnDutyWorker worker) {
        if (worker.getLeaveDate() != null) {
            worker.setWorkerStatus("已结束");
        } else if (worker.getHireDate() != null) {
            worker.setWorkerStatus("在岗");
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteWorker(@PathVariable Long id) {
        int result = workerMapper.delete(id);
        if (result > 0) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}
