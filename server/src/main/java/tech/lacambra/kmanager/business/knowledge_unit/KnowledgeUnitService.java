package tech.lacambra.kmanager.business.knowledge_unit;

import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.StreamingOutput;
import tech.lacambra.kmanager.business.documents.DocumentRepository;
import tech.lacambra.kmanager.resource.knowlege_manager.KnowledgeUnitRequest;
import tech.lacambra.kmanager.resource.knowlege_manager.KnowledgeUnitWithDocumentsResponse;
import tech.lacambra.kmanager.resource.knowlege_manager.KnowledgeUnitWithDocumentGroupUrisResponse;

@ApplicationScoped
public class KnowledgeUnitService {

 private final KnowledgeUnitRepository knowledgeUnitRepository;
 private final DocumentRepository documentRepository;
 private final MinioKnowledgeUnitContentProcessor minioContentProcessor;
 private final FileExportHelper fileExportHelper;

 @Inject
 public KnowledgeUnitService(KnowledgeUnitRepository knowledgeUnitRepository, 
                           DocumentRepository documentRepository,
                           MinioKnowledgeUnitContentProcessor minioContentProcessor,
                           FileExportHelper fileExportHelper) {
  this.knowledgeUnitRepository = knowledgeUnitRepository;
  this.documentRepository = documentRepository;
  this.minioContentProcessor = minioContentProcessor;
  this.fileExportHelper = fileExportHelper;
 }

 public UUID createKnowledgeUnit(KnowledgeUnitRequest knowledgeUnitRequest) {
  UUID kuId = knowledgeUnitRepository
    .create(new KnowledgeUnitInput(knowledgeUnitRequest.name(), knowledgeUnitRequest.description()));

  handleNewDocuments(kuId, knowledgeUnitRequest);

  handleAddingExistingDocuments(kuId, knowledgeUnitRequest);

  return kuId;
 }

 public void updateKnowledgeUnit(UUID kuId, KnowledgeUnitRequest knowledgeUnitRequest) {
  updateKnowledgeUnitBasicInfo(kuId, knowledgeUnitRequest);

  handleNewDocuments(kuId, knowledgeUnitRequest);

  handleAddingExistingDocuments(kuId, knowledgeUnitRequest);

  handleRemovingDocuments(kuId, knowledgeUnitRequest);
 }

 public String generateConcatenatedText(UUID knowledgeUnitId) {
  KnowledgeUnitWithDocumentGroupUrisResponse data = knowledgeUnitRepository
    .findByIdWithDocumentGroupUris(knowledgeUnitId)
    .orElseThrow(() -> new KnowledgeUnitNotFoundException(knowledgeUnitId));
  
  return minioContentProcessor.processKnowledgeUnitToText(data);
 }

 public Path exportToFile(UUID knowledgeUnitId, String filename) {
  String content = generateConcatenatedText(knowledgeUnitId);
  
  String finalFilename = determineFilename(knowledgeUnitId, filename);
  
  return fileExportHelper.writeToFile(content, finalFilename);
 }

 public StreamingOutput generateDownloadStream(UUID knowledgeUnitId) {
  String content = generateConcatenatedText(knowledgeUnitId);
  
  return output -> {
   try (OutputStreamWriter writer = new OutputStreamWriter(output, StandardCharsets.UTF_8)) {
    writer.write(content);
   }
  };
 }

 public String generateDownloadFilename(UUID knowledgeUnitId) {
  KnowledgeUnitWithDocumentGroupUrisResponse data = knowledgeUnitRepository
    .findByIdWithDocumentGroupUris(knowledgeUnitId)
    .orElseThrow(() -> new KnowledgeUnitNotFoundException(knowledgeUnitId));
  return sanitizeFilename(data.knowledgeUnit().getName()) + ".txt";
 }

 private String sanitizeFilename(String filename) {
  return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
 }

 private void handleNewDocuments(UUID kuId, KnowledgeUnitRequest request) {
  if (request.newDocuments() != null && !request.newDocuments().isEmpty()) {
   List<UUID> newDocIds = documentRepository.createDocuments(request.newDocuments());
   knowledgeUnitRepository.addDocumentsToDefaultDocumentGroup(kuId, newDocIds);
  }
 }

 private void handleAddingExistingDocuments(UUID kuId, KnowledgeUnitRequest request) {
  if (request.addedDocumentsIds() != null && !request.addedDocumentsIds().isEmpty()) {
   knowledgeUnitRepository.addDocumentsToDefaultDocumentGroup(kuId, request.addedDocumentsIds());
  }
 }

 private void handleRemovingDocuments(UUID kuId, KnowledgeUnitRequest request) {
  if (request.removedDocumentsIds() != null && !request.removedDocumentsIds().isEmpty()) {
   knowledgeUnitRepository.removeDocumentsFromDefaultDocumentGroup(kuId, request.removedDocumentsIds());
  }
 }

 private void updateKnowledgeUnitBasicInfo(UUID kuId, KnowledgeUnitRequest request) {
  if (request.name() != null || request.description() != null) {
   knowledgeUnitRepository.updateKnowledgeUnit(kuId, request.name(), request.description());
  }
 }

 private String determineFilename(UUID knowledgeUnitId, String filename) {
  if (filename != null && !filename.trim().isEmpty()) {
   return filename;
  }
  
  KnowledgeUnitWithDocumentsResponse data = knowledgeUnitRepository
    .findByIdWithDocumentsOrdered(knowledgeUnitId)
    .orElseThrow(() -> new KnowledgeUnitNotFoundException(knowledgeUnitId));
  return fileExportHelper.generateDefaultFilename(data.knowledgeUnit().getName());
 }
}
