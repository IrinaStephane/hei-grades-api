package school.hei.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import school.hei.api.model.Course;
import school.hei.api.model.dto.CourseCreation;
import school.hei.api.model.exception.ConflictException;
import school.hei.api.model.exception.NotFoundException;
import school.hei.api.repository.CourseRepository;

class CourseServiceTest {

  private final CourseRepository courseRepository = mock(CourseRepository.class);
  private final CourseService subject = new CourseService(courseRepository);

  private static Course aCourse() {
    return Course.builder().id("c1").code("CODE1").title("Title").credits(3).build();
  }

  @Test
  void get_all_ok() {
    var courses = List.of(aCourse());
    when(courseRepository.findAll()).thenReturn(courses);

    assertEquals(courses, subject.getAll());
  }

  @Test
  void get_by_id_ok() {
    when(courseRepository.findById("c1")).thenReturn(Optional.of(aCourse()));

    assertEquals("c1", subject.getById("c1").getId());
  }

  @Test
  void get_by_unknown_id_not_found() {
    when(courseRepository.findById("nope")).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> subject.getById("nope"));
  }

  @Test
  void create_course_ok() {
    var creation = CourseCreation.builder().code("NEW1").title("New").credits(5).build();
    when(courseRepository.existsByCode("NEW1")).thenReturn(false);
    when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

    var created = subject.create(creation);

    assertEquals("NEW1", created.getCode());
    assertEquals("New", created.getTitle());
    assertEquals(5, created.getCredits());
  }

  @Test
  void create_course_with_taken_code_conflicts() {
    var creation = CourseCreation.builder().code("CODE1").title("New").credits(5).build();
    when(courseRepository.existsByCode("CODE1")).thenReturn(true);

    assertThrows(ConflictException.class, () -> subject.create(creation));
    verify(courseRepository, never()).save(any());
  }

  @Test
  void update_course_ok() {
    var course = aCourse();
    when(courseRepository.findById("c1")).thenReturn(Optional.of(course));
    when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

    var updated =
        subject.update("c1", CourseCreation.builder().code("CODE2").title("T2").credits(6).build());

    assertEquals("CODE2", updated.getCode());
    assertEquals("T2", updated.getTitle());
    assertEquals(6, updated.getCredits());
  }

  @Test
  void delete_course_ok() {
    when(courseRepository.existsById("c1")).thenReturn(true);

    subject.delete("c1");

    verify(courseRepository).deleteById("c1");
  }

  @Test
  void delete_unknown_course_not_found() {
    when(courseRepository.existsById("nope")).thenReturn(false);

    assertThrows(NotFoundException.class, () -> subject.delete("nope"));
  }
}