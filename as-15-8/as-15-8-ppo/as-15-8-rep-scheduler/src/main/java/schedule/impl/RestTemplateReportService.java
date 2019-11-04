package schedule.impl;

import lombok.extern.slf4j.Slf4j;
import model.Report;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import schedule.ReportService;

import javax.transaction.Transactional;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Реализация для создания отчета по http
 * <p>
 * User: asinjavin
 * Date: 08.10.2019
 * Time: 19:49
 */
@Slf4j
@Service
public class RestTemplateReportService implements ReportService
{
    private final ReportStatusService reportStatusService;

    private final RestTemplate restTemplate;

    @Value("${app.birt.url}")
    String birt_url;

    @Autowired
    public RestTemplateReportService(ReportStatusService reportStatusService, RestTemplate restTemplate) {
        this.reportStatusService = reportStatusService;
        this.restTemplate = restTemplate;
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void runReport(Report report) {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

        reportStatusService.markStarted(report);

        try {
            log.info("Запуск отчета " + report);

            String from = dateFormat.format(report.getMsr_prd_st_dttm());
            String period = report.getMsr_prd_tp();
            String format = report.getFormat();
            Object rptdesign = report.getRptdesign();

            String surl = String.format("%s%s/%s?RPFD=%s&RPD=%s", birt_url, rptdesign, format, from, period);

            URL url = new URL(surl);
            log.debug("url = {}", url);

            ResponseEntity<byte[]> response=restTemplate.getForEntity(surl, byte[].class);

            log.debug("response = {}", response.getStatusCode());

            if (response.getStatusCode().isError()) throw new RuntimeException(response.getStatusCode().value() + ": " + response.getStatusCode().getReasonPhrase());

            reportStatusService.markDone(report, response.getHeaders().getContentType().toString(), response.getBody());
            log.info("Отчет {} успешно завершен", report.getRepId() );

        } catch (Throwable e) {
            log.error("Ошибка создания отчета " + report.toString(), e);
            reportStatusService.markFailed(report, e);
        }

    }
}
