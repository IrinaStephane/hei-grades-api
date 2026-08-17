package school.hei.api.model.exception;

public class UnauthorizedException extends ApiException {
  public UnauthorizedException(String message) {
    super(ExceptionType.CLIENT_EXCEPTION, message);
  }
}