package handlers;

import lombok.RequiredArgsConstructor;
import enums.SystemModeUnit;
import model.SystemMode;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import repositories.SystemModesRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
public class UpdateConfigHandler implements HandlerInterceptor {

    private final SystemModesRepository systemModesRepository;

    //TODO переделать на фильтр, перенести в бин
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        SystemMode curMode = systemModesRepository.getCurrentSystemMode().orElse( new SystemMode(SystemModeUnit.NORMAL, true));
        boolean isServiceMode = curMode.getSystemMode().equals(SystemModeUnit.SERVICE);
        if(!isServiceMode){
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.getWriter().write("Для обновления конфигурации микросервиса систему необходимо перевести в сервисный режим!");
            response.flushBuffer();
        }
        return isServiceMode;
    }
}
