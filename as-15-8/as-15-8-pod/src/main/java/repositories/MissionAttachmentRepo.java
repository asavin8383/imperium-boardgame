package repositories;

import model.scheme.MissionAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by san
 * Date: 23.11.2019
 */
@Repository
public interface MissionAttachmentRepo extends JpaRepository<MissionAttachment, Long> {
}
