package school.hei.api.endpoint.rest.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import school.hei.api.endpoint.rest.model.ApiExceptions.BadRequestException;
import school.hei.api.endpoint.rest.model.ApiExceptions.ForbiddenException;
import school.hei.api.endpoint.rest.model.ApiExceptions.NotFoundException;
import school.hei.api.endpoint.rest.model.RestException;

@RestControllerAdvice
public class RestExceptionHandler {

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<RestException> handleNotFound(NotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new RestException("NOT_FOUND", e.getMessage()));
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<RestException> handleBadRequest(BadRequestException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new RestException("BAD_REQUEST", e.getMessage()));
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<RestException> handleForbidden(ForbiddenException e) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(new RestException("FORBIDDEN", e.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<RestException> handleValidation(MethodArgumentNotValidException e) {
    String message =
        e.getBindingResult().getFieldErrors().stream()
            .map(err -> err.getField() + ": " + err.getDefaultMessage())
            .reduce((a, b) -> a + "; " + b)
            .orElse("Invalid request");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new RestException("BAD_REQUEST", message));
  }
}
