package school.hei.api.endpoint.rest.model;

public class ApiExceptions {

  private ApiExceptions() {}

  public static class NotFoundException extends RuntimeException {
    public NotFoundException(String resource, String id) {
      super(resource + " with id " + id + " not found");
    }
  }

  public static class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
      super(message);
    }
  }

  public static class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
      super(message);
    }
  }
}
