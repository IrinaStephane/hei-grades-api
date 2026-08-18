package school.hei.api.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.api.model.Group;
import school.hei.api.model.enums.Path;

public interface GroupRepository extends JpaRepository<Group, String> {

  List<Group> findByPromotionId(String promotionId);

  List<Group> findByPromotionIdAndPath(String promotionId, Path path);

  List<Group> findByPath(Path path);
}
