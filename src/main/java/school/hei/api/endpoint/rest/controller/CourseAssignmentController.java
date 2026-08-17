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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import school.hei.api.endpoint.rest.mapper.CourseAssignmentMapper;
import school.hei.api.model.dto.CourseAssignmentCreation;
import school.hei.api.model.dto.CourseAssignmentRest;
import school.hei.api.service.CourseAssignmentService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/course_assignments")
public class CourseAssignmentController {

  private final CourseAssignmentService courseAssignmentService;
  private final CourseAssignmentMapper courseAssignmentMapper;

  @GetMapping
  public List<CourseAssignmentRest> getCourseAssignments(
      @RequestParam(required = false) String teacherId,
      @RequestParam(required = false) String groupId,
      @RequestParam(required = false) String courseId) {
    return courseAssignmentMapper.toRest(
        courseAssignmentService.getAll(teacherId, groupId, courseId));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('ADMIN')")
  public CourseAssignmentRest createCourseAssignment(
      @Valid @RequestBody CourseAssignmentCreation creation) {
    return courseAssignmentMapper.toRest(courseAssignmentService.create(creation));
  }

  @GetMapping("/{id}")
  public CourseAssignmentRest getCourseAssignmentById(@PathVariable String id) {
    return courseAssignmentMapper.toRest(courseAssignmentService.getById(id));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public CourseAssignmentRest updateCourseAssignment(
      @PathVariable String id, @Valid @RequestBody CourseAssignmentCreation creation) {
    return courseAssignmentMapper.toRest(courseAssignmentService.update(id, creation));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ADMIN')")
  public void deleteCourseAssignment(@PathVariable String id) {
    courseAssignmentService.delete(id);
  }
}