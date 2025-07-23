package tech.lacambra.kmanager.business.knowledge_unit;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import tech.lacambra.kmanager.business.transformer.PdfMergerTransformer;
import tech.lacambra.kmanager.generated.jooq.tables.pojos.KnowledgeUnit;
import tech.lacambra.kmanager.resource.knowlege_manager.KnowledgeUnitWithDocumentGroupUrisResponse;
import tech.lacambra.kmanager.services.file_storage.MinioScanService;
import tech.lacambra.kmanager.util.ContentCleanupUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class MinioKnowledgeUnitContentProcessor {

 private DocumentGroupUriResolver uriResolver;
 private ContentCleanupUtil cleanupUtil;
 private PdfMergerTransformer pdfMergerTransformer;

 public MinioKnowledgeUnitContentProcessor(DocumentGroupUriResolver uriResolver, ContentCleanupUtil cleanupUtil,
   PdfMergerTransformer pdfMergerTransformer) {
  this.uriResolver = uriResolver;
  this.cleanupUtil = cleanupUtil;
  this.pdfMergerTransformer = pdfMergerTransformer;
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

 public byte[] processKnowledgeUnitToPdf(KnowledgeUnitWithDocumentGroupUrisResponse data) {
  if (data == null || data.knowledgeUnit() == null) {
   throw new ContentProcessingException("Knowledge unit data cannot be null");
  }

  try {
   List<InputStream> pdfStreams = new ArrayList<>();

   if (data.documentGroupUris() != null && !data.documentGroupUris().isEmpty()) {
    for (String uri : data.documentGroupUris()) {
     collectPdfStreamsFromUri(uri, pdfStreams);
    }
   }

   if (pdfStreams.isEmpty()) {
    throw new ContentProcessingException("No PDF files found to process");
   }

   return pdfMergerTransformer.transform(pdfStreams);
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
    content.append(processDocumentGroupUriForText(uri));
   }
  }

  content.append("[CONTENT_END]");

  return content.toString();
 }

 private String processDocumentGroupUriForText(String uri) {
  StringBuilder uriContent = new StringBuilder();

  try {
   List<MinioScanService.MinioFileInfo> files = uriResolver.scanMinioPath(uri);

   uriContent.append("\n[DOCUMENT_GROUP: ").append(cleanupUtil.cleanMetadataValue(uri)).append("]\n");

   for (MinioScanService.MinioFileInfo fileInfo : files) {
    if (isTextSupportedFileType(fileInfo.getObjectName())) {
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

 private void collectPdfStreamsFromUri(String uri, List<InputStream> pdfStreams) {
  try {
   List<MinioScanService.MinioFileInfo> files = uriResolver.scanMinioPath(uri);

   for (MinioScanService.MinioFileInfo fileInfo : files) {
    if (isPdfSupportedFileType(fileInfo.getObjectName())) {
     InputStream metadataPage = createMetadataPage(uri, fileInfo);
     pdfStreams.add(metadataPage);
     
     InputStream pdfStream = uriResolver.getFileInputStream(uri, fileInfo.getObjectName());
     pdfStreams.add(pdfStream);
    }
   }

  } catch (Exception e) {
   throw new ContentProcessingException("Failed to collect PDF streams from URI: " + uri, e);
  }
 }

 private InputStream createMetadataPage(String uri, MinioScanService.MinioFileInfo fileInfo) {
  try (PDDocument document = new PDDocument();
     ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

   PDPage page = new PDPage();
   document.addPage(page);

   try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
    contentStream.beginText();
    contentStream.newLineAtOffset(50, 750);
    contentStream.showText("FILE METADATA");
    contentStream.endText();

    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
    contentStream.beginText();
    contentStream.newLineAtOffset(50, 700);
    contentStream.showText("Document Group: " + cleanupUtil.cleanMetadataValue(uri));
    contentStream.newLineAtOffset(0, -20);
    contentStream.showText("File Name: " + cleanupUtil.cleanMetadataValue(fileInfo.getObjectName()));
    contentStream.newLineAtOffset(0, -20);
    contentStream.showText("File Size: " + fileInfo.getSize() + " bytes");
    contentStream.newLineAtOffset(0, -20);
    if (fileInfo.getLastModified() != null) {
     contentStream.showText("Last Modified: " + fileInfo.getLastModified().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
    contentStream.newLineAtOffset(0, -20);
    contentStream.showText("ETag: " + fileInfo.getEtag());
    contentStream.endText();
   }

   document.save(outputStream);
   return new ByteArrayInputStream(outputStream.toByteArray());

  } catch (IOException e) {
   throw new ContentProcessingException("Failed to create metadata page for file: " + fileInfo.getObjectName(), e);
  }
 }

 private boolean isTextSupportedFileType(String fileName) {
  String extension = getFileExtension(fileName);

  return switch (extension.toLowerCase()) {
   case "txt", "md", "java", "js", "ts", "json", "xml", "yaml", "yml", "properties" -> true;
   default -> false;
  };
 }

 private boolean isPdfSupportedFileType(String fileName) {
  String extension = getFileExtension(fileName);

  return switch (extension.toLowerCase()) {
   case "pdf" -> true;
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