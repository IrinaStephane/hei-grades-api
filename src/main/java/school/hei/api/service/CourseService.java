package school.hei.api.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.api.model.Course;
import school.hei.api.model.dto.CourseCreation;
import school.hei.api.model.dto.CourseRest;
import school.hei.api.model.exception.ApiException;
import school.hei.api.model.exception.ApiExceptionType;
import school.hei.api.repository.CourseRepository;

@Service
@RequiredArgsConstructor
public class CourseService {

  private final CourseRepository courseRepository;

  public List<CourseRest> getAll() {
    return courseRepository.findAll().stream().map(this::toRest).toList();
  }

  public CourseRest getById(String id) {
    return toRest(getEntityById(id));
  }

  @Transactional
  public CourseRest create(CourseCreation creation) {
    if (courseRepository.existsByCode(creation.getCode())) {
      throw new ApiException(
          ApiExceptionType.CONFLICT, "Course " + creation.getCode() + " already exists");
    }
    Course course =
        Course.builder()
            .code(creation.getCode())
            .title(creation.getTitle())
            .credits(creation.getCredits())
            .build();
    return toRest(courseRepository.save(course));
  }

  @Transactional
  public CourseRest update(String id, CourseCreation creation) {
    Course course = getEntityById(id);
    course.setCode(creation.getCode());
    course.setTitle(creation.getTitle());
    course.setCredits(creation.getCredits());
    return toRest(courseRepository.save(course));
  }

  @Transactional
  public void delete(String id) {
    if (!courseRepository.existsById(id)) {
      throw new ApiException(ApiExceptionType.NOT_FOUND, "Course " + id + " not found");
    }
    courseRepository.deleteById(id);
  }

  private Course getEntityById(String id) {
    return courseRepository
        .findById(id)
        .orElseThrow(
            () -> new ApiException(ApiExceptionType.NOT_FOUND, "Course " + id + " not found"));
  }

  private CourseRest toRest(Course course) {
    return CourseRest.builder()
        .id(course.getId())
        .code(course.getCode())
        .title(course.getTitle())
        .credits(course.getCredits())
        .build();
  }
}
