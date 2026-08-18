package school.hei.api.integration.conf;

/** Connection and id concerns only: how a test reaches the API and what unusable ids look like. */
public class TestUtils {
  public static final String BAD_TOKEN = "bad_token";
  public static final String NOT_EXISTING_ID = "not_existing_id";

  public static String apiUrl(int serverPort, String path) {
    return "http://localhost:" + serverPort + path;
  }
}
