package repositories;

import enums.Dictionary;
import model.scheme.Domain;
import model.scheme.DomainMask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import repositories.helper.DictionaryRepository;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by san
 * Date: 13.11.2019
 */
@Repository
public interface DomainMaskRepo extends JpaRepository<DomainMask, Long> {

    @Query("select d from DomainMask d where d.domainMask = :domainMask ")
    List<DomainMask> findMasks(@Param("domainMask") String domainMask);

    @Query("select p from Domain p")
    //Page<Domain> findPage(PageRequest page);
    Page<Domain> findDomainsPage(Pageable page);

    @Query("select p from DomainMask p")
        //Page<DomainMask> findDomainMasksPage(PageRequest page);
        Page<DomainMask> findDomainMasksPage(Pageable page);

    //@Query("select p from Domain p where p.domainMask like concat('%',:domainMask,'%')")
    @Query("select p from DomainMask p where p.domainMask like concat('%',:domainMask,'%')")
    Page<DomainMask> findDomainMasksPage(@Param("domainMask") String domainMask,  Pageable page);

    @Query("select p from DomainMask p where p.domainMask like concat('%',:domainMask,'%')")
    //Set<DomainMask> findAllDomainsByMask(@Param("domainMask") String domainMask);
    //Set<DomainMask> findAllDomainsByMask(@Param("domainMask") String domainMask);
    Set<DomainMask> findAllDomainMasksLike(@Param("domainMask") String domainMask);
}
