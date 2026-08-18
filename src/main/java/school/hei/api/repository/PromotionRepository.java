package school.hei.api.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.api.model.Promotion;

public interface PromotionRepository extends JpaRepository<Promotion, String> {

  Optional<Promotion> findByRef(String ref);

  boolean existsByRef(String ref);
}
