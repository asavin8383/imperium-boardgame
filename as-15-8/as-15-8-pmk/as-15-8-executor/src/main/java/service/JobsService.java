package service;

import checkUnits.CheckUnitJob;
import checkUnits.CheckUnitKey;
import execution.ExecutionJobResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import remoteEvents.ArrangementStopEvent;
import robots.exceptions.Timeout_ExecutionException;
import service.impl.RobotsServiceImpl;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class JobsService {

    @Value("${gateway.url}")
    private String gatewayUrl;

    @Getter
    @Value("${executor.timeout:60}")
    private int jobTimeout;

    private final OAuth2RestTemplate oAuth2RestTemplate;
    private final CheckUnitVerificationServiceFactory checkUnitVerificationServiceFactory;

    private Map<Long, Set<Long>> stoppedJobs = new ConcurrentHashMap<>();

    @PostConstruct
    private void fillStoppedArrangements() {
        try {
            UriComponents uriComponents =
                    UriComponentsBuilder
                            .fromHttpUrl(gatewayUrl)
                            .path("/dispatcher/arrangement/stopped")
                            .build();

            Map<Long, Set<Long>> actualStoppedJobs = oAuth2RestTemplate
                    .exchange(uriComponents.toString(),
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<Map<Long, Set<Long>>>() {
                            })
                    .getBody();
            if (actualStoppedJobs != null && actualStoppedJobs.size() > 0) {
                stoppedJobs.clear();
                stoppedJobs.putAll(actualStoppedJobs);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Ошибка при получении списка остановленных заданий из диспетчера", ex);
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    void clearStoppedJobs() {
        stoppedJobs.clear();
    }

    @EventListener
    public void onApplicationEvent(ArrangementStopEvent event) {
        if(stoppedJobs.containsKey(event.getArrangementId()))
            stoppedJobs.get(event.getArrangementId()).add(event.getVersion());
        else
            stoppedJobs.put(event.getArrangementId(), new HashSet<>(Collections.singletonList(event.getVersion())));
    }

    public ExecutionJobResult executeJob(CheckUnitKey key, CheckUnitJob job) {

        CheckUnitVerificationService service =
                checkUnitVerificationServiceFactory
                        .getService(job);

        ExecutionJobResult executionJobResult;

        if (service instanceof RobotsServiceImpl && jobTimeout >= 0){
            executionJobResult = CompletableFuture
                    .supplyAsync(() -> service.run(key.getJobId(), job))
                    .applyToEither(timeoutAfter(jobTimeout, TimeUnit.SECONDS), (result) -> result)
                    .exceptionally(throwable -> {
                        service.stop();
                        throw new CompletionException(throwable);
                    })
                    .join();
        }
        else {
            executionJobResult = service.run(key.getJobId(), job);
        }
        return executionJobResult;
    }

    private <T> CompletableFuture<T> timeoutAfter(long timeout, TimeUnit unit) {
        CompletableFuture<T> result = new CompletableFuture<>();
        ScheduledExecutorService timeoutService = Executors.newSingleThreadScheduledExecutor();
        timeoutService.schedule(() -> result.completeExceptionally(new Timeout_ExecutionException()), timeout, unit);
        return result;
    }

    public synchronized boolean isJobActual(Long arrangementId, Long version, LocalDateTime jobTime) {
        if(jobTime.isBefore(LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT)))
            return false;
        return Optional.ofNullable(stoppedJobs.get(arrangementId))
                .map(stArr -> !stArr.contains(version))
                .orElse(true);
    }
}
