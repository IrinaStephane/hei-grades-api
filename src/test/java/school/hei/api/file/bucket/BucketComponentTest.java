package school.hei.api.file.bucket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import school.hei.api.file.hash.FileHash;
import school.hei.api.file.hash.FileHashAlgorithm;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.CompletedDirectoryUpload;
import software.amazon.awssdk.transfer.s3.model.CompletedFileDownload;
import software.amazon.awssdk.transfer.s3.model.CompletedFileUpload;
import software.amazon.awssdk.transfer.s3.model.DirectoryUpload;
import software.amazon.awssdk.transfer.s3.model.DownloadFileRequest;
import software.amazon.awssdk.transfer.s3.model.FileDownload;
import software.amazon.awssdk.transfer.s3.model.FileUpload;
import software.amazon.awssdk.transfer.s3.model.UploadDirectoryRequest;
import software.amazon.awssdk.transfer.s3.model.UploadFileRequest;

class BucketComponentTest {

  private final BucketConf bucketConf = mock(BucketConf.class);
  private final S3TransferManager transferManager = mock(S3TransferManager.class);
  private final S3Presigner presigner = mock(S3Presigner.class);
  private final BucketComponent subject = new BucketComponent(bucketConf);

  private void configureConf() {
    when(bucketConf.getBucketName()).thenReturn("my-bucket");
    when(bucketConf.getS3TransferManager()).thenReturn(transferManager);
    when(bucketConf.getS3Presigner()).thenReturn(presigner);
  }

  @Test
  void upload_file_returns_sha256_hash() throws Exception {
    configureConf();
    var file = File.createTempFile("upload", ".txt");
    file.deleteOnExit();
    var completedUpload = mock(CompletedFileUpload.class);
    var putObjectResponse = mock(PutObjectResponse.class);
    when(putObjectResponse.checksumSHA256()).thenReturn("checksum");
    when(completedUpload.response()).thenReturn(putObjectResponse);
    var upload = mock(FileUpload.class);
    when(upload.completionFuture()).thenReturn(CompletableFuture.completedFuture(completedUpload));
    when(transferManager.uploadFile(any(UploadFileRequest.class))).thenReturn(upload);

    FileHash hash = subject.upload(file, "bucket/key.txt");

    assertEquals(FileHashAlgorithm.SHA256, hash.algorithm());
    assertEquals("checksum", hash.value());
  }

  @Test
  void upload_directory_returns_none_hash() throws Exception {
    configureConf();
    var dir = Files.createTempDirectory("upload-dir").toFile();
    dir.deleteOnExit();
    var completedDirectoryUpload = mock(CompletedDirectoryUpload.class);
    when(completedDirectoryUpload.failedTransfers()).thenReturn(List.of());
    var upload = mock(DirectoryUpload.class);
    when(upload.completionFuture())
        .thenReturn(CompletableFuture.completedFuture(completedDirectoryUpload));
    when(transferManager.uploadDirectory(any(UploadDirectoryRequest.class))).thenReturn(upload);

    FileHash hash = subject.upload(dir, "bucket/dir");

    assertEquals(FileHashAlgorithm.NONE, hash.algorithm());
  }

  @Test
  void download_creates_temp_file() throws Exception {
    configureConf();
    var download = mock(FileDownload.class);
    when(download.completionFuture())
        .thenReturn(CompletableFuture.completedFuture(mock(CompletedFileDownload.class)));
    when(transferManager.downloadFile(any(DownloadFileRequest.class))).thenReturn(download);

    File file = subject.download("bucket/file.txt");

    assertTrue(file.getName().startsWith("file") && file.getName().endsWith("txt"));
    verify(transferManager).downloadFile(any(DownloadFileRequest.class));
  }

  @Test
  void presign_returns_presigned_url() throws Exception {
    configureConf();
    var presignedRequest = mock(PresignedGetObjectRequest.class);
    var url = new URL("https://bucket.s3.amazonaws.com/key");
    when(presignedRequest.url()).thenReturn(url);
    when(presigner.presignGetObject(any(GetObjectPresignRequest.class)))
        .thenReturn(presignedRequest);

    assertEquals(url, subject.presign("bucket/key", Duration.ofMinutes(5)));
  }

  @Test
  void get_bucket_name_ok() {
    configureConf();

    assertEquals("my-bucket", subject.getBucketName());
  }
}
