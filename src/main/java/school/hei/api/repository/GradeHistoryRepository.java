package school.hei.api.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.api.repository.model.GradeHistory;

@Repository
public interface GradeHistoryRepository extends JpaRepository<GradeHistory, String> {
  List<GradeHistory> findByGradeIdOrderByChangedAtDesc(String gradeId);
}
