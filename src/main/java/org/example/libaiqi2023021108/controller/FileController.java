package org.example.libaiqi2023021108.controller;

import org.example.libaiqi2023021108.common.BusinessException;
import org.example.libaiqi2023021108.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * 模块：通用支撑
 * 功能：文件上传
 * 说明：仪器图片(imageUrl)、实验附件(attachmentUrl)、维修报告(reportUrl)、故障图片(faultImage)
 *      等字段需要一个统一的上传入口。文件按 yyyy/MM/dd 分目录存放，文件名用 UUID 防止重名覆盖，
 *      通过 /uploads/** 静态资源映射对外访问。
 * 权限：登录即可上传（不限定具体业务权限码，避免各角色被挡死）
 */
@RestController
@RequestMapping("/api/files")
public class FileController {

    /** 允许上传的扩展名白名单 */
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "png", "jpg", "jpeg", "gif", "webp", "pdf", "doc", "docx", "xls", "xlsx");

    /** 单个文件大小上限：50MB（与 spring.servlet.multipart.max-file-size 保持一致） */
    private static final long MAX_FILE_SIZE = 50L * 1024 * 1024;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('ROLE_AUTHENTICATED')")
    public Result<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("文件大小超过限制（最大 50MB）");
        }

        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
        String extension = "";
        int dot = originalName.lastIndexOf('.');
        if (dot >= 0) {
            extension = originalName.substring(dot + 1).toLowerCase(Locale.ROOT);
        }
        if (extension.isEmpty() || !ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("不支持的文件类型，仅允许：" + String.join("/", ALLOWED_EXTENSIONS));
        }

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String newFileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;

        try {
            Path baseDir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path targetDir = baseDir.resolve(datePath);
            Files.createDirectories(targetDir);
            Path targetFile = targetDir.resolve(newFileName);
            file.transferTo(targetFile.toFile());

            String url = "/uploads/" + datePath + "/" + newFileName;
            Map<String, Object> data = new HashMap<>();
            data.put("url", url);
            data.put("fileName", newFileName);
            data.put("originalName", originalName);
            data.put("size", file.getSize());
            return Result.success(data);
        } catch (IOException e) {
            throw new BusinessException("文件保存失败：" + e.getMessage());
        }
    }
}
