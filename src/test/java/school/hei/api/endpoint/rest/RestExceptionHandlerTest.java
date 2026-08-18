package school.hei.api.endpoint.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import school.hei.api.endpoint.rest.controller.RestExceptionHandler;
import school.hei.api.endpoint.rest.model.ApiExceptions.BadRequestException;
import school.hei.api.endpoint.rest.model.ApiExceptions.ForbiddenException;
import school.hei.api.endpoint.rest.model.ApiExceptions.NotFoundException;

class RestExceptionHandlerTest {

  private final RestExceptionHandler subject = new RestExceptionHandler();

  @Test
  void not_found_is_404() {
    var response = subject.handleNotFound(new NotFoundException("Exam", "id-1"));
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertEquals("NOT_FOUND", response.getBody().getType());
    assertEquals("Exam with id id-1 not found", response.getBody().getMessage());
  }

  @Test
  void bad_request_is_400() {
    var response = subject.handleBadRequest(new BadRequestException("invalid input"));
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("BAD_REQUEST", response.getBody().getType());
    assertEquals("invalid input", response.getBody().getMessage());
  }

  @Test
  void forbidden_is_403() {
    var response = subject.handleForbidden(new ForbiddenException("access denied"));
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertEquals("FORBIDDEN", response.getBody().getType());
    assertEquals("access denied", response.getBody().getMessage());
  }

  @Test
  void validation_error_is_400() {
    var br = mock(org.springframework.validation.BindingResult.class);
    var fieldError =
        new org.springframework.validation.FieldError("obj", "email", "must not be blank");
    when(br.getFieldErrors()).thenReturn(List.of(fieldError));
    var exception = new org.springframework.web.bind.MethodArgumentNotValidException(null, br);

    var response = subject.handleValidation(exception);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("BAD_REQUEST", response.getBody().getType());
  }
}
