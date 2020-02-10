package com.ecls.autoconfiguration.model;

import enums.SystemModeUnit;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SystemModeRequestTokenFilter implements Filter {

    private final CurrentSystemMode currentSystemMode;

    public SystemModeRequestTokenFilter (CurrentSystemMode currentSystemMode) {
        this.currentSystemMode = currentSystemMode;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        String uri = ((HttpServletRequest) request).getRequestURI();
        if(uri.equals("/mode") || uri.equals("/mode/current") ||  uri.equals("/mode/any")) {
             filterChain.doFilter(request, response);
             return;
        }
        SystemModeUnit systemModeUnit = currentSystemMode.getSystemModeUnit();

        if (systemModeUnit.equals(SystemModeUnit.SERVICE) || systemModeUnit.equals(SystemModeUnit.EMERGANCE)) {
            if (response instanceof HttpServletResponse) {
                HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (!(
                    authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SYSTEM")) ||
                    authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CONFIG_CLIENT")) ||
                    authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGE_CONFIGURATIONS")) ||
                    authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGE_FUNCTION_MODE")) ||
                    authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
                )
                ) {
                    httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, generateErrorMessage(systemModeUnit));
                    return;
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private String generateErrorMessage(SystemModeUnit systemModeUnit) {
        if (systemModeUnit.equals(SystemModeUnit.SERVICE))
            return "Доступ запрещен! Система находится в сервисном режиме!";
        if (systemModeUnit.equals(SystemModeUnit.EMERGANCE))
            return "Доступ запрещен! Система находится в аварийном режиме!";
        return "";
    }


}
