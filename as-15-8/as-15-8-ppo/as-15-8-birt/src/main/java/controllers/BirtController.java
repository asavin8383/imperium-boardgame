package controllers;

import birt.BirtFacade;
import enums.ReportType;
import lombok.extern.apachecommons.CommonsLog;
import org.eclipse.birt.report.engine.api.EngineException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@RestController
@CommonsLog
@SuppressWarnings("unused")
public class BirtController
{

    private BirtFacade birtFacade;

    private MimetypesFileTypeMap fileTypeMap;

    @Autowired
    public BirtController(BirtFacade birtFacade) {
        this.birtFacade = birtFacade;
        this.fileTypeMap = new MimetypesFileTypeMap();
    }

    @PreAuthorize("hasAnyRole('ROLE_SYSTEM')")
    @GetMapping(value = "/reglament/{reportName}/{format}")
    public ResponseEntity<?> createReport(HttpServletRequest params,
                                          @PathVariable String reportName,
                                          @PathVariable ReportType format)
            throws IOException, EngineException, SQLException {

        reportName = reportName.replaceAll("[*?/\\\\]", "");//Для безопасности

        String resource = String.format("reports/%s.rptdesign", reportName);
        log.info("Requested " + resource);

        try(InputStream reportTemplate = new ClassPathResource(resource).getInputStream()) {

            Map<String, String> paramMap = getReportParams(params);
            byte[] data = birtFacade.createReport(reportTemplate, paramMap, format.name());
            String extension = getExtension(format);

            log.debug("Started " + reportName);
            ResponseEntity<byte[]> byteResponse = getByteResponse(reportName + "." + extension, data);
            log.info("Created " + reportName);
            return byteResponse;
        }

    }

    private String getExtension(ReportType format) {
        return format == ReportType.xls ? "xml" :
               format == ReportType.ppt ? "mht" :
               format.name();
    }

    private Map<String, String> getReportParams(HttpServletRequest params) {
        Map<String, String> paramMap = new HashMap<>();
        for (Map.Entry<String, String[]> entry : params.getParameterMap().entrySet()) {
            if ( !entry.getKey().equals("format") ) {
                log.info("PARAM '" + entry.getKey() + "' = >" + entry.getValue()[0] + "<");
                paramMap.put(entry.getKey(), entry.getValue()[0]);
            }
        }
        return paramMap;
    }

    private InputStream getReportInputStream(String reportId, ReportType reportType) throws IOException {
        String resource;
        if (reportType == ReportType.pdf)
            resource = "reports/BuildingReportPdf.rptdesign";
        else if (reportType == ReportType.xls || reportType == ReportType.xlsx)
            resource = "reports/BuildingReportXls.rptdesign";
        else
            throw new IllegalArgumentException("Unsupported report type " + reportType.name());

        return new ClassPathResource(resource).getInputStream();
    }

    private ResponseEntity<byte[]> getByteResponse(String filename, byte[] data) {
        String type = fileTypeMap.getContentType(filename);
        return getByteResponse(MediaType.parseMediaType(type), filename, data);
    }

    private ResponseEntity<byte[]> getByteResponse(MediaType mediaType, String filename, byte[] data) {
        HttpHeaders httpHeaders = new HttpHeaders();
        String type = MediaType.APPLICATION_PDF.equals(mediaType) ?
                "inline" : "attachment";
        ContentDisposition contentDisposition = ContentDisposition
                .builder(type)
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
