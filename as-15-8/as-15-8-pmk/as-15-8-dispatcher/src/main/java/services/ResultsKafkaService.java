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
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
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

    @Value("${results.retention.days:31}")
    private int resultsRetentionDays;

    private final InteractiveQueryService interactiveQueryService;

    private ReadOnlyWindowStore<CheckUnitKey, CheckUnitResult> resultsStore;
    private ReadOnlyWindowStore<CheckUnitKey, Screenshots> screenshotsStore;

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
                    Comparator<Result> checkUnitResultComparator = null;
                    if(Strings.isNotEmpty(sortingColumn)) {
                        checkUnitResultComparator = createResultsComparator(sortingColumn);
                        if (sortingDirection != null && sortingDirection.equals(SortingDirection.DESC))
                            checkUnitResultComparator = checkUnitResultComparator.reversed();
                    }

                    Stream<Result> resultsStream = StreamSupport
                            .stream(Spliterators.spliteratorUnknownSize(resultsIterator, Spliterator.ORDERED), false)
                            .map(val -> KeyValue.pair(val.key.key(), val.value))
                            .filter(filter)
                            .map(kv -> {
                                DetailResultService<? super CheckUnitResult, ? extends DetailResult> service = AnalysisResultServiceFactory.getService(kv.value.getClass());
                                Result result = new Result();
                                fillResult(result, kv.key.getJobId(), kv.value, service);
                                return result;
                            });
                    if(checkUnitResultComparator != null)
                        resultsStream = resultsStream.sorted(checkUnitResultComparator);

                    return resultsStream;
                })
                .orElseGet(Stream::empty);
    }

    public List<Long> getArrangementResultIds(Long arrangementId) {
        return getArrangementResultsIterator(arrangementId)
                .map(resultsIterator -> StreamSupport
                    .stream(Spliterators.spliteratorUnknownSize(resultsIterator, Spliterator.ORDERED), false)
                    .map(kv -> kv.key.key().getJobId())
                    .collect(Collectors.toList()))
                .orElseGet(ArrayList::new);
    }

    public Optional<CheckUnitResult> getArrangementResult(Long arrangementId, Long jobId) {
        return getResultsKeyValueStore()
            .flatMap(store -> getLastIteratorValue(
                store.fetch(
                    new CheckUnitKey(arrangementId, jobId, minValue),
                    new CheckUnitKey(arrangementId, jobId, maxValue),
                    Instant.now().minus(resultsRetentionDays, ChronoUnit.DAYS),
                    Instant.now()
                )
            )
            .map(kv -> kv.value));
    }

    public Optional<Screenshots> getScreenshot(Long arrangementId, Long jobId) {
        return getScreenshotsKeyValueStore()
            .flatMap(store -> getLastIteratorValue(
                store.fetch(
                    new CheckUnitKey(arrangementId, jobId, minValue),
                    new CheckUnitKey(arrangementId, jobId, maxValue),
                    Instant.now().minus(resultsRetentionDays, ChronoUnit.DAYS),
                    Instant.now()
                )
            )
            .map(kv -> kv.value));
    }

    public long getResultsCount(Long arrangementId) {
        return getResultsKeyValueStore()
            .map(store -> {
                AtomicLong count = new AtomicLong(0);
                store.fetch(
                    new CheckUnitKey(arrangementId, minValue, minValue),
                    new CheckUnitKey(arrangementId, maxValue, maxValue),
                    Instant.now().minus(resultsRetentionDays, ChronoUnit.DAYS),
                    Instant.now()
                ).forEachRemaining(cu -> count.getAndIncrement());
                return count.longValue();
            }).orElse(0L);
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
            .map(resultsIterator -> StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(resultsIterator, Spliterator.ORDERED), false)
                .map(val -> KeyValue.pair(val.key.key(), val.value))
                .map(function)
                .distinct()
                .collect(Collectors.toList())
            )
            .orElse(new ArrayList<>());
    }

    private long getResultsCount(Long arrangementId, Predicate<KeyValue<CheckUnitKey, CheckUnitResult>> filter){
        return getArrangementResultsIterator(arrangementId)
                .map(resultsIterator -> StreamSupport
                        .stream(Spliterators.spliteratorUnknownSize(resultsIterator, Spliterator.ORDERED), false)
                        .map(val -> KeyValue.pair(val.key.key(), val.value))
                        .filter(filter)
                        .count())
                .orElse(0L);
    }

    Optional<KeyValueIterator<Windowed<CheckUnitKey>, CheckUnitResult>> getArrangementResultsIterator(Long arrangementId) {
        return getResultsKeyValueStore().map(store -> store.fetch(
                new CheckUnitKey(arrangementId, minValue, minValue),
                new CheckUnitKey(arrangementId, maxValue, maxValue),
                Instant.now().minus(resultsRetentionDays, ChronoUnit.DAYS),
                Instant.now()
            )
        );
    }

    Optional<KeyValueIterator<Windowed<CheckUnitKey>, Screenshots>> getArrangementResultScreenshotsIterator(Long arrangementId) {
        return getScreenshotsKeyValueStore().map(store -> store.fetch(
                new CheckUnitKey(arrangementId, minValue, minValue),
                new CheckUnitKey(arrangementId, maxValue, maxValue),
                Instant.now().minus(resultsRetentionDays, ChronoUnit.DAYS),
                Instant.now()
                )
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

    private Optional<ReadOnlyWindowStore<CheckUnitKey, CheckUnitResult>> getResultsKeyValueStore() {
        return Optional.ofNullable(resultsStore == null ?
                interactiveQueryService.getQueryableStore(
                        resultsTableName,
                        QueryableStoreTypes.windowStore()
                ) : resultsStore);
    }

    private Optional<ReadOnlyWindowStore<CheckUnitKey, Screenshots>> getScreenshotsKeyValueStore() {
        return Optional.ofNullable(screenshotsStore == null ?
                interactiveQueryService.getQueryableStore(
                        screenshotsTableName,
                        QueryableStoreTypes.windowStore()
                ) : screenshotsStore);
    }

    private <K, V> Optional<KeyValue<K, V>> getLastIteratorValue(KeyValueIterator<Windowed<K>, V> resultsIterator){
        return StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(resultsIterator, Spliterator.ORDERED), false)
            .map(val -> KeyValue.pair(val.key.key(), val.value))
            .reduce((first, second) -> second);
    }
}
