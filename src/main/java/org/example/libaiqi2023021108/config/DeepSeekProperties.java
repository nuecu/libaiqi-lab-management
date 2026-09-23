package org.example.libaiqi2023021108.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
/**
 * 模块：AI助手
 * 功能：DeepSeek 接入配置（密钥优先由环境变量 DEEPSEEK_API_KEY 覆盖）
 */
@Data
@Component
@ConfigurationProperties(prefix = "deepseek")
public class DeepSeekProperties {
    private String apiKey;
    private String baseUrl = "https://api.deepseek.com";
    private String model = "deepseek-chat";
    private int timeoutSeconds = 60;
}
