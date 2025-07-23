package tech.lacambra.kmanager.services.file_storage;

import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class MinioScanService {

 private static final Logger logger = LoggerFactory.getLogger(MinioScanService.class);

 private MinioClient minioClient;

 @Inject
 public MinioScanService(MinioClient minioClient) {
  this.minioClient = minioClient;
 }

 public List<MinioFileInfo> scanPath(String bucketName, String path) {
  List<MinioFileInfo> files = new ArrayList<>();

  try {
   ListObjectsArgs listArgs = ListObjectsArgs.builder()
     .bucket(bucketName)
     .prefix(path)
     .recursive(true)
     .build();

   Iterable<Result<Item>> results = minioClient.listObjects(listArgs);

   for (Result<Item> result : results) {
    Item item = result.get();

    if (!item.isDir()) {
     files.add(new MinioFileInfo(
       item.objectName(),
       item.size(),
       item.lastModified(),
       item.etag()));
    }
   }

   logger.debug("Found {} files under path: {}", files.size(), path);

  } catch (Exception e) {
   logger.error("Error scanning MinIO path: {}", path, e);
   throw new RuntimeException("Failed to scan MinIO path: " + path, e);
  }

  return files;
 }

 public InputStream getFileContent(String bucketName, String objectName) {
  try {
   return minioClient.getObject(
     io.minio.GetObjectArgs.builder()
       .bucket(bucketName)
       .object(objectName)
       .build());
  } catch (Exception e) {
   logger.error("Error retrieving file content: {}", objectName, e);
   throw new RuntimeException("Failed to retrieve file: " + objectName, e);
  }
 }

 public static class MinioFileInfo {
  private final String objectName;
  private final long size;
  private final java.time.ZonedDateTime lastModified;
  private final String etag;

  public MinioFileInfo(String objectName, long size, java.time.ZonedDateTime lastModified, String etag) {
   this.objectName = objectName;
   this.size = size;
   this.lastModified = lastModified;
   this.etag = etag;
  }

  public String getObjectName() {
   return objectName;
  }

  public long getSize() {
   return size;
  }

  public java.time.ZonedDateTime getLastModified() {
   return lastModified;
  }

  public String getEtag() {
   return etag;
  }
 }
}