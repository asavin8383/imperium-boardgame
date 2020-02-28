package services;

import analysis.CheckUnitResult;
import checkUnits.CheckUnitKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Result;
import org.apache.kafka.streams.processor.StateStore;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreType;
import org.apache.kafka.streams.state.Stores;
import org.apache.kafka.streams.state.internals.MeteredKeyValueStore;
import org.apache.kafka.streams.state.internals.StateStoreProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import repositories.ResultRepo;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

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
                interactiveQueryService.getQueryableStore(
                        resultsTableName,
                        new KeyValueStoreType<CheckUnitKey, CheckUnitResult>()
                );

           /* KeyValueStore<CheckUnitKey, CheckUnitResult> store =
                    Stores.keyValueStoreBuilder(
                            Stores.persistentKeyValueStore(resultsTableName),
                            new JsonSerde<>(CheckUnitKey.class),
                            new JsonSerde<>(CheckUnitResult.class)
                    ).build();*/
           // try {
            PageRequest pageable = PageRequest.of(0, 100);
            Page<Result> resultPage = resultRepo.findResultIdsBeforeDate(LocalDateTime.now().minusDays(resultsRetentionDays), pageable);
            int curPage = 0;
            do {
                resultPage.getContent().forEach(result -> {
                    CheckUnitKey checkUnitKey = new CheckUnitKey(
                            result.getArrangement().getId(),
                            result.getId(),
                            result.getArrangement().getVersion()
                    );
                    store.delete(checkUnitKey);
                    log.info("Результат " + result.getId() + " от " + result.getEndDate() + " успешно удален из локального хранилища");
                });
                curPage++;
                pageable = PageRequest.of(curPage, 100);
                resultPage = resultRepo.findResultIdsBeforeDate(LocalDateTime.now().minusDays(resultsRetentionDays), pageable);
            } while (resultPage.getTotalPages() > curPage);
            /*} finally {
                if(store != null)
                    store.close();
            }*/
        } catch (Exception ex) {
            log.error("Ошибка при очистке локального хранилища", ex);
        }
    }

    public static class KeyValueStoreType<K, V> implements QueryableStoreType<KeyValueStore<CheckUnitKey, CheckUnitResult>> {

        private final Set<Class> matchTo;

        KeyValueStoreType() {
            this.matchTo = Collections.singleton(MeteredKeyValueStore.class);
        }

        public KeyValueStore<CheckUnitKey, CheckUnitResult> create(StateStoreProvider storeProvider, String storeName) {
            return Stores.keyValueStoreBuilder(
                    Stores.persistentKeyValueStore(storeName),
                    new JsonSerde<>(CheckUnitKey.class),
                    new JsonSerde<>(CheckUnitResult.class)
            ).build();
        }

        public boolean accepts(StateStore stateStore) {
            Iterator iterator = this.matchTo.iterator();

            Class matchToClass;
            //noinspection unchecked
            do {
                if (!iterator.hasNext()) {
                    return true;
                }

                matchToClass = (Class)iterator.next();
            } while(matchToClass.isAssignableFrom(stateStore.getClass()));

            return false;
        }
    }
}
