package school.hei.api.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.api.model.Course;

public interface CourseRepository extends JpaRepository<Course, String> {

  Optional<Course> findByCode(String code);

  boolean existsByCode(String code);
}
