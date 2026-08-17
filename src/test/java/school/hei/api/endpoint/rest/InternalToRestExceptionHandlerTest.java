package school.hei.api.endpoint.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import school.hei.api.model.exception.BadRequestException;
import school.hei.api.model.exception.ConflictException;
import school.hei.api.model.exception.ForbiddenException;
import school.hei.api.model.exception.NotFoundException;
import school.hei.api.model.exception.NotImplementedException;
import school.hei.api.model.exception.TooManyRequestsException;
import school.hei.api.model.exception.UnauthorizedException;

class InternalToRestExceptionHandlerTest {

  private final InternalToRestExceptionHandler subject = new InternalToRestExceptionHandler();

  @Test
  void bad_request_is_400() {
    var response = subject.handleBadRequest(new BadRequestException("bad"));
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("400 BAD_REQUEST", response.getBody().getType());
    assertEquals("bad", response.getBody().getMessage());
  }

  @Test
  void method_argument_not_valid_is_400() throws Exception {
    Method method = getClass().getMethod("sample", String.class);
    var parameter = new org.springframework.core.MethodParameter(method, 0);
    BindingResult bindingResult = new org.springframework.validation.BeanPropertyBindingResult(new Object(), "obj");
    var exception =
        new org.springframework.web.bind.MethodArgumentNotValidException(parameter, bindingResult);

    var response = subject.handleMethodArgumentNotValid(exception);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void missing_parameter_is_400() {
    var exception = new MissingServletRequestParameterException("to", "String");

    var response = subject.handleMissingParameter(exception);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void type_mismatch_is_400() {
    Method method;
    try {
      method = getClass().getMethod("sample", String.class);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
    var parameter = new org.springframework.core.MethodParameter(method, 0);
    var exception =
        new MethodArgumentTypeMismatchException("value", Integer.class, "name", parameter, null);

    var response = subject.handleConversionFailed(exception);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void unauthorized_is_401() {
    var response = subject.handleUnauthorized(new UnauthorizedException("nope"));
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertEquals("nope", response.getBody().getMessage());
  }

  @Test
  void forbidden_variants_are_403() {
    assertEquals(HttpStatus.FORBIDDEN, subject.handleForbidden(new AccessDeniedException("denied")).getStatusCode());
    assertEquals(HttpStatus.FORBIDDEN, subject.handleForbidden(new BadCredentialsException("denied")).getStatusCode());
    assertEquals(HttpStatus.FORBIDDEN, subject.handleForbidden(new ForbiddenException("denied")).getStatusCode());
  }

  @Test
  void not_found_is_404() {
    var response = subject.handleNotFound(new NotFoundException("missing"));
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void conflict_is_409() {
    var response = subject.handleConflict(new ConflictException("dup"));
    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
  }

  @Test
  void too_many_requests_is_429() {
    var response = subject.handleTooManyRequests(new TooManyRequestsException("slow down"));
    assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
  }

  @Test
  void lock_acquisition_is_429() {
    var response = subject.handleLockAcquisitionException(new org.springframework.dao.CannotAcquireLockException("locked"));
    assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
    assertEquals("locked", response.getBody().getMessage());
  }

  @Test
  void not_implemented_is_501() {
    var response = subject.handleNotImplemented(new NotImplementedException("later"));
    assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
  }

  @Test
  void any_other_exception_is_500() {
    var response = subject.handleDefault(new IllegalStateException("crash"));
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals("crash", response.getBody().getMessage());
  }

  public void sample(String arg) {}
}
