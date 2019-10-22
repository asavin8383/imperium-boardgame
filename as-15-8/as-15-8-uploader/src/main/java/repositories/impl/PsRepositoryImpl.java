package repositories.impl;

import lombok.RequiredArgsConstructor;
import model.scheme.PsRecord;
import model.scheme.PsRecord_;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import repositories.PsRepositoryCustom;
import repositories.helper.GenericRelevantQuery;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.SingularAttribute;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
public class PsRepositoryImpl implements PsRepositoryCustom {

    private final EntityManager em;

    private GenericRelevantQuery<PsRecord> relevantQuery;

    @PostConstruct
    private void init() {
        relevantQuery = new GenericRelevantQuery<>(em, PsRecord.class);
    }

    private List<SingularAttribute<PsRecord, String>> getLikeAttrs() {
        List<SingularAttribute<PsRecord, String>> likeAttrs =
                new ArrayList<>(3);
        likeAttrs.add(PsRecord_.name);
        likeAttrs.add(PsRecord_.hostname);
        // источник данных
        return likeAttrs;
    }

    @Override
    public Page<PsRecord> findByEffDtAndQuery(Date effDt, String query, Pageable pageable) {
        return relevantQuery.query(effDt, PsRecord_.effDt, query, getLikeAttrs(), pageable);
    }
}