package filters;

import enums.SystemModeUnit;
import lombok.RequiredArgsConstructor;
import model.SystemMode;
import repositories.SystemModesRepository;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
public class UpdateConfigFilter implements Filter {

    private final SystemModesRepository systemModesRepository;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        String uri = ((HttpServletRequest) request).getRequestURI();
        if(uri.contains("/actuator/bus-refresh/")) {
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            SystemMode curMode = systemModesRepository.getCurrentSystemMode().orElse(new SystemMode(SystemModeUnit.NORMAL));

            boolean isServiceMode = curMode.getSystemMode().equals(SystemModeUnit.SERVICE);
            if (!isServiceMode) {
                httpServletResponse.sendError(400, "Для обновления конфигурации микросервиса систему необходимо перевести в сервисный режим!");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
