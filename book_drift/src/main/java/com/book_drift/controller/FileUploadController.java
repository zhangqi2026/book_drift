package com.book_drift.controller;

import com.book_drift.vo.BaseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/file")
@Api(tags = "文件上传")
public class FileUploadController {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @PostMapping("/upload")
    @ApiOperation("上传文件")
    public BaseResult<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        if (file.isEmpty()) {
            return BaseResult.error("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        
        String[] allowedExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};
        boolean isValid = false;
        for (String ext : allowedExtensions) {
            if (extension.toLowerCase().equals(ext)) {
                isValid = true;
                break;
            }
        }
        if (!isValid) {
            return BaseResult.error("只支持 jpg、jpeg、png、gif、bmp 格式的图片");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            return BaseResult.error("文件大小不能超过 5MB");
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            String datePath = sdf.format(new Date());
            String fullPath = uploadPath + File.separator + "avatar" + File.separator + datePath;
            
            File dir = new File(fullPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String newFileName = UUID.randomUUID().toString() + extension;
            File destFile = new File(fullPath + File.separator + newFileName);
            file.transferTo(destFile);

            String fileUrl = "/uploads/avatar/" + datePath.replace("\\", "/") + "/" + newFileName;
            
            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("fileName", newFileName);
            
            return BaseResult.ok("上传成功", result);
        } catch (IOException e) {
            e.printStackTrace();
            return BaseResult.error("上传失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除文件")
    public BaseResult<Boolean> deleteFile(@RequestParam("fileUrl") String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return BaseResult.ok("文件URL为空，无需删除", true);
        }

        try {
            String filePath = fileUrl.replace("/uploads/", "");
            File file = new File(uploadPath + File.separator + filePath);
            
            if (file.exists() && file.isFile()) {
                boolean deleted = file.delete();
                if (deleted) {
                    System.out.println("文件删除成功: " + file.getAbsolutePath());
                } else {
                    System.out.println("文件删除失败: " + file.getAbsolutePath());
                }
            } else {
                System.out.println("文件不存在: " + file.getAbsolutePath());
            }
            return BaseResult.ok("删除操作完成", true);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("删除文件时出错: " + e.getMessage());
            return BaseResult.ok("删除操作完成", true);
        }
    }
}
