package services.traffic;

import checkUnits.CheckUnitType;
import exceptions.AS_15_8_PPT_Exception;
import liquibase.util.StringUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.traffic.*;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import repositories.CustomErdiRepository;
import repositories.CustomErdiViewRepository;

import javax.annotation.PostConstruct;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CustomErdiService {

    private static final String TRAFFIC_ID_COLUMN = "id";

    private final CustomErdiRepository customErdiRepository;
    private final CustomErdiViewRepository viewRepository;
    private final TrafficService trafficService;

    private List<SingularAttribute<CustomErdiView, String>> searchQueryColumns;

    @PostConstruct
    public void init() {
        searchQueryColumns = new ArrayList<>(3);
        searchQueryColumns.add(CustomErdiView_.name);
        searchQueryColumns.add(CustomErdiView_.unitValue);
        searchQueryColumns.add(CustomErdiView_.unitType);
    }

    public Page<CustomErdiView> getCustomErdiView(Pageable pageable, String query,
                                                  boolean containsInTraffic,
                                                  Long erdiTrafficUnitId,
                                                  Long searchTrafficUnitId) {

        Specification<CustomErdiView> specification = getSpecification(query,
                containsInTraffic, erdiTrafficUnitId, searchTrafficUnitId);
        return specification == null ? viewRepository.findAll(pageable) :
                viewRepository.findAll(specification, pageable);
    }

    private Specification<CustomErdiView> getSpecification(String query,
                                                           boolean containsInTraffic,
                                                           Long erdiTrafficUnitId,
                                                           Long searchTrafficUnitId) {
        if (erdiTrafficUnitId != null)
            return containsInTrafficUnit(query, containsInTraffic, erdiTrafficUnitId,
                    CustomErdiView_.erdiTrafficUnits);
        else if (searchTrafficUnitId != null)
            return containsInTrafficUnit(query, containsInTraffic, searchTrafficUnitId,
                    CustomErdiView_.searchQueryPatterns);
        else if (query != null && query.trim().length() > 0)
            return (root, criteriaQuery, criteriaBuilder) ->
                    predicateContainsQuery(criteriaBuilder, root, query);

        return null;
    }

    private <T> Specification<CustomErdiView> containsInTrafficUnit(String query, boolean containsInTraffic,
                                                                    @NonNull Long trafficUnitId,
                                                                    ListAttribute<CustomErdiView, T> joinColumn) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            Predicate predicate;

            if (containsInTraffic) {
                Join<CustomErdiView, T> join = root.join(joinColumn, JoinType.LEFT);
                predicate = criteriaBuilder.equal(join.get(TRAFFIC_ID_COLUMN), trafficUnitId);
            } else {
                Subquery<ErdiTrafficUnitCustom> sub = criteriaQuery.subquery(ErdiTrafficUnitCustom.class);
                Root<ErdiTrafficUnitCustom> subRoot = sub.from(ErdiTrafficUnitCustom.class);
                sub.select(subRoot);
                sub.where(criteriaBuilder.and(
                        criteriaBuilder.equal(subRoot.get(ErdiTrafficUnitCustom_.trafficUnit).get(ErdiTrafficUnit_.id), trafficUnitId),
                        criteriaBuilder.equal(subRoot.get(ErdiTrafficUnitCustom_.customErdi).get(CustomErdi_.id), root.get("id"))));
                predicate = criteriaBuilder.not(criteriaBuilder.exists(sub));
            }

            return StringUtils.isEmpty(query) ? predicate : criteriaBuilder.and(
                    predicate, predicateContainsQuery(criteriaBuilder, root, query));
        };
    }

    private Predicate predicateContainsQuery(CriteriaBuilder cb, Root<CustomErdiView> root, String query) {
        String likeQuery = "%" + query + "%";

        Predicate[] likePredicates = searchQueryColumns.stream()
                .map(column -> cb.like(cb.lower(root.get(column)), likeQuery.toLowerCase()))
                .toArray(Predicate[]::new);

        return cb.or(likePredicates);
    }

    public CustomErdi createCustomErdi(CustomErdi customErdi) {
        customErdi.getCustomErdiUnits().forEach(
                unit -> unit.setCustomErdi(customErdi));
        CustomErdi erdi = customErdiRepository.save(customErdi);
        trafficService.actualizeCheckUnitsCount(erdi);
        return erdi;
    }

    public CustomErdi getCustomErdiById(Long id) {
        return customErdiRepository.findById(id).orElseThrow(() ->
                new AS_15_8_PPT_Exception("Custom ERDI was not found by id: " + id));
    }

    public CustomErdi updateCustomErdi(CustomErdi newCustomErdi,
                                       CustomErdi customErdi) {
        customErdi.setName(newCustomErdi.getName());
        customErdi.setSubtypeId(newCustomErdi.getSubtypeId());
        customErdi.getCustomErdiUnits().clear();
        newCustomErdi.getCustomErdiUnits().forEach(customErdiUnit -> customErdiUnit.setCustomErdi(customErdi));
        customErdi.getCustomErdiUnits().addAll(newCustomErdi.getCustomErdiUnits());
        CustomErdi erdi = customErdiRepository.save(customErdi);
        trafficService.actualizeCheckUnitsCount(erdi);
        return erdi;
    }

    public void deleteCustomErdi(Long id) {
        customErdiRepository.deleteById(id);
        customErdiRepository.findById(id).ifPresent(customErdi ->
                trafficService.actualizeCheckUnitsCount(customErdi));
    }

    public Set<CustomErdi> createCustomErdisFromFile(MultipartFile file, List<String> blackList) {
        try (InputStream inputStream = file.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines()
                    .filter(line -> !blackList.contains(line))
                    .map(this::createAndSaveCustomErdi)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new AS_15_8_PPT_Exception(String.format("Не удалось прочитать файл %s", file.getOriginalFilename()));
        }
    }

    public List<CustomErdi> createCustomErdisFromFile(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines()
                    .map(this::createAndSaveCustomErdi)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new AS_15_8_PPT_Exception(String.format("Не удалось прочитать файл %s", file.getOriginalFilename()));
        }
    }

    private CustomErdi createAndSaveCustomErdi(String address) {
        CheckUnitType checkUnitType;
        try {
            checkUnitType = getCheckUnitType(address);
        } catch (URISyntaxException e) {
            log.warn("Не удалось определить CheckUnitType для ресурса {}. Ресурс не будет добавлен к мероприятию", address);
            return null;
        }
        CustomErdi customErdi = createCustomErdi(address, checkUnitType);
        try {
            return customErdiRepository.save(customErdi);
        } catch (Exception e) {
            log.warn("CustomErdi для ресурса {} уже существует", address);
            return null;
        }
    }

    private CustomErdi createCustomErdi(String address, CheckUnitType checkUnitType) {
        String customErdiName = address.startsWith("xn--") ? IDN.toUnicode(address) : address;
        CustomErdi customErdi = new CustomErdi()
                .setName(customErdiName);
        customErdi.addCustomErdiUnit(new CustomErdiUnit()
                .setType(checkUnitType)
                .setValue(address));
        return customErdi;
    }


    private CheckUnitType getCheckUnitType(String address) throws URISyntaxException {
        UrlValidator urlValidator = new UrlValidator();
        if (urlValidator.isValid(address)) {
            return CheckUnitType.URL;
        } else {
            URI uri = new URI(address);
            return CheckUnitType.DOMAIN;
        }
    }
}
