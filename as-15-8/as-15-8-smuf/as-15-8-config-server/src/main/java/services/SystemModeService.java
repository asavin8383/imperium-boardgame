package services;


import enums.SystemModeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.SystemMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import repositories.SystemModesRepository;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class SystemModeService {

    private final SystemModesRepository systemModesRepository;
    private final TaskScheduler scheduler;
    private final DiscoveryClient discoveryClient;
    private final String AUTOCONFIG_SYSTEM_MODE = "/actuator/system-mode";
    private final RestTemplate restTemplate;

    @Async
    public ResponseEntity planServiceModeChange(String plannedDateTime) {

        LocalDateTime scheduleTime = parseLdt(plannedDateTime);

        SystemMode mode = systemModesRepository.findBySystemMode(SystemModeUnit.SERVICE).orElseGet(() -> new SystemMode(SystemModeUnit.SERVICE, false));
        mode.setPlannedDateTime(scheduleTime);

        if(mode.getPlannedDateTime()!= null && mode.getPlannedDateTime().isAfter(LocalDateTime.now())) {
            systemModesRepository.save(mode);
            scheduler.schedule(changeMode(mode), ldtToDate(mode.getPlannedDateTime()));
            return ResponseEntity.ok("Смена сервисного режима запланирована на " + mode.getPlannedDateTime());
        } else
            return ResponseEntity.badRequest().body("Смена режима работы на Сервисный не запланирована!");

    }

    private LocalDateTime parseLdt(String ldt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime dateTime = LocalDateTime.parse(ldt, formatter);
        return dateTime;
    }

    private Runnable changeMode(SystemMode mode) {
        return () -> {
            mode.setActive(true);
            setAllSystemModesDisabled();
            systemModesRepository.save(mode);
            notifyAllApplications(mode.getSystemMode());
        };
    }

    private void setAllSystemModesDisabled() {
        systemModesRepository.findALL().get().forEach(systemMode -> {
            systemMode.setActive(false);
            systemModesRepository.save(systemMode);
        });
    }

    private Date ldtToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public SystemMode getCurrentMode() {
        return systemModesRepository.getCurrentSystemMode()
                .orElseGet(() -> systemModesRepository.save(new SystemMode(SystemModeUnit.NORMAL, true)));
    }

    public void notifyAllApplications(SystemModeUnit systemModeUnit) {
        List<ServiceInstance> instances = getAllUris();

        instances.forEach(instance ->
                postCurrentSystemMode(instance.getUri(), systemModeUnit, instance));
    }

    private SystemModeUnit postCurrentSystemMode(URI uri, SystemModeUnit systemModeUnit, ServiceInstance instance) {
        try {
            ResponseEntity response = restTemplate.postForEntity(createUri(uri),
                                                                 createEntity(systemModeUnit),
                                                                 SystemModeUnit.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                SystemModeUnit result = (SystemModeUnit) response.getBody();
                log.warn("========================= ++++++++ Обновление автоконфигурации для uri " + instance.getInstanceId() + " успешно, ответ :" + result);
                return result;
            }
        } catch (Exception ex) {
            log.warn("============================= Обновление автоконфигурации для uri " + instance.getInstanceId() + " неуспешно, ошибка:" + ex);
        }
        return null;
    }

    private String createUri(URI uri) {
        return UriComponentsBuilder
                .fromUri(uri)
                .path(AUTOCONFIG_SYSTEM_MODE)
                .build().toString();
    }

    private HttpEntity createEntity(SystemModeUnit systemModeUnit) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<SystemModeUnit> entity = new HttpEntity<>(systemModeUnit, headers);
        return entity;
    }

    private List<ServiceInstance> getAllUris() {
        List<String> services = discoveryClient.getServices();

        List<ServiceInstance> instances = new ArrayList<ServiceInstance>();
        services.forEach(serviceName -> this.discoveryClient.getInstances(serviceName)
                .forEach(instance -> instances.add(instance)));
        return instances;
    }

    public void setCurrentSystemModeDisabled() {
        systemModesRepository.getCurrentSystemMode().map(systemMode -> {
            systemMode.setActive(false);
            return systemModesRepository.save(systemMode);
        }).orElseGet(() -> systemModesRepository.save(new SystemMode(SystemModeUnit.NORMAL, false)));
    }

    public SystemMode changeSystemMode(SystemMode mode) {
       return systemModesRepository.findBySystemMode(mode.getSystemMode())
                .map(systemMode -> {
                    if(!systemMode.isActive()){
                        systemMode.setActive(true);
                        systemModesRepository.save(systemMode);
                    }
                    return mode;
                    //return ResponseEntity.ok("Режим успешно сменен" + mode);
                })
                .orElseGet(() -> {
                    systemModesRepository.save(new SystemMode(mode.getSystemMode(), true));
                    return mode;
                    //return ResponseEntity.ok("Режим успешно сменен" + mode);
                });
    }
}
