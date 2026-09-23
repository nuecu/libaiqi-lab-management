package org.example.libaiqi2023021108.service;

import org.example.libaiqi2023021108.entity.Reservation;
import org.example.libaiqi2023021108.entity.SafetyAlert;
import org.example.libaiqi2023021108.repository.ReservationRepository;
import org.example.libaiqi2023021108.repository.SafetyAlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 模块：预约管理
 * 功能：预约违约自动处理定时任务
 * 说明：周期性扫描「今天已过结束时间但仍处于已审批(APPROVED)未签到」的预约，
 *       自动置为已取消（并反向同步实验记录），同时生成一条 NO_SHOW 安全预警。
 *       只处理当天数据，历史种子数据不受影响。
 */
@Component
public class ReservationScheduledService {

    private static final Logger log = LoggerFactory.getLogger(ReservationScheduledService.class);

    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;
    private final SafetyAlertRepository safetyAlertRepository;

    public ReservationScheduledService(ReservationRepository reservationRepository,
                                       ReservationService reservationService,
                                       SafetyAlertRepository safetyAlertRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationService = reservationService;
        this.safetyAlertRepository = safetyAlertRepository;
    }

    /**
     * 默认每 5 分钟扫描一次（启动后延迟 1 分钟首跑，避免与数据初始化竞争）。
     * 间隔可通过 app.scheduling.no-show-ms 配置覆盖。
     */
    @Scheduled(initialDelayString = "${app.scheduling.no-show-initial-delay:60000}",
               fixedDelayString = "${app.scheduling.no-show-ms:300000}")
    public void autoCancelNoShow() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        List<Reservation> noShows = reservationRepository.findNoShowToday(today, now);
        log.info("[违约扫描] date={} time={} 命中未签到已审批预约数={}", today, now, noShows.size());
        if (noShows.isEmpty()) return;
        for (Reservation r : noShows) {
            try {
                Reservation cancelled = reservationService.markNoShow(r.getId());
                if (cancelled == null) continue; // 已被其它流程处理，跳过
                createNoShowAlertIfAbsent(cancelled);
                log.info("违约自动取消预约 id={} no={} 资源={}", cancelled.getId(),
                        cancelled.getReservationNo(), cancelled.getResourceName());
            } catch (Exception e) {
                log.warn("违约自动取消失败 id={}: {}", r.getId(), e.getMessage());
            }
        }
    }

    /** 为一条被违约取消的预约生成 NO_SHOW 预警（去重：同一预约只生成一次） */
    private void createNoShowAlertIfAbsent(Reservation r) {
        if (safetyAlertRepository.existsByAlertTypeAndRelatedTypeAndRelatedId("NO_SHOW", "RESERVATION", r.getId())) {
            return;
        }
        SafetyAlert alert = new SafetyAlert();
        alert.setAlertType("NO_SHOW");
        alert.setAlertLevel("LOW");
        alert.setAlertContent("预约[" + r.getReservationNo() + "] 主题《" + r.getExperimentTopic()
                + "》于 " + r.getReservationDate() + " " + r.getStartTime() + "-" + r.getEndTime()
                + " 时段结束仍未签到，系统已自动取消。");
        alert.setRelatedId(r.getId());
        alert.setRelatedType("RESERVATION");
        alert.setStatus("ACTIVE");
        alert.setCreateBy("system");
        safetyAlertRepository.save(alert);
    }
}
