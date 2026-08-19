package school.hei.api.endpoint.rest.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import school.hei.api.endpoint.rest.mapper.CourseMapper;
import school.hei.api.model.dto.CourseCreation;
import school.hei.api.model.dto.CourseRest;
import school.hei.api.service.CourseService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/courses")
public class CourseController {

  private final CourseService courseService;
  private final CourseMapper courseMapper;

  @GetMapping
  public List<CourseRest> getCourses() {
    return courseMapper.toRest(courseService.getAll());
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CourseRest createCourse(@Valid @RequestBody CourseCreation creation) {
    return courseMapper.toRest(courseService.create(creation));
  }

  @GetMapping("/{id}")
  public CourseRest getCourseById(@PathVariable String id) {
    return courseMapper.toRest(courseService.getById(id));
  }

  @PutMapping("/{id}")
  public CourseRest updateCourse(
      @PathVariable String id, @Valid @RequestBody CourseCreation creation) {
    return courseMapper.toRest(courseService.update(id, creation));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteCourse(@PathVariable String id) {
    courseService.delete(id);
  }
}
