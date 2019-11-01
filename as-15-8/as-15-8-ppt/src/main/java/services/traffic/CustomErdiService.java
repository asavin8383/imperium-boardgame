package services.traffic;

import exceptions.AS_15_8_Exception;
import liquibase.util.StringUtils;
import lombok.NonNull;
import model.traffic.CustomErdi;
import model.traffic.CustomErdiView;
import model.traffic.CustomErdiView_;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import repositories.CustomErdiRepository;
import repositories.CustomErdiViewRepository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import java.util.ArrayList;
import java.util.List;

@Service
public class CustomErdiService {

    private final List<SingularAttribute<CustomErdiView, String>> searchQueryColumns;

    private final CustomErdiRepository customErdiRepository;
    private final CustomErdiViewRepository viewRepository;

    @Autowired
    public CustomErdiService(CustomErdiRepository customErdiRepository,
                             CustomErdiViewRepository viewRepository) {
        this.customErdiRepository = customErdiRepository;
        this.viewRepository = viewRepository;

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
            return containsInTrafficUnit(query, containsInTraffic, erdiTrafficUnitId,
                    CustomErdiView_.searchQueryTrafficUnits);
        else if (query != null && query.trim().length() > 0)
            return (root, criteriaQuery, criteriaBuilder) ->
                    predicateContainsQuery(criteriaBuilder, root, query);

        return null;
    }

    private <T> Specification<CustomErdiView> containsInTrafficUnit(String query, boolean containsInTraffic,
                                                                    @NonNull Long trafficUnitId,
                                                                    ListAttribute<CustomErdiView, T> joinColumn) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            Join<CustomErdiView, T> join = root.join(joinColumn);
            Predicate predicate = criteriaBuilder.equal(
                    join.get("trafficUnitId"), trafficUnitId);
            if ( !containsInTraffic )
                predicate = criteriaBuilder.or(criteriaBuilder.not(predicate),
                        criteriaBuilder.isNull(join.get("trafficUnitId"))
                );

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
        return customErdiRepository.save(customErdi);
    }

    public CustomErdi getCustomErdiById(Long id) {
        return customErdiRepository.findById(id).orElseThrow(() ->
                new AS_15_8_Exception("Custom ERDI was not found by id: " + id));
    }

    public CustomErdi updateCustomErdi(CustomErdi newCustomErdi,
                                       CustomErdi customErdi) {
        customErdi.setName(newCustomErdi.getName());
        customErdi.setViolation(newCustomErdi.getViolation());
        //customErdi.setCustomErdiUnits(newCustomErdi.getCustomErdiUnits());
        return customErdiRepository.save(customErdi);
    }

    public void deleteCustomErdi(Long id) {
        customErdiRepository.deleteById(id);
    }
}
