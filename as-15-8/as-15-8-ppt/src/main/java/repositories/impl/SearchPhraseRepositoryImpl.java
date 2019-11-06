package repositories.impl;

import model.traffic.*;
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

    @Override
    protected List<Predicate> getPredicates(CriteriaQuery query, Root<SearchPhrase> root,
                                            Class<SearchPhrase> clazz, SearchPhraseParams params) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        List<Predicate> predicates = new ArrayList<>();

        if (params.isContainsInTrafficUnit()) {
            Join<SearchPhrase, SearchQueryTrafficUnit> trafficUnitJoin =
                    root.join(SearchPhrase_.trafficUnits, JoinType.LEFT);
            predicates.add(cb.equal(trafficUnitJoin.get(SearchQueryTrafficUnit_.id), params.getSearchTrafficUnitId()));
        } else {
            Subquery<SearchQueryTrafficUnitPhrase> sub = query.subquery(SearchQueryTrafficUnitPhrase.class);
            Root<SearchQueryTrafficUnitPhrase> subRoot = sub.from(SearchQueryTrafficUnitPhrase.class);
            sub.select(subRoot);
            sub.where(cb.and(
                    cb.equal(subRoot.get(SearchQueryTrafficUnitPhrase_.trafficUnit).get(SearchQueryTrafficUnit_.id), params.getSearchTrafficUnitId()),
                    cb.equal(subRoot.get(SearchQueryTrafficUnitPhrase_.searchPhrase).get(SearchPhrase_.id), root.get(SearchPhrase_.id))));
            predicates.add(cb.not(cb.exists(sub)));
        }

        if (params.getPhrase() != null && params.getPhrase().length() > 0) {
            predicates.add(cb.like(cb.upper(root.get("phrase")),
                    '%' + params.getPhrase().toUpperCase() + '%'));
        }

        return predicates;
    }

}