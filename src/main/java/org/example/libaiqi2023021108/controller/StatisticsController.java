package org.example.libaiqi2023021108.controller;

import org.example.libaiqi2023021108.common.Result;
import org.example.libaiqi2023021108.common.SecurityUtils;
import org.example.libaiqi2023021108.service.StatisticsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
/**
 * 模块：数据统计
 * 功能：数据统计
 * 权限：全局概览类统计需要 statistics:overview；全校趋势分析需要 statistics:trend；
 *      个人统计(/my-overview、/my-trend)仅需登录(ROLE_AUTHENTICATED)，且按当前用户 userId 隔离，任何角色只能查看本人数据。
 */

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {
    private final StatisticsService statisticsService;
    public StatisticsController(StatisticsService statisticsService) { this.statisticsService = statisticsService; }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('statistics:overview')")
    public Result<Map<String, Object>> dashboard() { return Result.success(statisticsService.getDashboardStats()); }

    @GetMapping("/consumables")
    @PreAuthorize("hasAuthority('statistics:overview')")
    public Result<Map<String, Object>> consumableStats() { return Result.success(statisticsService.getConsumableStats()); }

    @GetMapping("/instruments")
    @PreAuthorize("hasAuthority('statistics:overview')")
    public Result<Map<String, Object>> instrumentStats() { return Result.success(statisticsService.getInstrumentStats()); }

    @GetMapping("/reservations")
    @PreAuthorize("hasAuthority('statistics:overview')")
    public Result<Map<String, Object>> reservationStats() { return Result.success(statisticsService.getReservationStats()); }

    @GetMapping("/reservation-trend")
    @PreAuthorize("hasAuthority('statistics:trend')")
    public Result<List<Map<String, Object>>> reservationTrend() { return Result.success(statisticsService.getReservationTrend()); }

    @GetMapping("/trend-analysis")
    @PreAuthorize("hasAuthority('statistics:trend')")
    public Result<Map<String, Object>> trendAnalysis(@RequestParam(defaultValue = "month") String range) {
        return Result.success(statisticsService.getTrendAnalysis(range));
    }

    @GetMapping("/my-overview")
    @PreAuthorize("hasAuthority('ROLE_AUTHENTICATED')")
    public Result<Map<String, Object>> myOverview() { return Result.success(statisticsService.getMyOverview(SecurityUtils.currentUserId())); }

    @GetMapping("/my-trend")
    @PreAuthorize("hasAuthority('ROLE_AUTHENTICATED')")
    public Result<List<Map<String, Object>>> myTrend() { return Result.success(statisticsService.getMyTrend(SecurityUtils.currentUserId())); }
}
