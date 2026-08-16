package school.hei.api.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.api.repository.model.Exam;

@Repository
public interface ExamRepository extends JpaRepository<Exam, String> {
  List<Exam> findByCourseAssignmentId(String courseAssignmentId);
}
