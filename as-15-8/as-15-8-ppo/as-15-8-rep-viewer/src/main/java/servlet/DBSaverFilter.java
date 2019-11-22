package servlet;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.SneakyThrows;
import model.MsrPrdTp;
import model.soap.Envelope;
import model.Report;
import model.soap.SoapOperation;
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
import java.time.LocalDateTime;
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

    private final Map<String, SoapOperation> requestMap;

    @Autowired
    public DBSaverFilter(ReportRepository reportRepository, MsrPrdTpRepository msrPrdTpRepository, Map<String, SoapOperation> requestMap) {
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
            System.out.println("\n");
            System.out.println("request.getMethod() = " + request.getMethod());
            System.out.println("request.getQueryString() = " + request.getQueryString());

            if (request.getMethod().equalsIgnoreCase("POST")) {

                ByteArrayOutputStream requestStream = new ByteArrayOutputStream();
                ByteArrayOutputStream responseStream = new ByteArrayOutputStream();

                chain.doFilter(
                        getRequestWrapper(request, requestStream),
                        getResponseWrapper(response, responseStream));

                System.out.println("request = " + request.getQueryString());

                String rptdesign = request.getParameter("__report");
                System.out.println("rptdesign " + rptdesign);

                String format = request.getParameter("__format");
                if (format == null) format = "html";
                System.out.println("format " + format);

                String sessionId = request.getParameter("__sessionId");
                System.out.println("sessionId " + sessionId);

                String mime = response.getHeader("Content-Type");
                System.out.println("mime " + mime);

                SoapOperation soapOperation = getSoapOperation(sessionId, requestStream);
//                System.out.println("soapOperation " + new ObjectMapper().writeValueAsString(soapOperation));
                System.out.println("soapOperation " + new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(soapOperation));
                Map<String, String> params = soapOperation.params();
                System.out.println("rep_params " + params);

                String RPD = params.get("RPD");
                System.out.println("RPD = " + RPD);


                String RPFD = params.get("RPFD");
                System.out.println("RPFD = " + RPFD);

                try {
                    Report report = new Report();
                    report.setData(responseStream.toByteArray());
                    report.setRptdesign(rptdesign);
                    report.setFormat(format);
                    report.setMime(mime);
                    report.setParams(new ObjectMapper().writeValueAsString(soapOperation));
                    report.setUsername(quessUsername());

                    if (RPD != null) {
                        Integer msr_prd_tp_id = Integer.valueOf(RPD);
                        Optional<MsrPrdTp> msrPrdTp = msrPrdTpRepository.findById(msr_prd_tp_id);
                        report.setMsr_prd_tp_id(msr_prd_tp_id);
                        msrPrdTp.ifPresent(msrPrdTp1 -> report.setMsr_prd_tp(msrPrdTp1.getNm()));
                    }

                    if (RPFD!=null) {
                        LocalDate start = LocalDate.parse(RPFD);
                        report.setMsr_prd_st_dttm(start);
                    }

                    System.out.println("report = " + report);
                    reportRepository.saveAndFlush(report);
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

    private String quessUsername() {
        return "anonymous";
//        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        if (principal instanceof UserDetails) {
//            String username = ((UserDetails) principal).getUsername();
//        } else {
//            String username = principal.toString();
//        }
    }

    private SoapOperation readRequest(ByteArrayOutputStream requestStream) throws IOException {
        if (requestStream == null) return null;

        byte[] byteArray = requestStream.toByteArray();
        if (byteArray == null || byteArray.length == 0) return null;

        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

        return xmlMapper.readValue(new ByteArrayInputStream(byteArray), Envelope.class).getBody().getGetUpdatedObjects().getOperation();
    }

    private SoapOperation getSoapOperation(String sessionId, ByteArrayOutputStream requestStream) throws IOException {
        try {
            System.out.println("requestStream " + requestStream.toString());
            SoapOperation soapOperation = readRequest(requestStream);
            System.out.println("soapOperation = " + soapOperation);

            if (soapOperation == null) soapOperation = requestMap.get(sessionId);
            else requestMap.put(sessionId, soapOperation);

            return soapOperation;
        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void init(javax.servlet.FilterConfig config) throws javax.servlet.ServletException {

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
