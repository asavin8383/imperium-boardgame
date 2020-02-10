package services;


import enums.SystemModeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.SystemMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class SystemModeService {

    @Value("${gateway.url}")
    private String gatewayUrl;

    private final SystemModesRepository systemModesRepository;
    private final TaskScheduler scheduler;
    private final DiscoveryClient discoveryClient;
    private final String AUTOCONFIG_SYSTEM_MODE = "/actuator/system-mode";
    private final String DISPATCHER_STOP_ARRANGEMENTS = "dispatcher/arrangements/stop_all_running";
    private final RestTemplate restTemplate;
    private final OAuth2RestTemplate oAuth2RestTemplate;
    private boolean cancelSytemModeChange = false;

    @Async
    public ResponseEntity planServiceModeChange(String plannedDateTime) {

        SystemMode mode = getOrCreateSystemMode(plannedDateTime);

        if(mode.getPlannedDateTime()!= null && mode.getPlannedDateTime().isAfter(LocalDateTime.now())) {
            systemModesRepository.save(mode);
            scheduler.schedule(changeMode(mode), ldtToDate(mode.getPlannedDateTime()));
            return ResponseEntity.ok("Смена сервисного режима запланирована на " + mode.getPlannedDateTime());
        } else
            return ResponseEntity.badRequest().body("Смена режима работы на Сервисный не запланирована!");
    }

    private SystemMode getOrCreateSystemMode(String plannedDateTime) {
        LocalDateTime scheduleTime = parseLdt(plannedDateTime);
        SystemMode mode = systemModesRepository.findBySystemMode(SystemModeUnit.SERVICE)
                .orElseGet(() -> new SystemMode(SystemModeUnit.SERVICE, false));
        mode.setPlannedDateTime(scheduleTime);
        return mode;
    }

    private LocalDateTime parseLdt(String ldt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime dateTime = LocalDateTime.parse(ldt, formatter);
        return dateTime;
    }

    private Runnable changeMode(SystemMode mode) {
        return () -> {
            if (!cancelSytemModeChange) {
                cancelSytemModeChange = false;
                stopAllArrangements();
                mode.setActive(true);
                mode.setPlannedDateTime(null);
                systemModesRepository.setAllSysemModesEnabled(false);
                systemModesRepository.save(mode);
                notifyAllApplications(mode.getSystemMode());
                log.info("Произошёл запланированный переход в сервисный режим");
            }
        };
    }

    public void cancelSystemModeSchedule() {
        this.cancelSytemModeChange = true;
        Optional<SystemMode> mode = systemModesRepository.findBySystemMode(SystemModeUnit.SERVICE);
        if (mode.isPresent()) {
            mode.get().setPlannedDateTime(null);
            mode.get().setActive(false);
            systemModesRepository.save(mode.get());
        }
        log.info("Запланированный переход в сервисный режим отменён пользователем");
    }

    private void stopAllArrangements() {
        try {
            oAuth2RestTemplate.postForObject(UriComponentsBuilder.
                            fromHttpUrl(gatewayUrl)
                            .path(DISPATCHER_STOP_ARRANGEMENTS)
                            .build().toString(),
                    null,
                    ResponseEntity.class);
        }catch (Exception ex) {
            log.warn("Ошибка отправки запроса на отсанов всех мероприятий в dispatcher, код:" + ex);
        }
    }

    private Date ldtToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public SystemMode getCurrentMode() {
        return systemModesRepository.getCurrentSystemMode()
                .orElseGet(() -> systemModesRepository.save(new SystemMode(SystemModeUnit.NORMAL, true)));
    }

    public void notifyAllApplications(SystemModeUnit systemModeUnit) {
        List<ServiceInstance> instances = getAllApplicationInstances();
        instances.forEach(instance ->
                postSystemModeUnit(instance.getUri(), systemModeUnit, instance));
    }

    private SystemModeUnit postSystemModeUnit(URI uri, SystemModeUnit systemModeUnit, ServiceInstance instance) {
        try {
            ResponseEntity response = restTemplate.postForEntity(createUri(uri, AUTOCONFIG_SYSTEM_MODE),
                                                                 createEntity(systemModeUnit),
                                                                 SystemModeUnit.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                SystemModeUnit result = (SystemModeUnit) response.getBody();
                log.info("Обновление автоконфигурации System_Mode для uri " + instance.getInstanceId() + " успешно, ответ :" + result);
                return result;
            }
        } catch (Exception ex) {
            log.warn("Обновление автоконфигурации System_Mode для uri " + instance.getInstanceId() + " не успешно, ошибка:" + ex);
        }
        return null;
    }

    private String createUri(URI uri, String path) {
        return UriComponentsBuilder
                .fromUri(uri)
                .path(path)
                .build().toString();
    }

    private HttpEntity createEntity(SystemModeUnit systemModeUnit) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<SystemModeUnit> entity = new HttpEntity<>(systemModeUnit, headers);
        return entity;
    }

    private List<ServiceInstance> getAllApplicationInstances() {
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
