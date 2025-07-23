package tech.lacambra.kmanager.business.knowledge_unit;

import jakarta.enterprise.context.ApplicationScoped;
import tech.lacambra.kmanager.generated.jooq.tables.pojos.KnowledgeUnit;
import tech.lacambra.kmanager.resource.knowlege_manager.KnowledgeUnitWithDocumentGroupUrisResponse;
import tech.lacambra.kmanager.services.file_storage.MinioScanService;
import tech.lacambra.kmanager.util.ContentCleanupUtil;

import java.time.format.DateTimeFormatter;
import java.util.List;

@ApplicationScoped
public class MinioKnowledgeUnitContentProcessor {

 private DocumentGroupUriResolver uriResolver;
 private ContentCleanupUtil cleanupUtil;

 public MinioKnowledgeUnitContentProcessor(DocumentGroupUriResolver uriResolver, ContentCleanupUtil cleanupUtil) {
  this.uriResolver = uriResolver;
  this.cleanupUtil = cleanupUtil;
 }

 public String processKnowledgeUnitToText(KnowledgeUnitWithDocumentGroupUrisResponse data) {
  if (data == null || data.knowledgeUnit() == null) {
   throw new ContentProcessingException("Knowledge unit data cannot be null");
  }

  try {
   StringBuilder result = new StringBuilder();

   result.append(buildMetadataSection(data.knowledgeUnit(), data.documentGroupUris()));
   result.append("\n");
   result.append(buildContentSection(data.documentGroupUris()));

   return result.toString();
  } catch (Exception e) {
   throw new ContentProcessingException("Failed to process knowledge unit content", e);
  }
 }

 private String buildMetadataSection(KnowledgeUnit ku, List<String> documentGroupUris) {
  StringBuilder metadata = new StringBuilder();

  metadata.append("[KNOWLEDGE_UNIT_METADATA_START]\n");
  metadata.append("NAME: ").append(cleanupUtil.cleanMetadataValue(ku.getName())).append("\n");
  metadata.append("DESCRIPTION: ").append(cleanupUtil.cleanMetadataValue(ku.getDescription())).append("\n");
  metadata.append("DOCUMENT_GROUP_COUNT: ").append(documentGroupUris != null ? documentGroupUris.size() : 0)
    .append("\n");

  if (ku.getCreatedAt() != null) {
   metadata.append("CREATED_AT: ").append(ku.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\n");
  }

  if (ku.getUpdatedAt() != null) {
   metadata.append("UPDATED_AT: ").append(ku.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\n");
  }

  metadata.append("\n[KNOWLEDGE_UNIT_METADATA_END]");

  return metadata.toString();
 }

 private String buildContentSection(List<String> documentGroupUris) {
  StringBuilder content = new StringBuilder();

  content.append("[CONTENT_START]\n");

  if (documentGroupUris != null && !documentGroupUris.isEmpty()) {
   for (String uri : documentGroupUris) {
    content.append(processDocumentGroupUri(uri));
   }
  }

  content.append("[CONTENT_END]");

  return content.toString();
 }

 private String processDocumentGroupUri(String uri) {
  StringBuilder uriContent = new StringBuilder();

  try {
   List<MinioScanService.MinioFileInfo> files = uriResolver.scanMinioPath(uri);

   uriContent.append("\n[DOCUMENT_GROUP: ").append(cleanupUtil.cleanMetadataValue(uri)).append("]\n");

   for (MinioScanService.MinioFileInfo fileInfo : files) {
    if (isSupportedFileType(fileInfo.getObjectName())) {
     String fileContent = uriResolver.getFileContent(uri, fileInfo.getObjectName());

     uriContent.append("\n[FILE: ").append(cleanupUtil.cleanMetadataValue(fileInfo.getObjectName())).append("]\n");
     uriContent.append(cleanupUtil.cleanDocumentContent(fileContent));
     uriContent.append("\n");
    }
   }

  } catch (Exception e) {
   uriContent.append("\n[ERROR processing URI: ").append(uri).append(" - ").append(e.getMessage()).append("]\n");
  }

  return uriContent.toString();
 }

 private boolean isSupportedFileType(String fileName) {
  String extension = getFileExtension(fileName);

  return switch (extension.toLowerCase()) {
   case "txt", "md", "java", "js", "ts", "json", "xml", "yaml", "yml", "properties" -> true;
   default -> false;
  };
 }

 private String getFileExtension(String fileName) {
  if (fileName == null || !fileName.contains(".")) {
   return "";
  }
  return fileName.substring(fileName.lastIndexOf(".") + 1);
 }
}