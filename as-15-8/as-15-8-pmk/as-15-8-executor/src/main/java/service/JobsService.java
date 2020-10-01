package service;

import checkUnits.CheckUnitJob;
import checkUnits.CheckUnitKey;
import common.ExecutorProperties;
import execution.ExecutionJobResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import robots.exceptions.ExecutionException;
import robots.exceptions.Timeout_ExecutionException;
import service.impl.RobotsServiceImpl;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class JobsService {

    @Value("${gateway.url}")
    private String gatewayUrl;

    private final ExecutorProperties executorProps;

    @Value("${fetch-stooped-jobs-enabled:true}")
    private boolean fetchStoppedJobsEnabled;

    private final OAuth2RestTemplate oAuth2RestTemplate;
    private final CheckUnitVerificationServiceFactory checkUnitVerificationServiceFactory;

    private final Map<Long, Set<Long>> stoppedJobs = new ConcurrentHashMap<>();

    public int getJobTimeout(){
        return Optional.ofNullable(executorProps.getExecutor().getTimeout()).orElse(180);
    }

    @PostConstruct
    private void fillStoppedArrangements() {
        if(!fetchStoppedJobsEnabled)
            return;
        try {
            UriComponents uriComponents =
                    UriComponentsBuilder
                            .fromHttpUrl(gatewayUrl)
                            .path("/dispatcher/arrangements/stopped")
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

    public ExecutionJobResult executeJob(CheckUnitKey key, CheckUnitJob job) {

        CheckUnitVerificationService service =
                checkUnitVerificationServiceFactory
                        .getService(job);

        ExecutionJobResult executionJobResult;

        if (service instanceof RobotsServiceImpl && getJobTimeout() >= 0){
            ExecutorService executorService = Executors.newSingleThreadExecutor();

            try {
                ExecutorService threadExecutor = Executors.newSingleThreadExecutor();
                CompletableFuture<ExecutionJobResult> future =
                    CompletableFuture
                        .supplyAsync(() -> service.run(key.getJobId(), job), threadExecutor)
                        .exceptionally(throwable -> {
                            throw new CompletionException(throwable);
                        })
                        .whenComplete((x, y) -> threadExecutor.shutdownNow());
                try {
                    executionJobResult = future.get(getJobTimeout(), TimeUnit.SECONDS);
                }
                catch(TimeoutException ex) {
                    service.stop(key.getJobId());
                    throw new Timeout_ExecutionException();
                }

            } catch (Exception ex) {
                log.warn("JobsService exc", ex);
                if(ex instanceof ExecutionException)
                    throw (ExecutionException)ex;
                else
                    throw new ExecutionException(ex);
            }
        }
        else {
            executionJobResult = service.run(key.getJobId(), job);
        }
        return executionJobResult;
    }

    public synchronized boolean isJobActual(Long arrangementId, Long version, LocalDateTime jobTime) {
        if(jobTime.isBefore(LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT)))
            return false;
        return Optional.ofNullable(stoppedJobs.get(arrangementId))
                .map(stArr -> !stArr.contains(version))
                .orElse(true);
    }

    public void stop(Long arrangementId, Long version) {
        if(stoppedJobs.containsKey(arrangementId))
            stoppedJobs.get(arrangementId).add(version);
        else
            stoppedJobs.put(arrangementId, new HashSet<>(Collections.singletonList(version)));
    }
}
