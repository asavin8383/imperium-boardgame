package services;

import analysis.CheckUnitResult;
import checkUnits.CheckUnitKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Result;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import repositories.ResultRepo;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class KafkaLocalStoreService {

    @Value("${spring.cloud.stream.bindings.results_table.destination}")
    private String resultsTableName;

    @Value("${results.retention.days:7}")
    private int resultsRetentionDays;

    private final ResultRepo resultRepo;
    private final InteractiveQueryService interactiveQueryService;

    @Scheduled(cron = "${results.clear.schedule}")
    public void clearOldMessages() {
        log.info("Запуск регламентной очистки локального хранилища");
        try {

            KeyValueStore<CheckUnitKey, CheckUnitResult> store =
                    Stores.keyValueStoreBuilder(
                            Stores.persistentKeyValueStore(resultsTableName),
                            new JsonSerde<>(CheckUnitKey.class),
                            new JsonSerde<>(CheckUnitResult.class)
                    ).build();
           // try {
                List<Result> oldResults = resultRepo.findResultIdsBeforeDate(LocalDateTime.now().minusDays(resultsRetentionDays));
                log.info("Отобрано записей для очистки: " + oldResults.size());
                oldResults.forEach(result -> {
                    CheckUnitKey checkUnitKey = new CheckUnitKey(
                            result.getArrangement().getId(),
                            result.getId(),
                            result.getArrangement().getVersion()
                    );
                    store.delete(checkUnitKey);
                    log.info("Результат " + result.getId() + " от " + result.getEndDate() + " успешно удален из локального хранилища");
                });
            /*} finally {
                if(store != null)
                    store.close();
            }*/
        } catch (Exception ex) {
            log.error("Ошибка при очистке локального хранилища", ex);
        }
    }

}
