package services;

import analysis.CheckUnitResult;
import checkUnits.CheckUnitKey;
import checkUnits.CheckUnitType;
import enums.CheckUnitJobResult;
import enums.SortingDirection;
import exceptions.AS_15_8_DispatcherException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Result;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class ResultsKafkaService {

    @Value("${spring.cloud.stream.bindings.results_table.destination}")
    private String resultsTableName;

    private final InteractiveQueryService interactiveQueryService;

    public Page<Result> getArrangementResults(
            Long arrangementId,
            List<CheckUnitJobResult> checkUnitJobResults,
            List<CheckUnitType> checkUnitTypes,
            String query,
            SortingDirection sortingDirection,
            String sortingColumn,
            Pageable pageable) {

        Comparator<KeyValue<CheckUnitKey, CheckUnitResult>> checkUnitResultComparator = createResultsComparator(sortingColumn);
        if(sortingDirection.equals(SortingDirection.DESC))
            checkUnitResultComparator = checkUnitResultComparator.reversed();

        ReadOnlyKeyValueStore<CheckUnitKey, CheckUnitResult> store = getResultsKeyValueStore();
        KeyValueIterator<CheckUnitKey, CheckUnitResult> resultsIterator = getArrangementResultsIterator(store, arrangementId);
        List<Result> results = StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(resultsIterator, Spliterator.ORDERED), false)
                .filter(kv -> filterResults(kv.value, checkUnitJobResults, checkUnitTypes, query))
                .sorted(checkUnitResultComparator)
                .skip(pageable.getOffset())
                .limit(pageable.getPageSize())
                .map(kv -> {
                    DetailResultService<? super CheckUnitResult> service = AnalysisResultServiceFactory.getService(kv.value.getClass());
                    Result result = new Result();
                    fillResult(result, kv.key.getJobId(), kv.value, service);
                    return result;
                })
                .collect(Collectors.toList());
        return new PageImpl<>(results, pageable, results.size());
    }

    public CheckUnitResult getArrangementResult(Long arrangementId, Long resultId) {
        ReadOnlyKeyValueStore<CheckUnitKey, CheckUnitResult> store = getResultsKeyValueStore();
        return getArrangementResult(store, arrangementId, resultId);
    }

    public long getResultsCount(Long arrangementId) {
        ReadOnlyKeyValueStore<CheckUnitKey, CheckUnitResult> store = getResultsKeyValueStore();
        return store.approximateNumEntries();
        /*KeyValueIterator<CheckUnitKey, CheckUnitResult> resultsIterator = getArrangementResultsIterator(store, arrangementId);
        return countIteratorSize(resultsIterator);*/
    }

    KeyValueIterator<CheckUnitKey, CheckUnitResult> getArrangementResultsIterator(ReadOnlyKeyValueStore<CheckUnitKey, CheckUnitResult> store, Long arrangementId) {
        return store.range(
                new CheckUnitKey(arrangementId, Long.MIN_VALUE),
                new CheckUnitKey(arrangementId, Long.MAX_VALUE)
        );
    }

    ReadOnlyKeyValueStore<CheckUnitKey, CheckUnitResult> getResultsKeyValueStore() {
        ReadOnlyKeyValueStore<CheckUnitKey, CheckUnitResult> store = interactiveQueryService.getQueryableStore(
                resultsTableName,
                QueryableStoreTypes.keyValueStore()
        );
        if(store == null)
            throw new AS_15_8_DispatcherException("Ошибка чтения store из kafka!");
        return store;
    }

    void fillResult(Result result, Long jobId, CheckUnitResult checkUnitResult, DetailResultService<? super CheckUnitResult> service){
        if(result.getId() == null)
            result.setId(jobId);
        result.setErdiId(checkUnitResult.getCheckUnit().getContentId());
        result.setResult(checkUnitResult.getCheckResult());
        result.setCheckUnitType(checkUnitResult.getCheckUnit().getType());
        result.setCheckUnitValue(checkUnitResult.getCheckUnit().getValue());

        result.setStartDate(LocalDateTime.ofInstant(checkUnitResult.getStartTime().toInstant(), ZoneId.systemDefault()));
        result.setEndDate(LocalDateTime.ofInstant(checkUnitResult.getEndTime().toInstant(), ZoneId.systemDefault()));
        result.setCheckType(service.getCheckType());
    }

    private CheckUnitResult getArrangementResult(ReadOnlyKeyValueStore<CheckUnitKey, CheckUnitResult> store, Long arrangementId, Long jobId) {
        return store.get(new CheckUnitKey(arrangementId, Long.MIN_VALUE));
    }

    private int countIteratorSize(KeyValueIterator<CheckUnitKey, CheckUnitResult> resultsIterator) {
        AtomicInteger count = new AtomicInteger();
        resultsIterator.forEachRemaining( s-> count.getAndIncrement());
        return count.get();
    }

    private boolean filterResults(
            CheckUnitResult checkUnitResult,
            List<CheckUnitJobResult> checkUnitJobResults,
            List<CheckUnitType> checkUnitTypes,
            String query) {

        boolean notFiltered = true;
        if(checkUnitJobResults != null && checkUnitJobResults.size() > 0 &&
                !checkUnitJobResults.contains(checkUnitResult.getCheckResult())
        )
            notFiltered = false;
        if(checkUnitTypes != null && checkUnitTypes.size() > 0 &&
                !checkUnitTypes.contains(checkUnitResult.getCheckUnit().getType())
        )
            notFiltered = false;
        if(Strings.isNotEmpty(query) &&
                !checkUnitResult.getCheckResult().name().toLowerCase().contains(query.toLowerCase()) &&
                !checkUnitResult.getCheckUnit().getType().name().toLowerCase().contains(query.toLowerCase()) &&
                !checkUnitResult.getCheckUnit().getValue().toLowerCase().contains(query.toLowerCase())
        )
            notFiltered = false;
        return notFiltered;
    }

    private Comparator<KeyValue<CheckUnitKey, CheckUnitResult>> createResultsComparator(String columnName) {
        switch (columnName){
            case "checkUnitType":
                return Comparator.comparing((KeyValue<CheckUnitKey, CheckUnitResult> kv) -> kv.value.getCheckUnit().getType());
            case "checkUnitValue":
                return Comparator.comparing((KeyValue<CheckUnitKey, CheckUnitResult> kv) -> kv.value.getCheckUnit().getValue());
            case "result":
                return Comparator.comparing((KeyValue<CheckUnitKey, CheckUnitResult> kv) -> kv.value.getCheckResult());
            case "startDate":
                return Comparator.comparing((KeyValue<CheckUnitKey, CheckUnitResult> kv) -> kv.value.getStartTime());
            default:
                return Comparator.comparing((KeyValue<CheckUnitKey, CheckUnitResult> kv) -> kv.value.getEndTime());
        }
    }
}
