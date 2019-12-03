package repositories;

import model.projection.ContentView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
            boolean random,
            Pageable pageable
   );
}
