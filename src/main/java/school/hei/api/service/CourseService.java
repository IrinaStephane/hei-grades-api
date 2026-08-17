package school.hei.api.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.api.model.Course;
import school.hei.api.model.dto.CourseCreation;
import school.hei.api.model.exception.ConflictException;
import school.hei.api.model.exception.NotFoundException;
import school.hei.api.repository.CourseRepository;

@Service
@RequiredArgsConstructor
public class CourseService {

  private final CourseRepository courseRepository;

  public List<Course> getAll() {
    return courseRepository.findAll();
  }

  public Course getById(String id) {
    return getEntityById(id);
  }

  @Transactional
  public Course create(CourseCreation creation) {
    if (courseRepository.existsByCode(creation.getCode())) {
      throw new ConflictException("Course " + creation.getCode() + " already exists");
    }
    Course course =
        Course.builder()
            .code(creation.getCode())
            .title(creation.getTitle())
            .credits(creation.getCredits())
            .build();
    return courseRepository.save(course);
  }

  @Transactional
  public Course update(String id, CourseCreation creation) {
    Course course = getEntityById(id);
    course.setCode(creation.getCode());
    course.setTitle(creation.getTitle());
    course.setCredits(creation.getCredits());
    return courseRepository.save(course);
  }

  @Transactional
  public void delete(String id) {
    if (!courseRepository.existsById(id)) {
      throw new NotFoundException("Course " + id + " not found");
    }
    courseRepository.deleteById(id);
  }

  private Course getEntityById(String id) {
    return courseRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Course " + id + " not found"));
  }
}