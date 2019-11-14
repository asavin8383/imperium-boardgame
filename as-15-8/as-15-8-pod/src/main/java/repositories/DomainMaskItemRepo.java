package repositories;

import model.scheme.DomainMaskItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by san
 * Date: 13.11.2019
 */
@Repository
public interface DomainMaskItemRepo extends JpaRepository<DomainMaskItem, Long> {
    List<DomainMaskItem> findAllByDomainMask(String domainMask);
}
