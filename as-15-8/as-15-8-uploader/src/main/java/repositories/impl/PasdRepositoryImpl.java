package repositories.impl;

import lombok.RequiredArgsConstructor;
import model.scheme.PasdRecord;
import model.scheme.PasdRecord_;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import repositories.PasdRepositoryCustom;
import repositories.helper.GenericRelevantQuery;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.SingularAttribute;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class PasdRepositoryImpl implements PasdRepositoryCustom { // DictionaryRepository<PasdRecord> {

    private final EntityManager em;

    private GenericRelevantQuery<PasdRecord> relevantQuery;

    @PostConstruct
    private void init() {
        relevantQuery = new GenericRelevantQuery<>(em, PasdRecord.class);
    }

    private List<SingularAttribute<PasdRecord, String>> getLikeAttrs() {
        List<SingularAttribute<PasdRecord, String>> likeAttrs =
                new ArrayList<>(3);
        likeAttrs.add(PasdRecord_.name);
        likeAttrs.add(PasdRecord_.hostname);
        // источник данных
        return likeAttrs;
    }

    @Override
    public Page<PasdRecord> findByEffDtAndQuery(LocalDateTime effDt, String query, Pageable pageable) {
        return relevantQuery.query(effDt, PasdRecord_.effDt, query, getLikeAttrs(), pageable);
    }
}