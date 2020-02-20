package services;

import analysis.CheckUnitResult;
import checkUnits.CheckUnitKey;
import checkUnits.CheckUnitType;
import enums.CheckUnitJobResult;
import enums.SortingDirection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.DetailResult;
import model.Result;
import model.Screenshots;
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

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class ResultsKafkaService {

    private static final long minValue = 0L;
    private static final long maxValue = 9999999999999L;
    
    @Value("${spring.cloud.stream.bindings.results_table.destination}")
    private String resultsTableName;

    @Value("${spring.cloud.stream.bindings.screenshots_table.destination}")
    private String screenshotsTableName;

    @Value("${spring.cloud.stream.bindings.results_count_table.destination}")
    private String resultsCountTableName;

    private final InteractiveQueryService interactiveQueryService;

    private ReadOnlyKeyValueStore<CheckUnitKey, CheckUnitResult> resultsStore;
    private ReadOnlyKeyValueStore<Long, Long> resultsCountStore;
    private ReadOnlyKeyValueStore<CheckUnitKey, Screenshots> screenshotsStore;

    public Page<Result> getArrangementResults(
            Long arrangementId,
            List<CheckUnitJobResult> checkUnitJobResults,
            List<CheckUnitType> checkUnitTypes,
            String query,
            SortingDirection sortingDirection,
            String sortingColumn,
            Pageable pageable) {

        final Predicate<KeyValue<CheckUnitKey, CheckUnitResult>> filter = kv -> filterResults(kv.value, checkUnitJobResults, checkUnitTypes, query);
        final long count = getResultsCount(arrangementId, filter);
        List<Result> results = getArrangementResults(
                arrangementId,
                filter,
                sortingDirection,
                sortingColumn)
            .skip(pageable.getOffset())
            .limit(pageable.getPageSize())
            .collect(Collectors.toList());
        return new PageImpl<>(results, pageable, count);
    }

    private List<Result> getArrangementResults(
            Long arrangementId,
            List<CheckUnitJobResult> checkUnitJobResults,
            List<CheckUnitType> checkUnitTypes,
            String query,
            SortingDirection sortingDirection,
            String sortingColumn) {

        final Predicate<KeyValue<CheckUnitKey, CheckUnitResult>> filter = kv -> filterResults(kv.value, checkUnitJobResults, checkUnitTypes, query);
        return getArrangementResults(arrangementId, filter, sortingDirection, sortingColumn)
                .collect(Collectors.toList());
    }

    public long getArrangementForbiddenContentResultsCount(
            Long arrangementId) {

        final Predicate<KeyValue<CheckUnitKey, CheckUnitResult>> filter = kv ->
                filterResults(
                        kv.value,
                        Collections.singletonList(CheckUnitJobResult.FORBIDDEN_CONTENT_DETECTED),
                        null,
                        null);
        return getArrangementResults(arrangementId, filter, null, null)
                .count();
    }

    private Stream<Result> getArrangementResults(
            Long arrangementId,
            Predicate<KeyValue<CheckUnitKey, CheckUnitResult>> filter,
            SortingDirection sortingDirection,
            String sortingColumn) {

        return getArrangementResultsIterator(arrangementId)
                .map(resultsIterator ->  {
                    try {
                        Comparator<Result> checkUnitResultComparator = null;
                        if (Strings.isNotEmpty(sortingColumn)) {
                            checkUnitResultComparator = createResultsComparator(sortingColumn);
                            if (sortingDirection != null && sortingDirection.equals(SortingDirection.DESC))
                                checkUnitResultComparator = checkUnitResultComparator.reversed();
                        }

                        Stream<Result> resultsStream = StreamSupport
                                .stream(Spliterators.spliteratorUnknownSize(resultsIterator, Spliterator.ORDERED), false)
                                .filter(filter)
                                .map(kv -> {
                                    DetailResultService<? super CheckUnitResult, ? extends DetailResult> service = AnalysisResultServiceFactory.getService(kv.value.getClass());
                                    Result result = new Result();
                                    fillResult(result, kv.key.getJobId(), kv.value, service);
                                    return result;
                                });
                        if (checkUnitResultComparator != null)
                            resultsStream = resultsStream.sorted(checkUnitResultComparator);

                        return resultsStream;
                    } finally {
                        resultsIterator.close();
                    }
                })
                .orElseGet(Stream::empty);
    }

    public List<Long> getArrangementResultIds(Long arrangementId) {
        return getArrangementResultsIterator(arrangementId)
                .map(resultsIterator -> {
                    try {
                        return StreamSupport
                            .stream(Spliterators.spliteratorUnknownSize(resultsIterator, Spliterator.ORDERED), false)
                            .map(kv -> kv.key.getJobId())
                            .collect(Collectors.toList());
                    } finally {
                        resultsIterator.close();
                    }
                })
                .orElseGet(ArrayList::new);
    }

    public Optional<CheckUnitResult> getArrangementResult(Long arrangementId, Long jobId) {
        return getResultsKeyValueStore()
            .flatMap(store -> getLastIteratorValue(
                store.range(
                    new CheckUnitKey(arrangementId, jobId, minValue),
                    new CheckUnitKey(arrangementId, jobId, maxValue)
                )
            )
            .map(kv -> kv.value));
    }

    public Optional<Screenshots> getScreenshot(Long arrangementId, Long jobId) {
        return getScreenshotsKeyValueStore()
            .flatMap(store -> getLastIteratorValue(
                store.range(
                    new CheckUnitKey(arrangementId, jobId, minValue),
                    new CheckUnitKey(arrangementId, jobId, maxValue)
                )
            )
            .map(kv -> kv.value));
    }

    public long getResultsCount(Long arrangementId) {
        return getResultsCountKeyValueStore()
                .map(store ->
                        Optional.ofNullable(store.get(arrangementId))
                        .orElse(0L)
                ).orElse(0L);
    }

    public List<CheckUnitType> getDictinctCheckUnitTypes(Long arrangementId){
        return getDistinctResultValues(
            arrangementId,
            (KeyValue<CheckUnitKey, CheckUnitResult> message) -> message.value.getCheckUnit().getType()
        );
    }

    public List<CheckUnitJobResult> getDictinctCheckUnitResults(Long arrangementId){
        return getDistinctResultValues(
            arrangementId,
            (KeyValue<CheckUnitKey, CheckUnitResult> message) -> message.value.getCheckResult()
        );
    }

    private <T> List<T> getDistinctResultValues(Long arrangementId, Function<KeyValue<CheckUnitKey, CheckUnitResult>, T> function){
        return getArrangementResultsIterator(arrangementId)
            .map(resultsIterator -> {
                try {
                    return StreamSupport
                        .stream(Spliterators.spliteratorUnknownSize(resultsIterator, Spliterator.ORDERED), false)
                        .map(function)
                        .distinct()
                        .collect(Collectors.toList());
                } finally {
                    resultsIterator.close();
                }
            })
            .orElse(new ArrayList<>());
    }

    private long getResultsCount(Long arrangementId, Predicate<KeyValue<CheckUnitKey, CheckUnitResult>> filter){
        return getArrangementResultsIterator(arrangementId)
                .map(resultsIterator -> {
                    try {
                        return StreamSupport
                            .stream(Spliterators.spliteratorUnknownSize(resultsIterator, Spliterator.ORDERED), false)
                            .filter(filter)
                            .count();
                    } finally {
                        resultsIterator.close();
                    }
                })
                .orElse(0L);
    }

    Optional<KeyValueIterator<CheckUnitKey, CheckUnitResult>> getArrangementResultsIterator(Long arrangementId) {
        return getResultsKeyValueStore().map(store -> store.range(
                new CheckUnitKey(arrangementId, minValue, minValue),
                new CheckUnitKey(arrangementId, maxValue, maxValue))
        );
    }

    void fillResult(Result result, Long jobId, CheckUnitResult checkUnitResult, DetailResultService<? super CheckUnitResult, ? extends DetailResult> service){
        if(result.getId() == null)
            result.setId(jobId);
        result.setErdiId(checkUnitResult.getCheckUnit().getContentId());
        result.setResult(checkUnitResult.getCheckResult());
        result.setCheckUnitType(checkUnitResult.getCheckUnit().getType());
        result.setCheckUnitValue(checkUnitResult.getCheckUnit().getValue());

        result.setStartDate(LocalDateTime.ofInstant(checkUnitResult.getStartTime().toInstant(), ZoneId.systemDefault()));
        result.setEndDate(LocalDateTime.ofInstant(checkUnitResult.getEndTime().toInstant(), ZoneId.systemDefault()));
        result.setCheckType(service.getCheckType());

        if(checkUnitResult.getCheckResult().equals(CheckUnitJobResult.FORBIDDEN_CONTENT_DETECTED))
            result.setCheckForAct(true);
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

    private Comparator<Result> createResultsComparator(String columnName) {
        try {
            if(Strings.isEmpty(columnName))
                return Comparator.comparing(Result::getResult);
            String methodName = "get" +
                    columnName.substring(0, 1).toUpperCase() +
                    columnName.substring(1);
            Method method = Result.class.getMethod(methodName);
            return Comparator.comparing((Result result) -> {
                try {
                    return method.invoke(result).toString();
                } catch (Exception ex) {
                    return result.getResult().name();
                }
            });
        } catch (Exception ex) {
            log.warn("Ошибка при создании компаратора для сортировки результатов проверок", ex);
            return Comparator.comparing(Result::getResult);
        }
    }

    private Optional<ReadOnlyKeyValueStore<CheckUnitKey, CheckUnitResult>> getResultsKeyValueStore() {
        return Optional.ofNullable(resultsStore == null ?
                interactiveQueryService.getQueryableStore(
                        resultsTableName,
                        QueryableStoreTypes.keyValueStore()
                ) : resultsStore);
    }

    private Optional<ReadOnlyKeyValueStore<Long, Long>> getResultsCountKeyValueStore() {
        return Optional.ofNullable(resultsCountStore == null ?
                interactiveQueryService.getQueryableStore(
                        resultsCountTableName,
                        QueryableStoreTypes.keyValueStore()
                ) : resultsCountStore);
    }

    private Optional<ReadOnlyKeyValueStore<CheckUnitKey, Screenshots>> getScreenshotsKeyValueStore() {
        return Optional.ofNullable(screenshotsStore == null ?
                interactiveQueryService.getQueryableStore(
                        screenshotsTableName,
                        QueryableStoreTypes.keyValueStore()
                ) : screenshotsStore);
    }

    private <K, V> Optional<KeyValue<K, V>> getLastIteratorValue(KeyValueIterator<K, V> resultsIterator){
        return StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(resultsIterator, Spliterator.ORDERED), false)
            .reduce((first, second) -> second);
    }
}
