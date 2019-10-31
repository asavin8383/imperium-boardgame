package com.ecl.impl;

import com.ecl.ReportPeriod;
import com.ecl.ReportService;
import lombok.extern.java.Log;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;

/**
 * Реализация для создания отчета по http
 *
 * User: asinjavin
 * Date: 08.10.2019
 * Time: 19:49
 */
@Log
@Service
public class OkReportService implements ReportService
{

    private OkHttpClient client = new OkHttpClient();

    @Value("${app.birt}")
    String birt_url;

    public byte[] runReport(String report, ReportPeriod reportPeriod, String format) {
        try {
            log.info("Запуск отчета " + report + " " + reportPeriod);
            String surl = String.format("%s%s/%s?RPFD=%s&RPD=%s", birt_url, report, format, reportPeriod.getFrom(), reportPeriod.getName());
            URL url = new URL(surl);
            System.out.println("url = " + url);
            Request request = new Request.Builder().url(url).build();

            Call call = client.newCall(request);
            Response response = call.execute();
            System.out.println("response = " + response);

            if (response.isSuccessful())
                return response.body().bytes();
            else
                throw new RuntimeException(response.code() + ": " + response.message());

        } catch (Throwable e) {
            e.printStackTrace();
        }

        return null;
    }
}
