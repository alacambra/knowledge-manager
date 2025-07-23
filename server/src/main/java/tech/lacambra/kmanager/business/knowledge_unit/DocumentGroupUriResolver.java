package tech.lacambra.kmanager.business.knowledge_unit;

import jakarta.enterprise.context.ApplicationScoped;
import tech.lacambra.kmanager.services.file_storage.MinioScanService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class DocumentGroupUriResolver {

 private MinioScanService minioScanService;

 public DocumentGroupUriResolver(MinioScanService minioScanService) {
  this.minioScanService = minioScanService;
 }

 public List<MinioScanService.MinioFileInfo> scanMinioPath(String uri) {
  String[] parts = parseUri(uri);
  String bucketName = parts[0];
  String path = parts[1];

  return minioScanService.scanPath(bucketName, path);
 }

 public String getFileContent(String uri, String objectName) {
  String[] parts = parseUri(uri);
  String bucketName = parts[0];

  try (InputStream inputStream = minioScanService.getFileContent(bucketName, objectName);
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

   return reader.lines().collect(Collectors.joining("\n"));

  } catch (IOException e) {
   throw new RuntimeException("Failed to read file content: " + objectName, e);
  }
 }

 private String[] parseUri(String uri) {
  if (uri == null || uri.isEmpty()) {
   throw new IllegalArgumentException("URI cannot be null or empty");
  }

  String[] parts = uri.split("/", 2);
  if (parts.length < 1) {
   throw new IllegalArgumentException("Invalid URI format. Expected: bucket/path");
  }

  String bucketName = parts[0];
  String path = parts.length > 1 ? parts[1] : "";

  return new String[] { bucketName, path };
 }
}