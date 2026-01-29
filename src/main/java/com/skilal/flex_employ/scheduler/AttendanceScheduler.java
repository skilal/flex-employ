package com.skilal.flex_employ.scheduler;

import com.skilal.flex_employ.entity.Attendance;
import com.skilal.flex_employ.entity.OnDutyWorker;
import com.skilal.flex_employ.mapper.AttendanceMapper;
import com.skilal.flex_employ.mapper.OnDutyWorkerMapper;
import com.skilal.flex_employ.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class AttendanceScheduler {

    @Autowired
    private OnDutyWorkerMapper workerMapper;

    @Autowired
    private AttendanceMapper attendanceMapper;

    @Autowired
    private AttendanceService attendanceService;

    /**
     * 每天凌晨 1 点执行，核算前一天的缺勤情况
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void autoCheckAbsence() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        // 1. 获取所有在岗员工 ( workerStatus = '在岗' )
        List<OnDutyWorker> workers = workerMapper.findAll("在岗", null, null);

        for (OnDutyWorker worker : workers) {
            // 检查昨日是否已有记录
            // 简单处理：如果昨日已有记录（可能是管理员补录或员工部分打卡），calculateStatus 会在 controller 层处理。
            // 这里主要处理“完全没记录”且“没请假”且“是工作日”的情况

            // 查找该员工该日在该岗位的记录
            // 注意：Mapper 需要一个 findByWorkerAndDate 的方法，或者直接在 findAll 里查
            // 为了效率，我们可以暂时简化逻辑：尝试生成记录，由于计算逻辑会包含判断

            if (attendanceService.isWorkDay(worker.getPositionId(), yesterday)) {
                // 判断是否已存在记录
                // 暂时利用 findAll 过滤一下，后面可以优化 Mapper
                List<Attendance> existing = attendanceMapper.findAll(yesterday, null, null, null);
                boolean recordExists = existing.stream()
                        .anyMatch(a -> a.getOnDutyWorkerId().equals(worker.getOnDutyWorkerId()));

                if (!recordExists) {
                    // 计算状态（内部会处理请假检查）
                    String status = attendanceService.calculateStatus(worker.getOnDutyWorkerId(), yesterday, null,
                            null);

                    if ("缺勤".equals(status)) {
                        Attendance absence = new Attendance();
                        absence.setOnDutyWorkerId(worker.getOnDutyWorkerId());
                        absence.setPositionId(worker.getPositionId());
                        absence.setAttendanceDate(yesterday);
                        absence.setAttendanceStatus("缺勤");
                        attendanceMapper.insert(absence);
                    } else if ("请假".equals(status)) {
                        // 也可以自动补全请假记录到考勤表
                        Attendance leaveRecord = new Attendance();
                        leaveRecord.setOnDutyWorkerId(worker.getOnDutyWorkerId());
                        leaveRecord.setPositionId(worker.getPositionId());
                        leaveRecord.setAttendanceDate(yesterday);
                        leaveRecord.setAttendanceStatus("请假");
                        attendanceMapper.insert(leaveRecord);
                    }
                }
            }
        }
    }
}
