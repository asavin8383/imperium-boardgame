package repositories;

import model.traffic.CustomErdi;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomErdiRepository extends PagingAndSortingRepository<CustomErdi, Long> {
}
