package school.hei.api.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.api.model.CourseAssignment;

public interface CourseAssignmentRepository extends JpaRepository<CourseAssignment, String> {

  List<CourseAssignment> findByTeacherId(String teacherId);

  List<CourseAssignment> findByGroupId(String groupId);

  List<CourseAssignment> findByCourseId(String courseId);

  List<CourseAssignment> findByGroupIdAndYearAndSemester(
      String groupId, Integer year, Integer semester);

  boolean existsByIdAndTeacherId(String id, String teacherId);

  boolean existsByCourseIdAndTeacherIdAndGroupIdAndYearAndSemester(
      String courseId, String teacherId, String groupId, Integer year, Integer semester);
}
