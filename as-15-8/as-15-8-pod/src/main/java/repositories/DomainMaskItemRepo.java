package repositories;

import model.scheme.DomainMaskItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by san
 * Date: 13.11.2019
 */
@Repository
public interface DomainMaskItemRepo extends JpaRepository<DomainMaskItem, Long> {

    @Query("select d from DomainMaskItem d where d.domainMask = :domainMask ")
    List<DomainMaskItem> findAllByDomainMask(@Param("domainMask") String domainMask);

    @Query("select p from DomainMaskItem p")
    Page<DomainMaskItem> findPage(PageRequest page);

    @Query("select p from DomainMaskItem p where p.domainMask like concat('%',:domainMask,'%')")
    Page<DomainMaskItem> findPage(@Param("domainMask") String domainMask,  Pageable page);
}
