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
    public Result<List<OnDutyWorker>> getMyWorkerRecord(
            @RequestParam(required = false) String positionName,
            @RequestParam(required = false) String workerStatus,
            @RequestHeader("Authorization") String token) {
        token = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        List<OnDutyWorker> workers = workerMapper.findByUserId(userId, positionName, workerStatus);
        return Result.success(workers);
    }

    @PostMapping
    public Result<String> createWorker(@RequestBody OnDutyWorker worker) {
        // 1. 检查是否已经在该岗位在岗
        if (workerMapper.checkWorkerStatus(worker.getUserId(), worker.getPositionId()) > 0) {
            return Result.error("添加失败：该员工已在该岗位处于“在岗”状态，请勿重复添加");
        }

        // 2. 检查名额是否充足
        com.skilal.flex_employ.entity.Position position = positionMapper.findById(worker.getPositionId());
        if (position != null && position.getRemainingPositions() != null && position.getRemainingPositions() <= 0) {
            return Result.error("添加失败：该岗位招聘名额已满");
        }

        deduceWorkerStatus(worker);
        int result = workerMapper.insert(worker);
        if (result > 0) {
            // 扣减名额
            positionMapper.decreaseRemainingPositions(worker.getPositionId());
            // 再次检查并决定是否变更为已满状态
            com.skilal.flex_employ.entity.Position updatedPos = positionMapper.findById(worker.getPositionId());
            if (updatedPos != null && updatedPos.getRemainingPositions() != null
                    && updatedPos.getRemainingPositions() <= 0) {
                positionMapper.closePosition(worker.getPositionId());
            }
            return Result.success("添加成功");
        }
        return Result.error("添加失败");
    }

    @PutMapping("/{id}")
    public Result<String> updateWorker(@PathVariable Long id, @RequestBody OnDutyWorker worker) {
        OnDutyWorker oldWorker = workerMapper.findById(id);
        worker.setOnDutyWorkerId(id);
        deduceWorkerStatus(worker);

        int result = workerMapper.update(worker);
        if (result > 0) {
            // 如果状态从 在岗 变为 已结束，释放名额
            if (oldWorker != null && "在岗".equals(oldWorker.getWorkerStatus())
                    && "已结束".equals(worker.getWorkerStatus())) {
                positionMapper.increaseRemainingPositions(oldWorker.getPositionId());
                positionMapper.openPosition(oldWorker.getPositionId());
            }
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

    @Autowired
    private com.skilal.flex_employ.mapper.PositionMapper positionMapper;

    @Autowired
    private com.skilal.flex_employ.mapper.PaySlipMapper paySlipMapper;

    @DeleteMapping("/{id}")
    public Result<String> deleteWorker(@PathVariable Long id) {
        OnDutyWorker worker = workerMapper.findById(id);
        // 1. 检查是否存在已结算薪资（防止删除有财务流水的人员）
        if (paySlipMapper.countSettledByWorkerId(id) > 0) {
            return Result.error("删除失败：该员工存在已确认或已发放的薪资记录，为保证财务数据完整性，无法删除该在岗记录");
        }

        int result = workerMapper.delete(id);
        if (result > 0) {
            // 如果删除的是 在岗 状态的人员，释放名额
            if (worker != null && "在岗".equals(worker.getWorkerStatus())) {
                positionMapper.increaseRemainingPositions(worker.getPositionId());
                positionMapper.openPosition(worker.getPositionId());
            }
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}
