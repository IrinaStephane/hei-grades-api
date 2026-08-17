package school.hei.api.model.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ExceptionsTest {

  @Test
  void client_exceptions_are_typed_client_exception() {
    assertEquals(ApiException.ExceptionType.CLIENT_EXCEPTION, new BadRequestException("m").getType());
    assertEquals(ApiException.ExceptionType.CLIENT_EXCEPTION, new ConflictException("m").getType());
    assertEquals(ApiException.ExceptionType.CLIENT_EXCEPTION, new ForbiddenException("m").getType());
    assertEquals(ApiException.ExceptionType.CLIENT_EXCEPTION, new NotFoundException("m").getType());
    assertEquals(
        ApiException.ExceptionType.CLIENT_EXCEPTION,
        new TooManyRequestsException("m").getType());
    assertEquals(
        ApiException.ExceptionType.CLIENT_EXCEPTION, new UnauthorizedException("m").getType());
  }

  @Test
  void server_exceptions_are_typed_server_exception() {
    assertEquals(
        ApiException.ExceptionType.SERVER_EXCEPTION, new NotImplementedException("m").getType());
  }

  @Test
  void exceptions_carry_the_message() {
    assertEquals("boom", new ConflictException("boom").getMessage());
  }

  @Test
  void too_many_requests_can_wrap_a_source_exception() {
    var source = new IllegalStateException("source message");
    var exception = new TooManyRequestsException(source);
    assertEquals(ApiException.ExceptionType.CLIENT_EXCEPTION, exception.getType());
    assertEquals("source message", exception.getMessage());
  }
}
