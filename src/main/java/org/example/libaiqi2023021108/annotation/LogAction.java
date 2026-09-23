package org.example.libaiqi2023021108.annotation;

import java.lang.annotation.*;
/**
 * 模块：系统管理
 * 功能：操作日志标记注解，标注在控制器写操作方法上，用于自定义日志中的操作名称
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogAction {
    /** 操作名称，如 "取消预约"；留空则按 HTTP 方法自动归类 */
    String value() default "";
}
