package tech.lacambra.kmanager.business.knowledge_unit;

import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.StreamingOutput;
import tech.lacambra.kmanager.business.documents.DocumentGroupInput;
import tech.lacambra.kmanager.business.documents.DocumentRepositoryOld;
import tech.lacambra.kmanager.business.kuResource.KuResourceInput;
import tech.lacambra.kmanager.business.kuResource.KuResourceRepository;
import tech.lacambra.kmanager.resource.knowlege_manager.KuResourceRequest;
import tech.lacambra.kmanager.resource.knowlege_manager.KnowledgeUnitRequest;
import tech.lacambra.kmanager.resource.knowlege_manager.KnowledgeUnitWithDocumentsResponse;
import tech.lacambra.kmanager.resource.knowlege_manager.KnowledgeUnitWithDocumentGroupUrisResponse;
import tech.lacambra.kmanager.resource.knowlege_manager.KnowledgeUnitWithResourcesResponse;
import tech.lacambra.kmanager.resource.knowlege_manager.KnowledgeUnitResourceWithDocumentGroupsResponse;
import tech.lacambra.kmanager.services.file_storage.MinioScanService;

import java.util.List;
import java.util.ArrayList;

@ApplicationScoped
public class KnowledgeUnitService {

 private final KnowledgeUnitRepository knowledgeUnitRepository;
 private final DocumentRepositoryOld documentRepository;
 private final MinioKnowledgeUnitContentProcessor minioContentProcessor;
 private final FileExportHelper fileExportHelper;
 private final KuResourceRepository kuResourceRepository;
 private final DocumentGroupUriResolver documentGroupUriResolver;

 @Inject
 public KnowledgeUnitService(
   KnowledgeUnitRepository knowledgeUnitRepository,
   KuResourceRepository kuResourceRepository,
   DocumentRepositoryOld documentRepository,
   MinioKnowledgeUnitContentProcessor minioContentProcessor,
   FileExportHelper fileExportHelper,
   DocumentGroupUriResolver documentGroupUriResolver) {
  this.knowledgeUnitRepository = knowledgeUnitRepository;
  this.documentRepository = documentRepository;
  this.minioContentProcessor = minioContentProcessor;
  this.fileExportHelper = fileExportHelper;
  this.kuResourceRepository = kuResourceRepository;
  this.documentGroupUriResolver = documentGroupUriResolver;
 }

 public UUID createKnowledgeUnit(KnowledgeUnitRequest knowledgeUnitRequest) {

  UUID kuId = knowledgeUnitRepository
    .create(new KnowledgeUnitInput(knowledgeUnitRequest.name(), knowledgeUnitRequest.description()));

  handleKuResources(kuId, knowledgeUnitRequest);

  return kuId;
 }

 public void updateKnowledgeUnit(UUID kuId, KnowledgeUnitRequest knowledgeUnitRequest) {
  updateKnowledgeUnitBasicInfo(kuId, knowledgeUnitRequest);

  handleKuResources(kuId, knowledgeUnitRequest);
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

 public List<String> getDocumentUrlsForKnowledgeUnit(UUID knowledgeUnitId) {
  KnowledgeUnitWithResourcesResponse kuWithResources = knowledgeUnitRepository
    .getKnowledgeUnitWithResources(knowledgeUnitId)
    .orElseThrow(() -> new KnowledgeUnitNotFoundException(knowledgeUnitId));

  List<String> allDocumentUrls = new ArrayList<>();

  for (KnowledgeUnitResourceWithDocumentGroupsResponse resource : kuWithResources.resources()) {
   resource.documentGroups().forEach(documentGroup -> {
    try {
     List<MinioScanService.MinioFileInfo> files = documentGroupUriResolver.scanMinioPath(documentGroup.getUri());
     files.forEach(file -> allDocumentUrls.add(file.getObjectName()));
    } catch (Exception e) {
     throw new RuntimeException("Failed to scan MinIO path for DocumentGroup: " + documentGroup.getUri(), e);
    }
   });
  }

  return allDocumentUrls;
 }

 private String sanitizeFilename(String filename) {
  return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
 }

 private void handleKuResources(UUID kuId, KnowledgeUnitRequest request) {
  if (request.kuResourceRequests() != null && !request.kuResourceRequests().isEmpty()) {
   for (KuResourceRequest kuResourceRequest : request.kuResourceRequests()) {
    UUID kurId = kuResourceRepository.createKuResource(
      new KuResourceInput(kuResourceRequest.name(), kuResourceRequest.description()));
    
    knowledgeUnitRepository.addKuResourceToKnowledgeUnit(kuId, kurId);
    
    handleDocumentGroupsForKuResource(kurId, kuResourceRequest);
   }
  } else {
   UUID kurId = kuResourceRepository.createKuResource(
     new KuResourceInput("Default Resource", "Default resource for knowledge unit"));
   knowledgeUnitRepository.addKuResourceToKnowledgeUnit(kuId, kurId);
  }
 }

 private void handleDocumentGroupsForKuResource(UUID kurId, KuResourceRequest kuResourceRequest) {
  if (kuResourceRequest.newDocumentGroups() != null && !kuResourceRequest.newDocumentGroups().isEmpty()) {
   kuResourceRequest.newDocumentGroups().forEach(dgRequest -> {
    UUID dgId = documentRepository.createDocumentGroup(new DocumentGroupInput(dgRequest.uri()));
    kuResourceRepository.addDocumentGroupToKuResource(kurId, dgId);
   });
  }
  
  if (kuResourceRequest.addedDocumentsGroupsIds() != null && !kuResourceRequest.addedDocumentsGroupsIds().isEmpty()) {
   kuResourceRequest.addedDocumentsGroupsIds().forEach(dgId -> {
    kuResourceRepository.addDocumentGroupToKuResource(kurId, dgId);
   });
  }
  
  if (kuResourceRequest.removedDocumentsGroupsIds() != null && !kuResourceRequest.removedDocumentsGroupsIds().isEmpty()) {
   kuResourceRequest.removedDocumentsGroupsIds().forEach(dgId -> {
    kuResourceRepository.removeDocumentGroupFromKuResource(kurId, dgId);
   });
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
