package school.hei.api.integration.conf;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import lombok.SneakyThrows;
import school.hei.api.file.bucket.BucketComponent;
import school.hei.api.file.hash.FileHash;
import school.hei.api.file.hash.FileHashAlgorithm;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;

/** Stubs the third parties the application talks to, for the rows a test owns. */
public class TestMocks {

  public static void setUpEventBridge(EventBridgeClient eventBridgeClient) {
    when(eventBridgeClient.putEvents((PutEventsRequest) any()))
        .thenReturn(PutEventsResponse.builder().build());
  }

  @SneakyThrows
  public static void setUpBucket(
      BucketComponent bucketComponent, AtomicReference<File> uploadedFileRef) {
    when(bucketComponent.upload(any(File.class), any(String.class)))
        .thenAnswer(
            invocation -> {
              File file = invocation.getArgument(0);
              if (file.isDirectory()) {
                return new FileHash(FileHashAlgorithm.NONE, null);
              }
              uploadedFileRef.set(file);
              return new FileHash(FileHashAlgorithm.SHA256, "some-checksum");
            });
    when(bucketComponent.download(any(String.class)))
        .thenAnswer(
            invocation -> {
              var uploaded = uploadedFileRef.get();
              if (uploaded == null) {
                throw new RuntimeException("no file uploaded yet");
              }
              return uploaded;
            });
    when(bucketComponent.presign(any(String.class), any(Duration.class)))
        .thenReturn(new URL("https://presigned.example.com/health.txt"));
  }
}
