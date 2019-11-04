package schedule;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Service
public class SchedulerService
{
    private final QueryService queryService;
    private final ReportService reportService;
    private final ReportRepository reportRepository;


    @Autowired
    public SchedulerService(QueryService queryService, ReportService reportService, ReportRepository reportRepository) {
        this.queryService = queryService;
        this.reportService = reportService;
        this.reportRepository = reportRepository;
    }


    @Scheduled(cron = "${app.schedule}")
    public void runDaily() {
        log.info("Запуск регламентных отчетов" );
        List<Report> todo = reportRepository.getTodo();
        queryService.beforeAll();
        for (Report report : todo) {
            queryService.beforeEach(report.getRepId());
            reportService.runReport(report);
        }
    }



}
