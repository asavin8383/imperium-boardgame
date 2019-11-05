package schedule.impl;

import lombok.extern.slf4j.Slf4j;
import model.Report;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import schedule.ReportService;

import javax.transaction.Transactional;
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
            Long period = report.getMsr_prd_tp_id();
            String format = report.getFormat();
            String rptdesign = report.getRptdesign();

            String url = UriComponentsBuilder
                    .fromHttpUrl(birt_url)
                    .path("/reglament/{rptdesign}/{format}")
                    .queryParam("dt", "{from}")
                    .queryParam("prd_tp", "{period}")
                    .buildAndExpand(rptdesign, format, from, period)
                    .toString();

            log.debug("url = {}", url);

            ResponseEntity<byte[]> response=restTemplate.getForEntity(url, byte[].class);

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
