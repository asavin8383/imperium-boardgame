package repositories.impl;

import model.traffic.SearchPhrase;
import model.traffic.SearchPhrase_;
import model.traffic.SearchQueryTrafficUnit;
import model.traffic.jointable.SearchQueryTrafficUnitPhrase;
import org.springframework.beans.factory.annotation.Autowired;
import repositories.SearchPhraseRepositoryCustom;
import repositories.helpers.JoinCriteriaHelper;
import repositories.helpers.SearchPhraseParams;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

public class SearchPhraseRepositoryImpl extends JoinCriteriaHelper<SearchPhrase, SearchPhraseParams>
        implements SearchPhraseRepositoryCustom {

    public SearchPhraseRepositoryImpl(@Autowired EntityManager em) {
        super(em);
    }

    // copy-paste ...

    @Override
    protected List<Predicate> getPredicates(CriteriaQuery query, Root<SearchPhrase> root,
                                            Class<SearchPhrase> clazz, SearchPhraseParams params) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        List<Predicate> predicates = new ArrayList<>();

        if (params.getTrafficUnitId() != null) {

            if (params.isBelongsTo()) {
                Join<SearchPhrase, SearchQueryTrafficUnit> trafficUnitJoin =
                        root.join(SearchPhrase_.trafficUnits, JoinType.LEFT);
                predicates.add(cb.equal(
                        trafficUnitJoin.get("id"), params.getTrafficUnitId()));
            } else {
                Subquery<Long> sub = query.subquery(Long.class);
                Root<SearchQueryTrafficUnitPhrase> subRoot = sub.from(SearchQueryTrafficUnitPhrase.class);
                sub.select(subRoot.get("id"));
                sub.where(cb.and(
                        cb.equal(subRoot.get("trafficUnitId"), params.getTrafficUnitId()),
                        cb.equal(subRoot.get("searchPhraseId"), root.get("id"))));
                predicates.add(cb.not(cb.exists(sub)));
            }
        }

        if (params.getPhrase() != null && params.getPhrase().length() > 0) {
            predicates.add(cb.like(cb.upper(root.get("phrase")),
                    '%' + params.getPhrase().toUpperCase() + '%'));
        }

        if (params.getViolationId() != null) {
            predicates.add(cb.equal(root.get("violation"), params.getViolationId()));
        }

        return predicates;
    }

}