package school.hei.api.endpoint.rest.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import school.hei.api.model.dto.CourseCreation;
import school.hei.api.model.dto.CourseRest;
import school.hei.api.service.CourseService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/courses")
public class CourseController {

  private final CourseService courseService;

  @GetMapping
  public List<CourseRest> getCourses() {
    return courseService.getAll();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('ADMIN')")
  public CourseRest createCourse(@Valid @RequestBody CourseCreation creation) {
    return courseService.create(creation);
  }

  @GetMapping("/{id}")
  public CourseRest getCourseById(@PathVariable String id) {
    return courseService.getById(id);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public CourseRest updateCourse(
      @PathVariable String id, @Valid @RequestBody CourseCreation creation) {
    return courseService.update(id, creation);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ADMIN')")
  public void deleteCourse(@PathVariable String id) {
    courseService.delete(id);
  }
}