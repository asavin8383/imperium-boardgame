package servlet;

import model.Report;
import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.io.output.TeeOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import repositories.ReportRepository;

import javax.annotation.PostConstruct;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * User: asinjavin
 * Date: 21.11.2019
 * Time: 15:41
 */
@Component("DBSaverFilter")
public class DBSaverFilter implements javax.servlet.Filter
{
    final ReportRepository reportRepository;

    @Autowired
    public DBSaverFilter(ReportRepository reportRepository) {this.reportRepository = reportRepository;}

    @PostConstruct
    public void init() {
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    public void destroy() {
    }

    public void doFilter(javax.servlet.ServletRequest req, javax.servlet.ServletResponse resp, javax.servlet.FilterChain chain) throws javax.servlet.ServletException, IOException {
        if (req instanceof HttpServletRequest && resp instanceof HttpServletResponse) {
            HttpServletRequest request = (HttpServletRequest) req;
            HttpServletResponse response = (HttpServletResponse) resp;
            System.out.println("DBSaverFilter.doFilter");
            System.out.println("request.getMethod() = " + request.getMethod());
            System.out.println("request.getQueryString() = " + request.getQueryString());
            System.out.println("response.getContentType() = " + response.getContentType());
            System.out.println("response.getCharacterEncoding() = " + response.getCharacterEncoding());
            System.out.println("response.getHeaderNames() = " + response.getHeaderNames());

            ByteArrayOutputStream requestStream = new ByteArrayOutputStream();
            ByteArrayOutputStream responseStream = new ByteArrayOutputStream();

            HttpServletRequest requestWrapper = getRequestWrapper(request, requestStream);
            HttpServletResponse responseWrapper = getResponseWrapper(response, responseStream);

            chain.doFilter(requestWrapper, responseWrapper);

            System.out.println("DBSaverFilter.doFilter DONE");

            System.out.println("\n\n");
            System.out.println("request = " + request.getQueryString());
            System.out.println("responseStream = " + responseStream.size());
            System.out.println("requestStream = " + requestStream.toString());

            if (request.getMethod().equalsIgnoreCase("POST")) {
                Report report = new Report();
                report.setData(responseStream.toByteArray());
                report.setRptdesign(request.getParameter("__report"));
                report.setFormat("html");
                reportRepository.saveAndFlush(report);
            }

        } else {
            System.err.println("DBSaverFilter.doFilter NOT HTTP");
            chain.doFilter(req, resp);
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
