package school.hei.api.endpoint.rest.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import school.hei.api.model.exception.ApiException;

@RestControllerAdvice
public class RestExceptionHandler {

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<RestExceptionBody> handleApiException(ApiException exception) {
    HttpStatus status =
        switch (exception.getType()) {
          case BAD_REQUEST -> HttpStatus.BAD_REQUEST;
          case NOT_FOUND -> HttpStatus.NOT_FOUND;
          case FORBIDDEN -> HttpStatus.FORBIDDEN;
          case CONFLICT -> HttpStatus.CONFLICT;
        };
    RestExceptionBody body =
        new RestExceptionBody(exception.getType().name(), exception.getMessage());
    return ResponseEntity.status(status).body(body);
  }

  // matches the RestException schema declared in api.yml
  public record RestExceptionBody(String type, String message) {}
}