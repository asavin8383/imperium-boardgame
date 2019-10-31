package com.ecl.birt;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.birt.report.engine.api.EngineException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
public class BirtController
{

    @Autowired
    BirtFacade birtFacade;

    @GetMapping(value = "/report/{rep_id}")
    public ResponseEntity<byte[]> createReport(@PathVariable String rep_id, HttpServletRequest params) throws IOException, EngineException, SQLException {

        try(InputStream reportTemplate = getReportInputStream(rep_id)) {
            Map<String, String> paramMap = new HashMap<>();
            for (Map.Entry<String, String[]> entry : params.getParameterMap().entrySet()) {
                paramMap.put(entry.getKey(), entry.getValue()[0]);
            }

            byte[] data = birtFacade.createReport(reportTemplate, paramMap, "pdf");

            return getByteResponse(rep_id+".pdf", data);
        }

    }

    @SuppressWarnings("unused")
    private InputStream getReportInputStream(String rep_id) throws IOException {
        return new ClassPathResource("reports/BuildingReport.rptdesign").getInputStream();
    }

    private ResponseEntity<byte[]> getByteResponse(String filename, byte[] data) {
        MediaType mediaType = MediaType.APPLICATION_PDF;
        HttpHeaders httpHeaders = new HttpHeaders();
        ContentDisposition contentDisposition = ContentDisposition
                .builder("inline")
                .filename(filename, StandardCharsets.UTF_8)
                .build();
        httpHeaders.setContentDisposition(contentDisposition);

        return ResponseEntity
                .ok()
                .headers(httpHeaders)
                .contentType(mediaType)
                .body(data);
    }
}
