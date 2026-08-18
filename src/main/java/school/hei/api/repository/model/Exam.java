package school.hei.api.repository.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exam {
  @Id private String id;
  private String courseAssignmentId;
  private String title;
  private Instant examinationDate;
  private Double coefficient;
}
