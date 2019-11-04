package repositories;

import model.traffic.CustomErdi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomErdiRepository extends JpaRepository<CustomErdi, Long> {

    /*@Query(value="SELECT a.* "
            + "FROM author a left outer join mappable_natural_person p on a.id = p.provenance_id "
            + "WHERE p.update_time is null OR (p.provenance_name='biblio_db' and a.update_time>p.update_time)"
            + "ORDER BY a.id \n#pageable\n",
            *//*countQuery="SELECT count(a.*) "
                + "FROM author a left outer join mappable_natural_person p on a.id = p.provenance_id "
                + "WHERE p.update_time is null OR (p.provenance_name='biblio_db' and a.update_time>p.update_time) \n#pageable\n",*//*
            nativeQuery=true)
    Page<CustomErdiView>  findAuthorsUpdatedAndNew(Pageable pageable);*/

}
