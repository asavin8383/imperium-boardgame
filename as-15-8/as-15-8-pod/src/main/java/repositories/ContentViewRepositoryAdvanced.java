package repositories;

import model.projection.ContentView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by san
 * Date: 02.12.2019
 */
public interface ContentViewRepositoryAdvanced {

    Page<ContentView> findPage(
            String id,
            List<String> categoryNames,
            List<String> decisionOrgs,
            List<String> infoTypeIds,
            List<String> registryNames,
            List<String> resourceTypes,
            String resourceValue,
            List<String> violationNames,
            String query,
            Boolean random,
            Pageable pageable,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Long visitorsCntRussiaMin,
            Long visitorsCntRussiaMax,
            Long visitorsCntWorldMin,
            Long visitorsCntWorldMax
    );

    List<Long> findIds(
            String idMask,
            List<String> categoryNames,
            List<String> decisionOrgs,
            List<String> infoTypeIds,
            List<String> registryNames,
            List<String> resourceTypes,
            String resourceValue,
            List<String> violationNames,
            Integer size,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Boolean random,
            Pageable pageable,
            Long visitorsCntRussiaMin,
            Long visitorsCntRussiaMax,
            Long visitorsCntWorldMin,
            Long visitorsCntWorldMax,
            String query
    );
}
