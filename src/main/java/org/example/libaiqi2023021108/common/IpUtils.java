package org.example.libaiqi2023021108.common;

import jakarta.servlet.http.HttpServletRequest;
/**
 * 模块：通用工具
 * 功能：客户端真实 IP 解析（兼容反向代理头）
 */
public final class IpUtils {
    private IpUtils() {}

    public static String getIp(HttpServletRequest request) {
        if (request == null) return null;
        String[] headers = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP"};
        for (String h : headers) {
            String ip = request.getHeader(h);
            if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
                int idx = ip.indexOf(',');
                return idx > 0 ? ip.substring(0, idx).trim() : ip.trim();
            }
        }
        return request.getRemoteAddr();
    }
}
