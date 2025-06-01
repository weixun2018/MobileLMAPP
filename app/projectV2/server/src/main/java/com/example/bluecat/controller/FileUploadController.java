package com.example.bluecat.controller;

import com.example.bluecat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Slf4j
public class FileUploadController {

    private final UserService userService;
    private static final String UPLOAD_DIR = "uploads/avatars/";

    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file, @RequestParam("userId") Long userId) {
        try {
            log.info("开始处理文件上传请求，用户ID: {}", userId);
            
            if (file.isEmpty()) {
                log.error("上传失败：文件为空");
                return ResponseEntity.badRequest().body("文件不能为空");
            }

            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                log.error("上传失败：不支持的文件类型 {}", contentType);
                return ResponseEntity.badRequest().body("只支持图片文件");
            }

            // 创建上传目录
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                log.info("创建上传目录: {}", UPLOAD_DIR);
                if (!uploadDir.mkdirs()) {
                    log.error("创建上传目录失败");
                    return ResponseEntity.internalServerError().body("创建上传目录失败");
                }
            }

            // 生成唯一的文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? 
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String filename = UUID.randomUUID().toString() + extension;
            
            // 保存文件
            Path path = Paths.get(UPLOAD_DIR + filename);
            log.info("保存文件到: {}", path);
            Files.write(path, file.getBytes());

            // 更新用户头像URL
            String avatarUrl = "/uploads/avatars/" + filename;
            Map<String, String> fieldData = new HashMap<>();
            fieldData.put("avatarUrl", avatarUrl);
            userService.updateUserField(userId, fieldData);

            log.info("文件上传成功，avatarUrl: {}", avatarUrl);
            Map<String, String> response = new HashMap<>();
            response.put("url", avatarUrl);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("文件上传失败", e);
            return ResponseEntity.internalServerError().body("文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("处理文件上传请求时发生错误", e);
            return ResponseEntity.internalServerError().body("服务器错误: " + e.getMessage());
        }
    }
} 