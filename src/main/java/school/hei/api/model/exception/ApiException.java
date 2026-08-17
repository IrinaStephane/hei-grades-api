package school.hei.api.model.exception;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {

  private final ApiExceptionType type;

  public ApiException(ApiExceptionType type, String message) {
    super(message);
    this.type = type;
  }
}
