package schedule.impl;

import lombok.extern.slf4j.Slf4j;
import model.Report;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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
public class OkReportService implements ReportService
{
    private final ReportStatusService reportStatusService;


    private OkHttpClient client = new OkHttpClient();

    @Value("${app.birt.url}")
    String birt_url;

    @Autowired
    public OkReportService(ReportStatusService reportStatusService) {
        this.reportStatusService = reportStatusService;
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
            Request request = new Request.Builder().url(url).build();

            Call call = client.newCall(request);
            Response response = call.execute();
            log.debug("response = {}", response);

            if (!response.isSuccessful()) throw new RuntimeException(response.code() + ": " + response.message());

            //noinspection ConstantConditions
            reportStatusService.markDone(report, response.header("Content-Type"), response.body().bytes());

        } catch (Throwable e) {
            log.error(report.toString(), e);
            reportStatusService.markFailed(report, e);
        }

    }
}
