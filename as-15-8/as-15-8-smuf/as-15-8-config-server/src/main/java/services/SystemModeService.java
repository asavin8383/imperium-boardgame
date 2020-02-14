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
import java.time.temporal.ChronoUnit;
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
    private volatile boolean cancelServiceModeSchedule = false;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ResponseEntity planServiceModeChange(String plannedDateTime) {

        SystemMode mode = getOrCreateSystemMode(plannedDateTime);

        if (plannedTimeGreaterThanNow(mode)) {
            planServiceModeChange(mode);
            return ResponseEntity.ok("Смена сервисного режима запланирована на " + mode.getPlannedDateTime().format(formatter));
        } if (plannedTimeEqualNow(mode)) {
            changeSystemMode(mode);
            return ResponseEntity.ok("Система перешла в сервисный режим");
        }
        else return ResponseEntity.badRequest().body("Смена режима работы на Сервисный не запланирована! Задано некорректное время.");
    }

    private void planServiceModeChange(SystemMode mode) {
        cancelServiceModeSchedule = false;
        systemModesRepository.save(mode);
        scheduler.schedule(changeMode(mode), ldtToDate(mode.getPlannedDateTime()));
    }

    private boolean ifServiceModeIsPlanned() {
        Optional<SystemMode> mode = systemModesRepository.findBySystemMode(SystemModeUnit.SERVICE);
        if (mode.isPresent()) {
            if (plannedTimeGreaterThanNow(mode.get()) && plannedTimeEqualNow(mode.get()))
                return true;
        }
        return false;
    }
    private boolean plannedTimeGreaterThanNow(SystemMode mode) {
        return mode.getPlannedDateTime() != null && mode.getPlannedDateTime().isAfter(LocalDateTime.now());
    }

    private boolean plannedTimeEqualNow(SystemMode mode) {
        return mode.getPlannedDateTime() != null && mode.getPlannedDateTime().truncatedTo(ChronoUnit.MINUTES)
                .isEqual(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
    }

    private SystemMode getOrCreateSystemMode(String plannedDateTime) {
        LocalDateTime scheduleTime = parseLdt(plannedDateTime);
        SystemMode mode = systemModesRepository.findBySystemMode(SystemModeUnit.SERVICE)
                .orElseGet(() -> new SystemMode(SystemModeUnit.SERVICE));
        mode.setPlannedDateTime(scheduleTime);
        return mode;
    }

    private LocalDateTime parseLdt(String ldt) {
        return LocalDateTime.parse(ldt, formatter);
    }

    private Runnable changeMode(SystemMode mode) {
        return () -> {
            if (!cancelServiceModeSchedule) {
                cancelServiceModeSchedule = false;
                stopAllArrangementsInDispatcher();
                mode.setPlannedDateTime(null);
                SystemMode savedMode = systemModesRepository.save(mode);
                systemModesRepository.setCurrentSystemMode(savedMode.getId());
                notifyAllApplications(mode.getSystemMode());
                log.info("Произошёл запланированный переход в сервисный режим");
            } else cancelServiceModeSchedule = false;
        };
    }

    public void cancelServiceModeSchedule() {
        Optional<SystemMode> mode = systemModesRepository.findBySystemMode(SystemModeUnit.SERVICE);
        if (mode.isPresent()) {
            cancelServiceModeSchedule = true;
            mode.get().setPlannedDateTime(null);
            systemModesRepository.save(mode.get());
        }
        activateNormalMode();
    }


    private SystemMode activateNormalMode() {
        Optional<SystemMode> mode = systemModesRepository.findBySystemMode(SystemModeUnit.NORMAL);
        if (mode.isPresent()) {
            systemModesRepository.setCurrentSystemMode(mode.get().getId());
            return systemModesRepository.save(mode.get());
        } else {
            SystemMode savedMode = systemModesRepository.save(new SystemMode(SystemModeUnit.NORMAL));
            systemModesRepository.setCurrentSystemMode(savedMode.getId());
            return savedMode;
        }
    }

    private void stopAllArrangementsInDispatcher() {
        try {
            oAuth2RestTemplate.postForObject(UriComponentsBuilder.
                            fromHttpUrl(gatewayUrl)
                            .path(DISPATCHER_STOP_ARRANGEMENTS)
                            .build().toString(),
                    null,
                    ResponseEntity.class);
        }catch (Exception ex) {
            log.warn("Ошибка отправки запроса на останов всех мероприятий в dispatcher, код: " + ex);
        }
    }

    private Date ldtToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public SystemMode getCurrentMode() {
        return systemModesRepository.getCurrentSystemMode()
                  .orElseGet(this::activateNormalMode);
    }

    private void notifyAllApplications(SystemModeUnit systemModeUnit) {
        List<ServiceInstance> instances = getAllApplicationInstances();
        instances.forEach(instance ->
                postToSystemModeAutoconfiguration(instance.getUri(), systemModeUnit, instance));
    }

    private SystemModeUnit postToSystemModeAutoconfiguration(URI uri, SystemModeUnit systemModeUnit, ServiceInstance instance) {
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
        return new HttpEntity<>(systemModeUnit, headers);
    }

    private List<ServiceInstance> getAllApplicationInstances() {
        List<String> services = discoveryClient.getServices();

        List<ServiceInstance> instances = new ArrayList<ServiceInstance>();
        services.forEach(serviceName -> this.discoveryClient.getInstances(serviceName)
                .forEach(instance -> instances.add(instance)));
        return instances;
    }

    @Async
    public SystemMode changeSystemMode(SystemMode mode) {
        notifyAllApplications(mode.getSystemMode());
        stopArrangementsIfNecessary(mode);
        return systemModesRepository.findBySystemMode(mode.getSystemMode())
                .map(systemMode -> {
                    systemMode.setPlannedDateTime(null);
                    systemModesRepository.save(systemMode);
                    systemModesRepository.setCurrentSystemMode(systemMode.getId());
                    return systemMode;
                })
                .orElseGet(() -> {
                    SystemMode savedMode = systemModesRepository.save(new SystemMode(mode.getSystemMode()));
                    systemModesRepository.setCurrentSystemMode(savedMode.getId());
                    return savedMode;
                });
    }

    private void stopArrangementsIfNecessary(SystemMode mode) {
        if (mode.getSystemMode() == SystemModeUnit.SERVICE || mode.getSystemMode() == SystemModeUnit.EMERGANCE)
            stopAllArrangementsInDispatcher();
    }

}
