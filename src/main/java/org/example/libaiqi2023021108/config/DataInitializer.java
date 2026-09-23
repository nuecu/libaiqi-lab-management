package org.example.libaiqi2023021108.config;

import org.example.libaiqi2023021108.entity.*;
import org.example.libaiqi2023021108.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final InstrumentRepository instrumentRepository;
    private final ReservationRuleRepository reservationRuleRepository;
    private final ReservationRepository reservationRepository;
    private final ExperimentLogRepository experimentLogRepository;
    private final ExperimentRecordRepository experimentRecordRepository;
    private final MaterialRequisitionRepository materialRequisitionRepository;
    private final MaterialRequisitionItemRepository materialRequisitionItemRepository;
    private final TemporaryRequisitionRepository temporaryRequisitionRepository;
    private final MaterialReturnRepository materialReturnRepository;
    private final InventoryCheckRepository inventoryCheckRepository;
    private final RepairOrderRepository repairOrderRepository;
    private final MaintenancePlanRepository maintenancePlanRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final SafetyAlertRepository safetyAlertRepository;
    private final HazardousMaterialRecordRepository hazardousMaterialRecordRepository;
    private final AnnouncementCategoryRepository announcementCategoryRepository;

    public DataInitializer(
            InstrumentRepository instrumentRepository,
            ReservationRuleRepository reservationRuleRepository,
            ReservationRepository reservationRepository,
            ExperimentLogRepository experimentLogRepository,
            ExperimentRecordRepository experimentRecordRepository,
            MaterialRequisitionRepository materialRequisitionRepository,
            MaterialRequisitionItemRepository materialRequisitionItemRepository,
            TemporaryRequisitionRepository temporaryRequisitionRepository,
            MaterialReturnRepository materialReturnRepository,
            InventoryCheckRepository inventoryCheckRepository,
            RepairOrderRepository repairOrderRepository,
            MaintenancePlanRepository maintenancePlanRepository,
            MaintenanceRecordRepository maintenanceRecordRepository,
            SafetyAlertRepository safetyAlertRepository,
            HazardousMaterialRecordRepository hazardousMaterialRecordRepository,
            AnnouncementCategoryRepository announcementCategoryRepository) {
        this.instrumentRepository = instrumentRepository;
        this.reservationRuleRepository = reservationRuleRepository;
        this.reservationRepository = reservationRepository;
        this.experimentLogRepository = experimentLogRepository;
        this.experimentRecordRepository = experimentRecordRepository;
        this.materialRequisitionRepository = materialRequisitionRepository;
        this.materialRequisitionItemRepository = materialRequisitionItemRepository;
        this.temporaryRequisitionRepository = temporaryRequisitionRepository;
        this.materialReturnRepository = materialReturnRepository;
        this.inventoryCheckRepository = inventoryCheckRepository;
        this.repairOrderRepository = repairOrderRepository;
        this.maintenancePlanRepository = maintenancePlanRepository;
        this.maintenanceRecordRepository = maintenanceRecordRepository;
        this.safetyAlertRepository = safetyAlertRepository;
        this.hazardousMaterialRecordRepository = hazardousMaterialRecordRepository;
        this.announcementCategoryRepository = announcementCategoryRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (instrumentRepository.count() == 0) {
            initInstruments();
            initReservationRules();
            initReservations();
            initExperimentLogs();
            initExperimentRecords();
            initMaterialRequisitions();
            initMaterialRequisitionItems();
            initTemporaryRequisitions();
            initMaterialReturns();
            initInventoryChecks();
            initRepairOrders();
            initMaintenancePlans();
            initMaintenanceRecords();
            initSafetyAlerts();
            initHazardousMaterialRecords();
            System.out.println("===== 15 tables data initialized =====");
        }
        // 公告分类字典独立初始化（对已有库也生效，仅在表为空时写入默认分类）
        if (announcementCategoryRepository.count() == 0) {
            initAnnouncementCategories();
            System.out.println("===== announcement categories initialized =====");
        }
    }

    private void initAnnouncementCategories() {
        createAnnouncementCategory(1L, "通知公告", "NOTICE", "系统通知、安排调整等公告");
        createAnnouncementCategory(2L, "新闻动态", "NEWS", "实验室动态、成果展示等公告");
        createAnnouncementCategory(3L, "政策法规", "POLICY", "管理制度、政策法规等公告");
    }

    private void createAnnouncementCategory(Long id, String name, String code, String desc) {
        AnnouncementCategory c = new AnnouncementCategory();
        c.setId(id); c.setCategoryName(name); c.setCategoryCode(code);
        c.setDescription(desc); c.setStatus("ACTIVE");
        c.setCreateTime(LocalDateTime.now()); c.setUpdateTime(LocalDateTime.now());
        c.setCreateBy("admin");
        announcementCategoryRepository.save(c);
    }

    private void initInstruments() {
        createInstrument(1L,"YQ-001","分析天平",4L,"梅特勒","ME204E","最大称量220g/精度0.1mg",new BigDecimal("15000.00"),LocalDate.of(2023,3,15),"梅特勒-托利多中国",1L,null,null,"IDLE",36);
        createInstrument(2L,"YQ-002","紫外可见分光光度计",2L,"岛津","UV-2600","波长范围190-1100nm",new BigDecimal("85000.00"),LocalDate.of(2022,9,20),"岛津中国有限公司",3L,null,null,"IDLE",24);
        createInstrument(3L,"YQ-003","旋转蒸发仪",5L,null,"RE-52AA","5L防爆型",new BigDecimal("12000.00"),LocalDate.of(2023,6,10),"上海亚荣生化仪器厂",2L,null,null,"IDLE",12);
        createInstrument(4L,"YQ-004","电热鼓风干燥箱",3L,"上海一恒","DHG-9140A","140L最高温度300度",new BigDecimal("6500.00"),LocalDate.of(2023,1,8),"上海一恒科学仪器",1L,null,null,"IDLE",24);
        createInstrument(5L,"YQ-005","高效液相色谱仪",1L,"安捷伦","1260 Infinity II","四元泵/VWD检测器",new BigDecimal("450000.00"),LocalDate.of(2021,11,1),"安捷伦科技中国",4L,5L,null,"IDLE",36);
        createInstrument(6L,"YQ-006","pH计",1L,"梅特勒","FE28-Standard","测量范围0-14pH",new BigDecimal("4500.00"),LocalDate.of(2024,1,15),"梅特勒-托利多中国",1L,null,null,"IDLE",12);
        createInstrument(7L,"YQ-007","马弗炉",3L,"天津泰斯特","SX2-4-10","最高温度1000度4L",new BigDecimal("8000.00"),LocalDate.of(2022,5,20),"天津泰斯特仪器",3L,null,null,"FAULT",24);
        createInstrument(8L,"YQ-008","离心机",5L,"湘仪","H1650-W","最高转速16500rpm",new BigDecimal("28000.00"),LocalDate.of(2023,8,12),"湘仪离心机仪器",5L,null,null,"IDLE",24);
    }

    private void createInstrument(Long id,String code,String name,Long catId,String brand,String model,String spec,BigDecimal price,LocalDate purchaseDate,String supplier,Long labId,Long wsId,String imageUrl,String status,int warranty) {
        Instrument i = new Instrument();
        i.setId(id); i.setInstrumentCode(code); i.setInstrumentName(name);
        i.setCategoryId(catId); i.setBrand(brand); i.setModel(model);
        i.setSpecification(spec); i.setPurchasePrice(price); i.setPurchaseDate(purchaseDate);
        i.setSupplier(supplier); i.setLaboratoryId(labId); i.setWorkstationId(wsId);
        i.setImageUrl(imageUrl); i.setStatus(status); i.setWarrantyPeriodMonths(warranty);
        i.setCreateTime(LocalDateTime.of(2024,9,1,8,0,0));
        i.setUpdateTime(LocalDateTime.of(2024,9,1,8,0,0));
        i.setCreateBy("admin");
        instrumentRepository.save(i);
    }

    private void initReservationRules() {
        createReservationRule(1L,"教学实验室通用规则","LABORATORY",null,"08:00","21:00",7,4,2,"STUDENT",false,1);
        createReservationRule(2L,"科研实验室通用规则","LABORATORY",null,"08:30","20:00",14,8,1,"TEACHER",false,1);
        createReservationRule(3L,"精密仪器预约规则","INSTRUMENT",5L,"09:00","18:00",7,3,1,"TEACHER",false,1);
        createReservationRule(4L,"通用仪器预约规则","INSTRUMENT",null,"08:00","21:00",3,4,2,"STUDENT",true,1);
    }

    private void createReservationRule(Long id,String name,String type,Long resourceId,String start,String end,int advDays,int maxDur,int maxTimes,String minQual,boolean autoApprove,int status) {
        ReservationRule r = new ReservationRule();
        r.setId(id); r.setRuleName(name); r.setResourceType(type);
        r.setResourceId(resourceId); r.setAvailableStartTime(start); r.setAvailableEndTime(end);
        r.setAdvanceDays(advDays); r.setMaxDurationHours(maxDur); r.setMaxTimesPerDay(maxTimes);
        r.setMinQualification(minQual); r.setAutoApprove(autoApprove); r.setStatus(status);
        r.setCreateTime(LocalDateTime.of(2024,9,1,8,0,0));
        r.setUpdateTime(LocalDateTime.of(2024,9,1,8,0,0));
        r.setCreateBy("admin");
        reservationRuleRepository.save(r);
    }

    private void initReservations() {
        createReservation(1L,"RV-20240916-001",4L,"LABORATORY",1L,"无机化学实验室",LocalDate.of(2024,9,16),LocalTime.of(9,0),LocalTime.of(12,0),"无机合成实验-硫酸铜结晶",3,"王同学,赵同学,刘同学","COMPLETED",null,2L,LocalDateTime.of(2024,9,15,14,0),LocalDateTime.of(2024,9,16,8,55),LocalDateTime.of(2024,9,16,12,5),"student1");
        createReservation(2L,"RV-20240916-002",5L,"INSTRUMENT",2L,"紫外可见分光光度计",LocalDate.of(2024,9,16),LocalTime.of(14,0),LocalTime.of(17,0),"样品吸光度测定",1,null,"COMPLETED",null,3L,LocalDateTime.of(2024,9,15,15,0),LocalDateTime.of(2024,9,16,13,50),LocalDateTime.of(2024,9,16,16,50),"student2");
        createReservation(3L,"RV-20240917-001",4L,"LABORATORY",2L,"有机化学实验室",LocalDate.of(2024,9,17),LocalTime.of(9,0),LocalTime.of(12,0),"有机物提取实验",2,"王同学,刘同学","APPROVED",null,2L,LocalDateTime.of(2024,9,16,9,0),null,null,"student1");
        createReservation(4L,"RV-20240917-002",6L,"INSTRUMENT",5L,"高效液相色谱仪",LocalDate.of(2024,9,17),LocalTime.of(10,0),LocalTime.of(13,0),"药物成分分析",1,null,"PENDING",null,null,null,null,null,"student3");
        createReservation(5L,"RV-20240918-001",5L,"LABORATORY",3L,"材料分析实验室",LocalDate.of(2024,9,18),LocalTime.of(9,0),LocalTime.of(17,0),"材料表面形貌分析",2,"赵同学,刘同学","APPROVED",null,3L,LocalDateTime.of(2024,9,17,10,0),null,null,"student2");
        createReservation(6L,"RV-20240915-001",4L,"INSTRUMENT",1L,"分析天平",LocalDate.of(2024,9,15),LocalTime.of(9,0),LocalTime.of(10,0),"试剂称量",1,null,"COMPLETED",null,null,null,LocalDateTime.of(2024,9,15,8,50),LocalDateTime.of(2024,9,15,10,0),"student1");
        createReservation(7L,"RV-20240918-002",6L,"LABORATORY",5L,"综合检测实验室",LocalDate.of(2024,9,18),LocalTime.of(14,0),LocalTime.of(18,0),"水质检测综合实验",4,"刘同学,王同学,赵同学,张同学","REJECTED","该时段已有其他班级预约",2L,LocalDateTime.of(2024,9,17,11,0),null,null,"student3");
        createReservation(8L,"RV-20240919-001",4L,"INSTRUMENT",8L,"离心机",LocalDate.of(2024,9,19),LocalTime.of(9,0),LocalTime.of(11,0),"溶液离心分离",1,null,"PENDING",null,null,null,null,null,"student1");
    }

    private void createReservation(Long id,String no,Long userId,String type,Long resId,String resName,LocalDate date,LocalTime start,LocalTime end,String topic,int partCount,String parts,String status,String rejectReason,Long approverId,LocalDateTime approveTime,LocalDateTime checkIn,LocalDateTime checkOut,String createBy) {
        Reservation r = new Reservation();
        r.setId(id); r.setReservationNo(no); r.setUserId(userId);
        r.setResourceType(type); r.setResourceId(resId); r.setResourceName(resName);
        r.setReservationDate(date); r.setStartTime(start); r.setEndTime(end);
        r.setExperimentTopic(topic); r.setParticipantCount(partCount); r.setParticipants(parts);
        r.setStatus(status); r.setRejectReason(rejectReason); r.setApproverId(approverId);
        r.setApproveTime(approveTime); r.setCheckInTime(checkIn); r.setCheckOutTime(checkOut);
        r.setCreateTime(LocalDateTime.now()); r.setUpdateTime(LocalDateTime.now());
        r.setCreateBy(createBy);
        reservationRepository.save(r);
    }

    private void initExperimentLogs() {
        createExperimentLog(1L,1L,4L,1L,"无机合成实验-硫酸铜结晶",LocalDateTime.of(2024,9,16,9,0),LocalDateTime.of(2024,9,16,12,0),185,"COMPLETED","QR-EXP-001","student1");
        createExperimentLog(2L,2L,5L,3L,"样品吸光度测定",LocalDateTime.of(2024,9,16,14,0),LocalDateTime.of(2024,9,16,17,0),180,"COMPLETED","QR-EXP-002","student2");
        createExperimentLog(3L,6L,4L,1L,"试剂称量",LocalDateTime.of(2024,9,15,9,0),LocalDateTime.of(2024,9,15,10,0),70,"COMPLETED","QR-EXP-003","student1");
        createExperimentLog(4L,3L,4L,2L,"有机物提取实验",LocalDateTime.of(2024,9,17,9,0),LocalDateTime.of(2024,9,17,12,0),null,"IN_PROGRESS","QR-EXP-004","student1");
        createExperimentLog(5L,5L,5L,3L,"材料表面形貌分析",LocalDateTime.of(2024,9,18,9,0),LocalDateTime.of(2024,9,18,17,0),null,"IN_PROGRESS","QR-EXP-005","student2");
    }

    private void createExperimentLog(Long id,Long resId,Long userId,Long labId,String topic,LocalDateTime start,LocalDateTime end,Integer dur,String status,String qr,String createBy) {
        ExperimentLog e = new ExperimentLog();
        e.setId(id); e.setReservationId(resId); e.setUserId(userId);
        e.setLaboratoryId(labId); e.setExperimentTopic(topic);
        e.setStartTime(start); e.setEndTime(end); e.setActualDurationMinutes(dur);
        e.setStatus(status); e.setQrCode(qr);
        e.setCreateTime(LocalDateTime.now()); e.setUpdateTime(LocalDateTime.now());
        e.setCreateBy(createBy);
        experimentLogRepository.save(e);
    }

    private void initExperimentRecords() {
        createExperimentRecord(1L,1L,4L,1L,"完成硫酸铜结晶实验,得到蓝色晶体约5.2g,产率78%","NORMAL",null);
        createExperimentRecord(2L,2L,5L,2L,"测定5组样品吸光度,数据记录完整,R2=0.998","NORMAL",null);
        createExperimentRecord(3L,3L,4L,1L,"称取NaCl 5.00g,CuSO4 5H2O 12.50g","NORMAL",null);
    }

    private void createExperimentRecord(Long id,Long logId,Long userId,Long instId,String content,String instStatus,String abnormalDesc) {
        ExperimentRecord r = new ExperimentRecord();
        r.setId(id); r.setExperimentLogId(logId); r.setUserId(userId);
        r.setInstrumentId(instId); r.setRecordContent(content);
        r.setInstrumentStatus(instStatus); r.setAbnormalDesc(abnormalDesc);
        r.setCreateTime(LocalDateTime.now()); r.setUpdateTime(LocalDateTime.now());
        experimentRecordRepository.save(r);
    }

    private void initMaterialRequisitions() {
        createMaterialRequisition(1L,"MR-20240916-001",4L,1L,"无机合成实验-硫酸铜结晶","ISSUED",2L,LocalDateTime.of(2024,9,15,15,0),3,"student1");
        createMaterialRequisition(2L,"MR-20240916-002",5L,2L,"样品吸光度测定","ISSUED",3L,LocalDateTime.of(2024,9,15,16,0),2,"student2");
        createMaterialRequisition(3L,"MR-20240917-001",4L,4L,"有机物提取实验","APPROVED",2L,LocalDateTime.of(2024,9,16,17,0),2,"student1");
        createMaterialRequisition(4L,"MR-20240918-001",6L,null,"水质检测综合实验","PENDING",null,null,3,"student3");
    }

    private void createMaterialRequisition(Long id,String no,Long userId,Long logId,String project,String status,Long approverId,LocalDateTime approveTime,int totalItems,String createBy) {
        MaterialRequisition m = new MaterialRequisition();
        m.setId(id); m.setRequisitionNo(no); m.setUserId(userId);
        m.setExperimentLogId(logId); m.setExperimentProject(project);
        m.setStatus(status); m.setApproverId(approverId); m.setApproveTime(approveTime);
        m.setTotalItems(totalItems);
        m.setCreateTime(LocalDateTime.now()); m.setUpdateTime(LocalDateTime.now());
        m.setCreateBy(createBy);
        materialRequisitionRepository.save(m);
    }

    private void initMaterialRequisitionItems() {
        createMaterialRequisitionItem(1L,1L,7L,"硫酸铜",20,20,"g");
        createMaterialRequisitionItem(2L,1L,3L,"氢氧化钠",10,10,"g");
        createMaterialRequisitionItem(3L,1L,4L,"烧杯100mL",2,2,"个");
        createMaterialRequisitionItem(4L,2L,10L,"NaCl氯化钠",50,50,"g");
        createMaterialRequisitionItem(5L,2L,6L,"滤纸中速",1,1,"盒");
        createMaterialRequisitionItem(6L,3L,1L,"无水乙醇",100,100,"mL");
        createMaterialRequisitionItem(7L,3L,8L,"丙酮",50,0,"mL");
        createMaterialRequisitionItem(8L,4L,2L,"盐酸",30,null,"mL");
        createMaterialRequisitionItem(9L,4L,3L,"氢氧化钠",20,null,"g");
        createMaterialRequisitionItem(10L,4L,9L,"一次性手套",1,null,"盒");
    }

    private void createMaterialRequisitionItem(Long id,Long reqId,Long consId,String name,int reqQty,Integer actQty,String unit) {
        MaterialRequisitionItem item = new MaterialRequisitionItem();
        item.setId(id); item.setRequisitionId(reqId); item.setConsumableId(consId);
        item.setConsumableName(name); item.setRequestQuantity(reqQty);
        item.setActualQuantity(actQty); item.setUnit(unit);
        materialRequisitionItemRepository.save(item);
    }

    private void initTemporaryRequisitions() {
        createTemporaryRequisition(1L,"TR-20240916-001",4L,4L,"烧杯100mL",3,"实验过程中额外需要","APPROVED",2L,LocalDateTime.of(2024,9,16,10,0),"student1");
        createTemporaryRequisition(2L,"TR-20240917-001",5L,9L,"一次性手套",2,"实验手套损耗","APPROVED",3L,LocalDateTime.of(2024,9,17,11,0),"student2");
    }

    private void createTemporaryRequisition(Long id,String no,Long userId,Long consId,String name,int qty,String reason,String status,Long approverId,LocalDateTime approveTime,String createBy) {
        TemporaryRequisition t = new TemporaryRequisition();
        t.setId(id); t.setRequisitionNo(no); t.setUserId(userId);
        t.setConsumableId(consId); t.setConsumableName(name);
        t.setQuantity(qty); t.setReason(reason); t.setStatus(status);
        t.setApproverId(approverId); t.setApproveTime(approveTime);
        t.setCreateTime(LocalDateTime.now()); t.setUpdateTime(LocalDateTime.now());
        t.setCreateBy(createBy);
        temporaryRequisitionRepository.save(t);
    }

    private void initMaterialReturns() {
        createMaterialReturn(1L,"RT-20240916-001",4L,7L,"硫酸铜",5,"REMAINING","COMPLETED",2L);
        createMaterialReturn(2L,"RT-20240916-002",5L,10L,"NaCl氯化钠",15,"REMAINING","COMPLETED",3L);
    }

    private void createMaterialReturn(Long id,String no,Long userId,Long consId,String name,int qty,String type,String status,Long receiverId) {
        MaterialReturn r = new MaterialReturn();
        r.setId(id); r.setReturnNo(no); r.setUserId(userId);
        r.setConsumableId(consId); r.setConsumableName(name);
        r.setReturnQuantity(qty); r.setReturnType(type); r.setStatus(status);
        r.setReceiverId(receiverId);
        r.setCreateTime(LocalDateTime.now()); r.setUpdateTime(LocalDateTime.now());
        r.setCreateBy("student"+userId);
        materialReturnRepository.save(r);
    }

    private void initInventoryChecks() {
        createInventoryCheck(1L,"IC-20240920-001","9月化学试剂盘点",LocalDate.of(2024,9,20),2L,1L,120,118,-2,"实验损耗误差","COMPLETED");
        createInventoryCheck(2L,"IC-20240920-002","9月化学试剂盘点",LocalDate.of(2024,9,20),2L,3L,95,95,0,null,"COMPLETED");
        createInventoryCheck(3L,"IC-20240920-003","9月玻璃器皿盘点",LocalDate.of(2024,9,20),2L,4L,200,197,-3,"实验中破损3个","COMPLETED");
        createInventoryCheck(4L,"IC-20240920-004","9月危化品盘点",LocalDate.of(2024,9,21),1L,2L,80,79,-1,"挥发损耗","COMPLETED");
    }

    private void createInventoryCheck(Long id,String no,String name,LocalDate date,Long checkerId,Long consId,int sysQty,int actQty,int diff,String reason,String status) {
        InventoryCheck c = new InventoryCheck();
        c.setId(id); c.setCheckNo(no); c.setCheckName(name); c.setCheckDate(date);
        c.setCheckerId(checkerId); c.setConsumableId(consId);
        c.setSystemQuantity(sysQty); c.setActualQuantity(actQty);
        c.setDifference(diff); c.setLossReason(reason); c.setStatus(status);
        c.setCreateTime(LocalDateTime.now()); c.setUpdateTime(LocalDateTime.now());
        c.setCreateBy("admin");
        inventoryCheckRepository.save(c);
    }

    private void initRepairOrders() {
        createRepairOrder(1L,"RP-20240910-001",7L,"马弗炉",3L,"马弗炉加热丝断裂,无法升温至设定温度,显示屏报E03错误代码",7L,"更换加热丝和温控模块,重新校准温度传感器","已修复,温度控制精度恢复到正负2度",new BigDecimal("1200.00"),"COMPLETED",LocalDateTime.of(2024,9,12,16,0),"teacher2");
        createRepairOrder(2L,"RP-20240918-001",3L,"旋转蒸发仪",2L,"真空泵密封不良,出现漏气现象,真空度无法维持",7L,null,null,null,"PENDING",null,"teacher1");
        createRepairOrder(3L,"RP-20240915-001",6L,"pH计",4L,"pH计读数不稳定,波动范围超过0.5",7L,"更换电极,重新校准","恢复正常使用",new BigDecimal("350.00"),"COMPLETED",LocalDateTime.of(2024,9,16,11,0),"student1");
    }

    private void createRepairOrder(Long id,String no,Long instId,String instName,Long reporterId,String faultDesc,Long assigneeId,String repairDesc,String repairResult,BigDecimal cost,String status,LocalDateTime completeTime,String createBy) {
        RepairOrder r = new RepairOrder();
        r.setId(id); r.setOrderNo(no); r.setInstrumentId(instId);
        r.setInstrumentName(instName); r.setReporterId(reporterId);
        r.setFaultDesc(faultDesc); r.setAssigneeId(assigneeId);
        r.setRepairDesc(repairDesc); r.setRepairResult(repairResult);
        r.setRepairCost(cost); r.setStatus(status); r.setCompleteTime(completeTime);
        r.setCreateTime(LocalDateTime.now()); r.setUpdateTime(LocalDateTime.now());
        r.setCreateBy(createBy);
        repairOrderRepository.save(r);
    }

    private void initMaintenancePlans() {
        createMaintenancePlan(1L,"分析天平季度保养",1L,"QUARTERLY",90,LocalDate.of(2024,6,15),LocalDate.of(2024,9,15),7L,"ACTIVE");
        createMaintenancePlan(2L,"液相色谱仪月度保养",5L,"MONTHLY",30,LocalDate.of(2024,8,20),LocalDate.of(2024,9,20),7L,"ACTIVE");
        createMaintenancePlan(3L,"分光光度计半年保养",2L,"YEARLY",180,LocalDate.of(2024,3,10),LocalDate.of(2024,9,10),7L,"ACTIVE");
        createMaintenancePlan(4L,"干燥箱季度保养",4L,"QUARTERLY",90,LocalDate.of(2024,7,1),LocalDate.of(2024,10,1),7L,"ACTIVE");
        createMaintenancePlan(5L,"离心机月度保养",8L,"MONTHLY",30,LocalDate.of(2024,8,25),LocalDate.of(2024,9,25),7L,"ACTIVE");
    }

    private void createMaintenancePlan(Long id,String name,Long instId,String type,int cycleDays,LocalDate lastDate,LocalDate nextDate,Long responsibleId,String status) {
        MaintenancePlan p = new MaintenancePlan();
        p.setId(id); p.setPlanName(name); p.setInstrumentId(instId);
        p.setMaintenanceType(type); p.setCycleDays(cycleDays);
        p.setLastMaintenanceDate(lastDate); p.setNextMaintenanceDate(nextDate);
        p.setResponsibleId(responsibleId); p.setStatus(status);
        p.setCreateTime(LocalDateTime.of(2024,9,1,8,0,0));
        p.setUpdateTime(LocalDateTime.of(2024,9,1,8,0,0));
        p.setCreateBy("admin");
        maintenancePlanRepository.save(p);
    }

    private void initMaintenanceRecords() {
        createMaintenanceRecord(1L,1L,1L,LocalDate.of(2024,6,15),7L,"清洁称量盘,校准零点,检查水平泡,测试线性","各项指标正常,通过校准",LocalDate.of(2024,9,15));
        createMaintenanceRecord(2L,2L,5L,LocalDate.of(2024,8,20),7L,"更换流动相滤芯,清洗管路,检查泵密封,运行系统适用性测试","系统适用性测试通过,基线平稳",LocalDate.of(2024,9,20));
        createMaintenanceRecord(3L,3L,2L,LocalDate.of(2024,3,10),7L,"更换氘灯,校准波长,清洁光学元件,检查杂散光","氘灯能量正常,波长精度正负0.5nm",LocalDate.of(2024,9,10));
        createMaintenanceRecord(4L,1L,1L,LocalDate.of(2024,9,15),7L,"季度保养:清洁,校准,检查防风罩,测试重复性","校准通过,重复性RSD小于0.05%",LocalDate.of(2024,12,15));
    }

    private void createMaintenanceRecord(Long id,Long planId,Long instId,LocalDate date,Long maintainerId,String content,String result,LocalDate nextDate) {
        MaintenanceRecord r = new MaintenanceRecord();
        r.setId(id); r.setPlanId(planId); r.setInstrumentId(instId);
        r.setMaintenanceDate(date); r.setMaintainerId(maintainerId);
        r.setMaintenanceContent(content); r.setResult(result);
        r.setNextMaintenanceDate(nextDate);
        r.setCreateTime(LocalDateTime.now()); r.setUpdateTime(LocalDateTime.now());
        r.setCreateBy("maintainer1");
        maintenanceRecordRepository.save(r);
    }

    private void initSafetyAlerts() {
        createSafetyAlert(1L,"INVENTORY_LOW","MEDIUM","耗材丙酮(HC-008)当前库存35瓶,低于安全库存阈值(10瓶),请及时补充",8L,"CONSUMABLE","ACTIVE",null,null);
        createSafetyAlert(2L,"INSTRUMENT_OVERTIME","HIGH","马弗炉(YQ-007)持续故障超过7天未修复,请尽快安排维修",7L,"INSTRUMENT","RESOLVED",LocalDateTime.of(2024,9,12,16,0),7L);
        createSafetyAlert(3L,"HAZARDOUS_OVERUSE","HIGH","危化品无水乙醇本周领用量达到100mL,超过单人周限额,需额外审批",1L,"CONSUMABLE","ACKNOWLEDGED",null,null);
        createSafetyAlert(4L,"NO_SHOW","LOW","预约RV-20240918-002(综合检测实验室)未按时签到,已自动取消",7L,"RESERVATION","ACTIVE",null,null);
        createSafetyAlert(5L,"INVENTORY_LOW","LOW","耗材滤纸中速(HC-006)当前库存60盒,接近安全库存阈值(10盒)",6L,"CONSUMABLE","ACTIVE",null,null);
    }

    private void createSafetyAlert(Long id,String type,String level,String content,Long relatedId,String relatedType,String status,LocalDateTime resolveTime,Long resolverId) {
        SafetyAlert a = new SafetyAlert();
        a.setId(id); a.setAlertType(type); a.setAlertLevel(level);
        a.setAlertContent(content); a.setRelatedId(relatedId);
        a.setRelatedType(relatedType); a.setStatus(status);
        a.setResolveTime(resolveTime); a.setResolverId(resolverId);
        a.setCreateTime(LocalDateTime.now()); a.setUpdateTime(LocalDateTime.now());
        a.setCreateBy("system");
        safetyAlertRepository.save(a);
    }

    private void initHazardousMaterialRecords() {
        createHazardousMaterialRecord(1L,1L,"无水乙醇",4L,"ISSUE",100,true,2L,4L,true,"用于有机物提取实验,使用80mL,剩余20mL已归还");
        createHazardousMaterialRecord(2L,8L,"丙酮",5L,"ISSUE",50,true,3L,5L,true,"用于样品清洗,使用45mL,废液已收集处理");
        createHazardousMaterialRecord(3L,2L,"盐酸",4L,"ISSUE",30,true,2L,4L,true,"用于酸碱滴定实验,使用25mL");
        createHazardousMaterialRecord(4L,1L,"无水乙醇",4L,"RETURN",20,true,2L,4L,true,"实验剩余归还");
        createHazardousMaterialRecord(5L,8L,"丙酮",5L,"DISPOSE",5,true,3L,7L,true,"废液处理,交由有资质单位处置");
    }

    private void createHazardousMaterialRecord(Long id,Long consId,String consName,Long userId,String opType,int qty,boolean dualConfirm,Long confirm1,Long confirm2,boolean qualVerified,String tracking) {
        HazardousMaterialRecord r = new HazardousMaterialRecord();
        r.setId(id); r.setConsumableId(consId); r.setConsumableName(consName);
        r.setUserId(userId); r.setOperationType(opType); r.setQuantity(qty);
        r.setDualConfirm(dualConfirm); r.setConfirmUser1Id(confirm1);
        r.setConfirmUser2Id(confirm2); r.setQualificationVerified(qualVerified);
        r.setUsageTracking(tracking);
        r.setCreateTime(LocalDateTime.now()); r.setUpdateTime(LocalDateTime.now());
        r.setCreateBy("student"+userId);
        hazardousMaterialRecordRepository.save(r);
    }
}
