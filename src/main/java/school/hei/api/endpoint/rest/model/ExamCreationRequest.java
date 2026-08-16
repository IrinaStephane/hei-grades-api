package school.hei.api.endpoint.rest.model;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record ExamCreationRequest(
    @NotBlank String courseAssignmentId,
    @NotBlank String title,
    @NotNull Instant examinationDate,
    @NotNull @DecimalMin(value = "0.0", inclusive = false) @DecimalMax("1.0") Double coefficient) {}
