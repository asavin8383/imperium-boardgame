package repositories.impl;

import lombok.RequiredArgsConstructor;
import model.scheme.Subtype;
import model.scheme.Subtype_;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import repositories.SubtypeRepositoryCustom;
import repositories.helper.GenericRelevantQuery;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.SingularAttribute;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
public class SubtypeRepositoryImpl implements SubtypeRepositoryCustom {

    private final EntityManager em;

    private GenericRelevantQuery<Subtype> relevantQuery;

    @PostConstruct
    private void init() {
        relevantQuery = new GenericRelevantQuery<>(em, Subtype.class);
    }

    // to do js - create field

    private List<SingularAttribute<Subtype, String>> getLikeAttrs() {
        List<SingularAttribute<Subtype, String>> likeAttrs =
                new ArrayList<>(3);
        likeAttrs.add(Subtype_.violationName);
        likeAttrs.add(Subtype_.categoryName);
        likeAttrs.add(Subtype_.registryName);
        likeAttrs.add(Subtype_.origId);
        return likeAttrs;
    }

    @Override
    public Page<Subtype> findByEffDtAndQuery(Date effDt, String query, Pageable pageable) {
        return relevantQuery.query(effDt, Subtype_.effDt, query, getLikeAttrs(), pageable);
    }
}