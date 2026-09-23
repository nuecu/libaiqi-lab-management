package org.example.libaiqi2023021108.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * 模块：通用支撑
 * 功能：Web MVC 配置
 * 说明：把上传目录映射到 /uploads/**，使 FileController 返回的访问 URL 可以直接被浏览器/前端 <img> 访问。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Paths.get(uploadDir).toAbsolutePath().normalize().toUri().toString();
        // 注意：Path#toUri() 只有在目录「已存在」时才会补上结尾的 "/"。
        // 首次启动时 uploads 目录还不存在（由 FileController 首次上传时创建），
        // 缺少结尾斜杠会导致 /uploads/** 按相对路径解析到父目录，静态资源永远 404/500。
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}
