package org.example.libaiqi2023021108.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.libaiqi2023021108.common.BusinessException;
import org.example.libaiqi2023021108.config.DeepSeekProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
/**
 * 模块：AI助手
 * 功能：DeepSeek（OpenAI 兼容 /chat/completions）HTTP 客户端
 * 说明：使用 JDK21 内置 java.net.http.HttpClient，零额外依赖，离线构建安全
 */
@Service
public class DeepSeekClient {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekClient.class);

    private final DeepSeekProperties props;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public DeepSeekClient(DeepSeekProperties props, ObjectMapper objectMapper) {
        this.props = props;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    /** 发送一组 messages（含 system 角色），返回助手回复文本 */
    public String chat(List<Map<String, String>> messages) {
        if (props.getApiKey() == null || props.getApiKey().isBlank()) {
            throw new BusinessException(500, "AI助手未配置密钥(deepseek.api-key)");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", props.getModel());
        body.put("messages", messages);
        body.put("temperature", 0.3);
        body.put("stream", false);
        body.put("max_tokens", 1024);
        try {
            String json = objectMapper.writeValueAsString(body);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(props.getBaseUrl() + "/chat/completions"))
                    .timeout(Duration.ofSeconds(props.getTimeoutSeconds()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + props.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (resp.statusCode() != 200) {
                log.error("DeepSeek 调用失败 status={} body={}", resp.statusCode(), abbrev(resp.body()));
                throw new BusinessException(500, "AI服务暂时不可用，请稍后重试");
            }
            JsonNode root = objectMapper.readTree(resp.body());
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                return choices.get(0).path("message").path("content").asText("").trim();
            }
            throw new BusinessException(500, "AI服务返回异常");
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            log.error("DeepSeek 调用异常: {}", e.getMessage());
            throw new BusinessException(500, "AI服务连接失败：" + e.getMessage());
        }
    }

    private String abbrev(String s) {
        if (s == null) return "";
        return s.length() > 500 ? s.substring(0, 500) + "..." : s;
    }
}
