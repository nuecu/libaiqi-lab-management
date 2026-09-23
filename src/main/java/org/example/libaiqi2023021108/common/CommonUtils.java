package org.example.libaiqi2023021108.common;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 通用工具类
 * 功能：提取项目中重复使用的公共逻辑，包括订单号生成、用户显示名获取等
 */
public class CommonUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private CommonUtils() {
    }

    /**
     * 生成业务订单号
     * 格式：前缀 + 日期(yyyyMMdd) + 4位随机大写字母
     *
     * @param prefix 订单号前缀，如 "RP"、"MR"、"RSV"
     * @return 生成的订单号
     */
    public static String generateOrderNo(String prefix) {
        return prefix + LocalDate.now().format(DATE_FORMATTER)
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    /**
     * 生成业务订单号（指定随机串长度）
     *
     * @param prefix      订单号前缀
     * @param randomLength 随机串长度
     * @return 生成的订单号
     */
    public static String generateOrderNo(String prefix, int randomLength) {
        return prefix + LocalDate.now().format(DATE_FORMATTER)
                + UUID.randomUUID().toString().substring(0, randomLength).toUpperCase();
    }
}
