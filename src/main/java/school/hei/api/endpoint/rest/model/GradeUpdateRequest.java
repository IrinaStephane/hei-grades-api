package school.hei.api.endpoint.rest.model;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GradeUpdateRequest(
    @NotNull @DecimalMin("0.0") @DecimalMax("20.0") Double score,
    @NotBlank(message = "comment is mandatory when changing a grade") String comment,
    Boolean isFinal) {}
