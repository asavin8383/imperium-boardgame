package com.ecl;

import com.ecl.impl.OkReportService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;

/**
 * Центральный класс сервиса.
 * Методы класса вызываются по расписанию и запускают создание отчетов.
 *
 * User: asinjavin
 * Date: 08.10.2019
 * Time: 16:16
 */
@Log
@Service
public class SchedulerService
{
    private final OkReportService reportService;
    private final DateTimePeriodUtil dateTimePeriodUtil;
    private final ReportSaver reportSaver;

    private final ReportsBy reports;

    @Autowired
    public SchedulerService(ReportsBy reports, OkReportService reportService, DateTimePeriodUtil dateTimePeriodUtil, ReportSaver reportSaver) {
        this.reports = reports;
        this.reportService = reportService;
        this.dateTimePeriodUtil = dateTimePeriodUtil;
        this.reportSaver = reportSaver;
    }


    @PostConstruct
    void init() {
        log.info("Дневные отчеты: " + reports.day);
        log.info("Недельные отчеты: " + reports.week);
        log.info("Месячные отчеты: " + reports.month);
        log.info("Квартальные отчеты: " + reports.quarter);
        log.info("Полугодовые отчеты: " + reports.half);
        log.info("Годовые отчеты: " + reports.year);
    }

    @Scheduled(cron = "${app.schedule.day}")
    public void runDaily() {
        log.info("Запуск дневных отчетов" );
        runReports(reports.day, dateTimePeriodUtil.getLastDay(new Date()));
    }

    @Scheduled(cron = "${app.schedule.week}")
    public void runWeekly() {
        log.info("Запуск недельных отчетов" );
        runReports(reports.week, dateTimePeriodUtil.getLastWeek(new Date()));
    }

    @Scheduled(cron = "${app.schedule.month}")
    public void runMonthly() {
        log.info("Запуск месячных отчетов" );
        runReports(reports.month, dateTimePeriodUtil.getLastMonth(new Date()));
    }

    @Scheduled(cron = "${app.schedule.quarter}")
    public void runQuarterly() {
        log.info("Запуск квартальных отчетов" );
        runReports(reports.quarter, dateTimePeriodUtil.getLastQuarter(new Date()));
    }

    @Scheduled(cron = "${app.schedule.half}")
    public void runHalfAYear() {
        Date today = new Date();
        if (dateTimePeriodUtil.isHalfStart(today)) {
            log.info("Запуск полугодовых отчетов");
        }
    }

    @Scheduled(cron = "${app.schedule.year}")
    public void runYearly() {
        log.info("Запуск годовых отчетов" );
        runReports(reports.year, dateTimePeriodUtil.getLastYear(new Date()));
    }

    private void runReports(List<String> reports, ReportPeriod reportPeriod) {
        if (reports != null) {
            for (String report : reports) {
                byte[] data = reportService.runReport(report, reportPeriod, "docx");
                reportSaver.saveReport(report, data, reportPeriod, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            }
        }
    }


}
