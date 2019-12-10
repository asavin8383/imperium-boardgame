package servlet;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.SneakyThrows;
import model.MsrPrdTp;
import model.Report;
import model.soap.Envelope;
import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.io.output.TeeOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import repositories.MsrPrdTpRepository;
import repositories.ReportRepository;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * User: asinjavin
 * Date: 21.11.2019
 * Time: 15:41
 */
@Component("DBSaverFilter")
public class DBSaverFilter implements javax.servlet.Filter
{

    private final ReportRepository reportRepository;
    private final MsrPrdTpRepository msrPrdTpRepository;

    private final Map<String, Map<String, String>> requestMap;

    @Autowired
    public DBSaverFilter(ReportRepository reportRepository, MsrPrdTpRepository msrPrdTpRepository, Map<String, Map<String, String>> requestMap) {
        this.reportRepository = reportRepository;
        this.msrPrdTpRepository = msrPrdTpRepository;
        this.requestMap = requestMap;
    }

    public void destroy() {
    }

    @SneakyThrows
    public void doFilter(javax.servlet.ServletRequest req, javax.servlet.ServletResponse resp, javax.servlet.FilterChain chain) throws javax.servlet.ServletException, IOException {
        if (req instanceof HttpServletRequest && resp instanceof HttpServletResponse) {
            HttpServletRequest request = (HttpServletRequest) req;
            HttpServletResponse response = (HttpServletResponse) resp;

            if (request.getMethod().equalsIgnoreCase("POST")) {

                System.out.println("\n");
                System.out.println("request.getMethod() = " + request.getMethod());
                System.out.println("request.getQueryString() = " + request.getQueryString());
                System.out.println("request.getParameterMap = " + request.getParameterMap());

                ByteArrayOutputStream requestStream = new ByteArrayOutputStream();
                ByteArrayOutputStream responseStream = new ByteArrayOutputStream();

                chain.doFilter(
                        getRequestWrapper(request, requestStream),
                        getResponseWrapper(response, responseStream));

                try {
                    writeReport(request, response, requestStream, responseStream);
                } catch (Throwable e) {
                    e.printStackTrace();
                    throw e;
                }

            } else {
                chain.doFilter(req, resp);
            }

        } else {
            System.err.println("DBSaverFilter.doFilter NOT HTTP");
            chain.doFilter(req, resp);
        }
    }

    private void writeReport(HttpServletRequest request, HttpServletResponse response, ByteArrayOutputStream requestStream, ByteArrayOutputStream responseStream) throws IOException {

        String mime = response.getHeader("Content-Type");

        String rptdesign = request.getParameter("__report");
        String format = request.getParameter("__format");
        if (format == null) format = "html";

        Map<String, String> params = getSoapParams(request, requestStream);
        System.out.println("reportParams " + new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(params));
        System.out.println("rep_params " + params);

        String dns = params.get("dont-save");
        if (dns != null && (dns.equalsIgnoreCase("true") || dns.equalsIgnoreCase("yes"))) {
            System.out.println("Do not save: " + dns);
            return;
        }

        String RPD = params.get("RPD");
        String RPFD = params.get("RPFD");
        String username = params.get("username");
        String rep_nm = params.get("rep_nm");
        String rep_tp_id = params.get("rep_tp_id");

        Report report = new Report();
        report.setData(responseStream.toByteArray());
        report.setRptdesign(rptdesign);
        report.setFormat(format);
        report.setMime(mime);
        report.setParams(new ObjectMapper().writeValueAsString(params));
        report.setUsername(username);
        report.setRep_nm(rep_nm);
        report.setRep_tp_id(rep_tp_id != null ? Integer.valueOf(rep_tp_id) : null);

        if (RPD != null) {
            Integer msr_prd_tp_id = Integer.valueOf(RPD);
            Optional<MsrPrdTp> msrPrdTp = msrPrdTpRepository.findById(msr_prd_tp_id);
            report.setMsr_prd_tp_id(msr_prd_tp_id);
            msrPrdTp.ifPresent(msrPrdTp1 -> report.setMsr_prd_tp(msrPrdTp1.getNm()));
        }

        if (RPFD != null) {
            LocalDate start;
            try {
                start = LocalDate.parse(RPFD, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            } catch (DateTimeParseException e) {
                start = LocalDate.parse(RPFD);
            }
            report.setMsr_prd_st_dttm(start);
        }

        System.out.println("report = " + report);
        reportRepository.saveAndFlush(report);
    }

    private Map<String, String> readRequestParams(ByteArrayOutputStream requestStream) throws IOException {
        if (requestStream == null) return null;

        byte[] byteArray = requestStream.toByteArray();
        if (byteArray == null || byteArray.length == 0) return null;

        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

        return xmlMapper.readValue(new ByteArrayInputStream(byteArray), Envelope.class).getBody().getGetUpdatedObjects().getOperation().params();
    }

    private Map<String, String> readFormParams(HttpServletRequest request) throws IOException {
        if (request == null) return null;

        Map<String, String> res = new HashMap<>();
        Map<String, String[]> p = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : p.entrySet()) {
            res.put(entry.getKey(), entry.getValue()[0]);
        }
        return res;
    }

    private Map<String, String> getSoapParams(HttpServletRequest request, ByteArrayOutputStream requestStream) throws IOException {
        try {
            String sessionId = request.getParameter("__sessionId");
            System.out.println("sessionId = " + sessionId);
            System.out.println("requestStream " + requestStream.toString());

            Map<String, String> params = readRequestParams(requestStream);
            System.out.println("params = " + params);

            if (params == null) params = readFormParams(request);

            if (params == null) params = requestMap.get(sessionId);

            if (params == null)
                throw new RuntimeException("Can't read request params");

            if (sessionId != null && !requestMap.containsKey(sessionId))
                requestMap.put(sessionId, params);

            return params;
        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void init(javax.servlet.FilterConfig config) {

    }


    private HttpServletResponse getResponseWrapper(HttpServletResponse response, final ByteArrayOutputStream stream) {
        return new HttpServletResponseWrapper(response) {
            @Override
            public ServletOutputStream getOutputStream() throws IOException {
                return new DelegatingServletOutputStream(new TeeOutputStream(
                        super.getOutputStream(),
                        stream
                ));
            }
        };
    }

    private HttpServletRequestWrapper getRequestWrapper(HttpServletRequest request, final ByteArrayOutputStream stream) {
        return new HttpServletRequestWrapper(request)
        {
            @Override
            public ServletInputStream getInputStream() throws IOException {
                return new DelegatingServletInputStream(new TeeInputStream(
                        super.getInputStream(),
                        stream
                ));
            }
        };
    }


}
