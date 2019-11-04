package repositories;

import model.DomainMaskItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by san
 * Date: 04.11.2019
 */
public interface DomainMaskItemRepo extends JpaRepository<DomainMaskItem, Long> {

    List<DomainMaskItem> findAllByDomainMask(String domainMask);
}
