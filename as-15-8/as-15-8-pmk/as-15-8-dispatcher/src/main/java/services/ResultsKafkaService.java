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
import java.util.stream.StreamSupport;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class ResultsKafkaService {

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
        return getArrangementResultsIterator(arrangementId)
                .map(resultsIterator ->  {
                    Comparator<Result> checkUnitResultComparator = createResultsComparator(sortingColumn);
                    if(sortingDirection != null && sortingDirection.equals(SortingDirection.DESC))
                        checkUnitResultComparator = checkUnitResultComparator.reversed();
                    List<Result> results = StreamSupport
                            .stream(Spliterators.spliteratorUnknownSize(resultsIterator, Spliterator.ORDERED), false)
                            .filter(filter)
                            .map(kv -> {
                                DetailResultService<? super CheckUnitResult, ? extends DetailResult> service = AnalysisResultServiceFactory.getService(kv.value.getClass());
                                Result result = new Result();
                                fillResult(result, kv.key.getJobId(), kv.value, service);
                                return result;
                            })
                            .sorted(checkUnitResultComparator)
                            .skip(pageable.getOffset())
                            .limit(pageable.getPageSize())
                            .collect(Collectors.toList());
                    return new PageImpl<>(results, pageable, count);
                })
                .orElseGet(() -> new PageImpl<>(new ArrayList<>(), pageable, 0));
    }

    public Optional<CheckUnitResult> getArrangementResult(Long arrangementId, Long jobId) {
        return getResultsKeyValueStore()
                .map(store -> store.get(new CheckUnitKey(arrangementId, jobId)));
    }

    public Optional<Screenshots> getScreenshot(Long arrangementId, Long jobId) {
        return getScreenshotsKeyValueStore()
                .map(store -> store.get(new CheckUnitKey(arrangementId, jobId)));
    }

    public long getResultsCount(Long arrangementId) {
        return getResultsCountKeyValueStore()
                .map(store -> store.get(arrangementId))
                .orElse(0L);
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
                        .filter(filter)
                        .count())
                .orElse(0L);
    }

    Optional<KeyValueIterator<CheckUnitKey, CheckUnitResult>> getArrangementResultsIterator(Long arrangementId) {
        return getResultsKeyValueStore().map(store -> store.range(
                new CheckUnitKey(arrangementId, Long.MIN_VALUE),
                new CheckUnitKey(arrangementId, Long.MAX_VALUE))
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
            /*return (o1, o2) -> {
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
            };*/
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
}
