package handlers;

import lombok.RequiredArgsConstructor;
import model.enums.SystemModeUnit;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import repositories.SystemModesRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
public class UpdateConfigHandler implements HandlerInterceptor {

    private final SystemModesRepository systemModesRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        SystemModeUnit curMode = systemModesRepository.getCurrentMode().orElse(SystemModeUnit.NORMAL);
        boolean isServiceMode = curMode.equals(SystemModeUnit.SERVICE);
        if(!isServiceMode){
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.getWriter().write("Для обновления конфигурации микросервиса систему необходимо перевести в сервисный режим!");
            response.flushBuffer();
        }
        return isServiceMode;
    }
}
