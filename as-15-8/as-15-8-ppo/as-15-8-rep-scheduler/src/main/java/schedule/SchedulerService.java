package schedule;

import lombok.extern.java.Log;
import model.Report;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import repositories.ReportRepository;

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
    private final ReportService reportService;
    private final ReportRepository reportRepository;

//    private final ReportsBy reports;

    @Autowired
    public SchedulerService(ReportService reportService, ReportRepository reportRepository) {
        this.reportService = reportService;
        this.reportRepository = reportRepository;
    }


    @Scheduled(cron = "${app.schedule}")
    public void runDaily() {
        log.info("Запуск регламентных отчетов" );
        List<Report> todo = reportRepository.getTodo();
        for (Report report : todo) {
            reportService.runReport(report);
        }
    }



}
