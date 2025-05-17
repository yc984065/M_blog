package com.mrblue.controller; // 根据您的项目包结构调整

import com.mrblue.common.lang.Result; // 确保引入您项目中的Result类
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/upload")
public class ImageUploadController {


    @Value("${file.upload.path}")
    private String fileUploadPath;

    private final String imageSubDirectory = "images";

    @PostMapping("/image")
    @RequiresAuthentication // 上传图片需要用户登录认证
    public Result handleImageUpload(@RequestParam("image") MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            return Result.fail("上传失败：未选择任何图片文件");
        }

        // 获取原始文件名
        String originalFilename = imageFile.getOriginalFilename();
        if (originalFilename == null) {
            return Result.fail("上传失败：文件名无效");
        }

        // 获取文件后缀
        String fileExtension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < originalFilename.length() - 1) {
            fileExtension = originalFilename.substring(dotIndex);
        }

        // 生成新的唯一文件名，可以加入日期路径以更好地组织文件
        String datePath = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
        String uniqueFileNameWithoutExtension = UUID.randomUUID().toString();
        String newFileName = uniqueFileNameWithoutExtension + fileExtension;

        // 构建图片存储的完整目录路径
        File uploadDir = new File(fileUploadPath + File.separator + imageSubDirectory + File.separator + datePath);
        if (!uploadDir.exists()) {
            if (!uploadDir.mkdirs()) {
                log.error("无法创建图片上传目录: {}", uploadDir.getAbsolutePath());
                return Result.fail("上传失败：服务器内部错误（无法创建目录）");
            }
        }

        // 构建文件的完整保存路径
        File destinationFile = new File(uploadDir, newFileName);

        try {
            // 保存文件到服务器
            imageFile.transferTo(destinationFile);
            log.info("图片成功上传至: {}", destinationFile.getAbsolutePath());

            String imageUrl = "/uploads/" + imageSubDirectory + "/" + datePath + "/" + newFileName;

            return Result.succ("图片上传成功", Map.of("url", imageUrl));

        } catch (IOException e) {
            log.error("图片保存失败: {}", destinationFile.getAbsolutePath(), e);
            return Result.fail("上传失败：服务器内部错误（文件保存异常）");
        } catch (IllegalStateException e) {
            log.error("图片上传状态异常 (可能文件已被移动或处理): ", e);
            return Result.fail("上传失败：服务器内部错误（文件状态异常）");
        }
    }
}
