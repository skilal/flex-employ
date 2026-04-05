package com.skilal.flex_employ.controller;

import com.skilal.flex_employ.common.CheckRole;
import com.skilal.flex_employ.common.Role;
import com.skilal.flex_employ.common.Result;
import com.skilal.flex_employ.entity.Application;
import com.skilal.flex_employ.entity.OnDutyWorker;
import com.skilal.flex_employ.mapper.ApplicationMapper;
import com.skilal.flex_employ.mapper.OnDutyWorkerMapper;
import com.aliyun.oss.AliOssUtil;
import com.skilal.flex_employ.mapper.PositionMapper;
import com.skilal.flex_employ.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private OnDutyWorkerMapper onDutyWorkerMapper;

    @Autowired
    private PositionMapper positionMapper;

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AliOssUtil ossUtil; // 来自 aliyun-oss-spring-boot-starter 自动装配

    @CheckRole(Role.ADMIN)
    @GetMapping
    public Result<List<Application>> getApplications(@RequestParam(required = false) String status,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String positionName) {
        List<Application> applications = applicationMapper.findAll(status, userName, positionName);
        return Result.success(applications);
    }

    @CheckRole(Role.EMPLOYEE)
    @GetMapping("/my")
    public Result<List<Application>> getMyApplications(@RequestHeader("Authorization") String token) {
        token = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        List<Application> applications = applicationMapper.findByUserId(userId);
        return Result.success(applications);
    }

    // 检查员工是否已在某岗位在岗
    @GetMapping("/check-worker-status")
    public Result<Map<String, Object>> checkWorkerStatus(
            @RequestParam Long positionId,
            @RequestHeader("Authorization") String token) {
        token = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);

        int count = onDutyWorkerMapper.checkWorkerStatus(userId, positionId);
        boolean isOnDuty = count > 0;

        return Result.success(Map.of(
                "isOnDuty", isOnDuty,
                "message", isOnDuty ? "您已经是该岗位的在岗员工，无需再次申请" : "可以申请"));
    }

    // 检查工作时间冲突
    @GetMapping("/check-time-conflict")
    public Result<Map<String, Object>> checkTimeConflict(
            @RequestParam Long positionId,
            @RequestHeader("Authorization") String token) {
        token = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);

        // 获取要申请的岗位信息
        com.skilal.flex_employ.entity.Position targetPosition = positionMapper.findById(positionId);
        if (targetPosition == null) {
            return Result.error("岗位不存在");
        }

        // 获取用户当前所有在岗记录
        java.util.List<com.skilal.flex_employ.entity.OnDutyWorker> onDutyList = onDutyWorkerMapper.findByUserId(userId,
                null, null);

        // 检查每个在岗岗位的时间是否与目标岗位冲突
        for (com.skilal.flex_employ.entity.OnDutyWorker worker : onDutyList) {
            log.info("检查冲突 - 当前在岗记录ID: {}, 岗位ID: {}, 状态: {}",
                    worker.getOnDutyWorkerId(), worker.getPositionId(), worker.getWorkerStatus());
            if (!"在岗".equals(worker.getWorkerStatus())) {
                continue; // 只检查在岗状态的记录
            }

            // 获取在岗岗位的详细信息
            com.skilal.flex_employ.entity.Position currentPosition = positionMapper.findById(worker.getPositionId());
            if (currentPosition == null) {
                continue;
            }

            // 检查日期范围是否重叠
            if (targetPosition.getWorkStartTime() != null && targetPosition.getWorkEndTime() != null
                    && currentPosition.getWorkStartTime() != null && currentPosition.getWorkEndTime() != null) {

                boolean dateOverlap = !targetPosition.getWorkStartTime().isAfter(currentPosition.getWorkEndTime())
                        && !targetPosition.getWorkEndTime().isBefore(currentPosition.getWorkStartTime());

                if (dateOverlap) {
                    // 如果日期有重叠，进一步检查时段是否有交集
                    LocalTime tStart1 = targetPosition.getCheckInTime();
                    LocalTime tEnd1 = targetPosition.getCheckOutTime();
                    LocalTime tStart2 = currentPosition.getCheckInTime();
                    LocalTime tEnd2 = currentPosition.getCheckOutTime();

                    if (tStart1 != null && tEnd1 != null && tStart2 != null && tEnd2 != null) {
                        // 时段冲突判定算法：!(结束1 <= 开始2 || 结束2 <= 开始1)
                        boolean timeOverlap = tStart1.isBefore(tEnd2) && tEnd1.isAfter(tStart2);

                        if (timeOverlap) {
                            return Result.success(Map.of(
                                    "hasConflict", true,
                                    "conflictPosition", currentPosition.getPositionName(),
                                    "message",
                                    "该岗位工作时间与您在岗的【" + currentPosition.getPositionName() + "】岗位存在冲突 (日期重叠且时段交叠)"));
                        }
                    } else {
                        // 如果任一岗位没有设置时段，则保守起见认为凡是日期重叠即为冲突
                        return Result.success(Map.of(
                                "hasConflict", true,
                                "conflictPosition", currentPosition.getPositionName(),
                                "message", "该岗位工作时间与您在岗的【" + currentPosition.getPositionName() + "】岗位在日期上存在冲突"));
                    }
                }
            }
        }

        return Result.success(Map.of(
                "hasConflict", false,
                "message", "无时间冲突"));
    }

    @CheckRole(Role.EMPLOYEE)
    @PostMapping
    public Result<String> createApplication(@RequestBody Application application,
            @RequestHeader("Authorization") String token) {
        token = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        application.setUserId(userId);
        application.setStatus("已申请");
        int result = applicationMapper.insert(application);
        if (result > 0) {
            return Result.success("申请提交成功");
        }
        return Result.error("申请提交失败");
    }

    @CheckRole(Role.ADMIN)
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    @PutMapping("/{id}/approve")
    public Result<String> approveApplication(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        String status = (String) data.get("status");

        // 如果是通过操作，必须检查名额
        if ("已通过".equals(status)) {
            Application app = applicationMapper.findById(id);
            if (app == null)
                return Result.error("申请不存在");

            com.skilal.flex_employ.entity.Position pos = positionMapper.findById(app.getPositionId());
            if (pos == null)
                return Result.error("岗位不存在");

            if (pos.getRemainingPositions() != null && pos.getRemainingPositions() <= 0) {
                return Result.error("审批失败：该岗位招聘名额已满");
            }
        }

        // 更新申请状态
        int result = applicationMapper.updateStatus(id, status);
        if (result <= 0) {
            return Result.error("审批失败");
        }

        // 如果审批通过，创建在岗员工记录
        if ("已通过".equals(status)) {
            // 获取申请信息
            Application application = applicationMapper.findById(id);
            if (application == null) {
                throw new com.skilal.flex_employ.common.BusinessException("申请不存在");
            }

            // 创建在岗员工记录
            OnDutyWorker worker = new OnDutyWorker();
            worker.setUserId(application.getUserId());
            worker.setPositionId(application.getPositionId());

            // 从请求中获取入职信息
            String hireDateStr = (String) data.get("hireDate");

            worker.setHireDate(LocalDate.parse(hireDateStr));
            worker.setWorkerStatus("在岗"); // 设置在岗状态

            int workerResult = onDutyWorkerMapper.insert(worker);
            if (workerResult <= 0) {
                throw new com.skilal.flex_employ.common.BusinessException("创建在岗员工记录失败");
            }

            // 乐观锁扣减名额：重新读取最新 version
            com.skilal.flex_employ.entity.Position position = positionMapper.findById(application.getPositionId());
            if (position == null) {
                throw new com.skilal.flex_employ.common.BusinessException("岗位数据异常");
            }
            int decreased = positionMapper.decreaseRemainingPositions(
                    application.getPositionId(), position.getVersion());
            if (decreased <= 0) {
                // version 不匹配说明有并发请求已占用，触发回滚刚才更新的 application 状态和新增的 worker
                throw new com.skilal.flex_employ.common.BusinessException("审批失败：该岗位名额已被其他操作占用，请重试");
            }

            // 检查剩余人数，如果为0则关闭岗位
            com.skilal.flex_employ.entity.Position updated = positionMapper.findById(application.getPositionId());
            if (updated != null && updated.getRemainingPositions() != null
                    && updated.getRemainingPositions() <= 0) {
                positionMapper.closePosition(application.getPositionId());
            }
        }

        return Result.success("审批成功");

    }

    @CheckRole(Role.EMPLOYEE)
    @PostMapping("/upload")
    public Result<String> uploadResume(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }

        try {
            // 确保上传目录存在
            // String uploadDir = System.getProperty("user.dir") + File.separator +
            // "uploads" + File.separator + "resumes"
            // + File.separator;
            // File dir = new File(uploadDir);
            // if (!dir.exists()) {
            // dir.mkdirs();
            // }
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            // String fileName = UUID.randomUUID().toString() + extension;
            //
            // // 保存文件
            // Path filePath = Paths.get(uploadDir + fileName);
            // Files.write(filePath, file.getBytes());
            //
            // // 返回前端可访问的相对路径
            // return Result.success("/uploads/resumes/" + fileName);
            // } catch (IOException e) {
            String objectName = "resumes/" + UUID.randomUUID() + extension;
            // 调用 OSS 工具类上传并返回访问 URL
            String url = ossUtil.upload(file.getInputStream(), objectName);
            return Result.success(url);
        } catch (Exception e) {
            log.error("简历上传失败", e);
            return Result.error("简历上传失败: " + e.getMessage());
        }
    }

    // ==================== OSS PDF 代理预览接口 ====================
    // 背景：OSS 默认域名因安全策略强制添加 Content-Disposition: attachment 和
    // x-oss-force-download: true，导致浏览器触发下载而非在线预览。
    // 方案：由后端从 OSS 拉取文件流，以 Content-Disposition: inline 转发给浏览器，
    // 绕过 OSS 的强制下载限制。
    // 回退方案：若不再需要代理，前端 handleViewResume 改回直接 window.open(ossUrl, '_blank') 即可。
    @CheckRole({ Role.ADMIN, Role.EMPLOYEE })
    @GetMapping("/preview-resume")
    public void previewResume(@RequestParam String url, HttpServletResponse response) {
        try {
            // 从 OSS 拉取文件流（Bucket 为公共读，可直接 HTTP 访问）
            URL ossUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) ossUrl.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(30000);
            conn.connect();

            // 设置响应头：inline 告知浏览器在页面内预览，而非触发下载
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline");

            // 将 OSS 文件流直接透传给浏览器
            try (InputStream in = conn.getInputStream();
                    OutputStream out = response.getOutputStream()) {
                in.transferTo(out);
            }
        } catch (Exception e) {
            log.error("PDF 代理预览失败: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    // ==================== 代理预览接口 END ====================

    @CheckRole(Role.ADMIN)
    @DeleteMapping("/{id}")
    public Result<String> deleteApplication(@PathVariable Long id) {
        int result = applicationMapper.delete(id);
        if (result > 0) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}
