package school.hei.api.endpoint.rest.mapper;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.api.model.Course;
import school.hei.api.model.dto.CourseRest;

@Component
@AllArgsConstructor
public class CourseMapper {

  public CourseRest toRest(Course course) {
    return CourseRest.builder()
        .id(course.getId())
        .code(course.getCode())
        .title(course.getTitle())
        .credits(course.getCredits())
        .build();
  }

  public List<CourseRest> toRest(List<Course> courses) {
    return courses.stream().map(this::toRest).toList();
  }
}