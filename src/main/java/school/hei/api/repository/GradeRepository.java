package school.hei.api.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.api.repository.model.Grade;

@Repository
public interface GradeRepository extends JpaRepository<Grade, String> {
  List<Grade> findByStudentId(String studentId);

  List<Grade> findByExamId(String examId);

  List<Grade> findByStudentIdAndIsFinalTrue(String studentId);
}
