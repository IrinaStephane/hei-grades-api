package school.hei.api.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.api.repository.model.Grade;

@Repository
public interface GradeRepository extends JpaRepository<Grade, String> {
  List<Grade> findByStudentId(String studentId);

  List<Grade> findByExamId(String examId);

  List<Grade> findByStudentIdAndExamId(String studentId, String examId);

  boolean existsByExamIdAndStudentId(String examId, String studentId);

  List<Grade> findByStudentIdAndIsFinalTrue(String studentId);

  @Query(
      "select g from Grade g where g.examId in (select e.id from Exam e where e.courseAssignmentId"
          + " in :courseAssignmentIds)")
  List<Grade> findByExamCourseAssignmentIds(
      @Param("courseAssignmentIds") List<String> courseAssignmentIds);
}
