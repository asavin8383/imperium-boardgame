package services;

import analysis.CheckUnitResult;
import checkUnits.CheckUnitKey;
import checkUnits.CheckUnitType;
import enums.CheckUnitJobResult;
import enums.SortingDirection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Result;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.logging.log4j.util.Strings;
import org.apache.logging.log4j.util.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

        log.info("Запрос результатов мероприятия: " + arrangementId);
        return getArrangementResultsIterator(arrangementId)
                .map(resultsIterator ->  {
                    log.info("Результаты получены из хранилища: " + arrangementId);
                    Comparator<KeyValue<CheckUnitKey, CheckUnitResult>> checkUnitResultComparator = createResultsComparator(sortingColumn);
                    if(sortingDirection != null && sortingDirection.equals(SortingDirection.DESC))
                        checkUnitResultComparator = checkUnitResultComparator.reversed();
                    Supplier<Stream<KeyValue<CheckUnitKey, CheckUnitResult>>> streamSupplier = () -> StreamSupport
                            .stream(Spliterators.spliteratorUnknownSize(resultsIterator, Spliterator.ORDERED), false)
                            .filter(kv -> filterResults(kv.value, checkUnitJobResults, checkUnitTypes, query));
                    long count = streamSupplier.get().count();
                    List<Result> results = streamSupplier
                            .get()
                            //.sorted(checkUnitResultComparator)
                            .skip(pageable.getOffset())
                            .limit(pageable.getPageSize())
                            .map(kv -> {
                                DetailResultService<? super CheckUnitResult> service = AnalysisResultServiceFactory.getService(kv.value.getClass());
                                Result result = new Result();
                                fillResult(result, kv.key.getJobId(), kv.value, service);
                                return result;
                            })
                            .collect(Collectors.toList());
                    return new PageImpl<>(results, pageable, count);
                })
                .orElseGet(() -> new PageImpl<>(new ArrayList<>(), pageable, 0));
    }

    public Optional<CheckUnitResult> getArrangementResult(Long arrangementId, Long jobId) {
        return getResultsKeyValueStore()
                .map(store -> store.get(new CheckUnitKey(arrangementId, jobId)));
    }

    public long getResultsCount(Long arrangementId) {
        return getResultsKeyValueStore().
                map(ReadOnlyKeyValueStore::approximateNumEntries)
                .orElse(0L);
        /*KeyValueIterator<CheckUnitKey, CheckUnitResult> resultsIterator = getArrangementResultsIterator(store, arrangementId);
        return countIteratorSize(resultsIterator);*/
    }

    Optional<KeyValueIterator<CheckUnitKey, CheckUnitResult>> getArrangementResultsIterator(Long arrangementId) {
        return getResultsKeyValueStore().map(store -> store.range(
                new CheckUnitKey(arrangementId, Long.MIN_VALUE),
                new CheckUnitKey(arrangementId, Long.MAX_VALUE))
        );
    }

    private Optional<ReadOnlyKeyValueStore<CheckUnitKey, CheckUnitResult>> getResultsKeyValueStore() {
        return Optional.ofNullable(interactiveQueryService.getQueryableStore(
                resultsTableName,
                QueryableStoreTypes.keyValueStore()
        ));
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

    @SuppressWarnings("unchecked")
    private Comparator<KeyValue<CheckUnitKey, CheckUnitResult>> createResultsComparator(String columnName) {
        try {
            if(Strings.isEmpty(columnName))
                return Comparator.comparing((KeyValue<CheckUnitKey, CheckUnitResult> kv) -> kv.value.getEndTime());
            String methodName = "get" +
                    columnName.substring(0, 1).toUpperCase() +
                    columnName.substring(1);
            Method method = Result.class.getMethod(methodName);
            return (o1, o2) -> {
                try {
                    Object val1 = method.invoke(o1.value);
                    Object val2 = method.invoke(o2.value);
                    if(val1 instanceof Comparable && val2 instanceof Comparable)
                        return ((Comparable)val1).compareTo(val2);
                    else
                        return 0;
                } catch (Exception ex){
                    return 0;
                }
            };
        } catch (Exception ex) {
            log.warn("Ошибка при создании компаратора для сортировки результатов проверок", ex);
            return Comparator.comparing((KeyValue<CheckUnitKey, CheckUnitResult> kv) -> kv.value.getEndTime());
        }
    }
}
