package tech.lacambra.kmanager.business.knowledge_unit;

import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.lacambra.kmanager.business.documents.DocumentRepository;
import tech.lacambra.kmanager.resource.knowlege_manager.KnowledgeUnitRequest;

@ApplicationScoped
public class KnowledgeUnitService {

 private KnowledgeUnitRepository knowledgeUnitRepository;
 private DocumentRepository documentRepository;

 @Inject
 public KnowledgeUnitService(KnowledgeUnitRepository knowledgeUnitRepository, DocumentRepository documentRepository) {
  this.knowledgeUnitRepository = knowledgeUnitRepository;
  this.documentRepository = documentRepository;
 }

 public UUID createKnowledgeUnit(KnowledgeUnitRequest knowledgeUnitRequest) {
  UUID kuId = knowledgeUnitRepository
    .create(new KnowledgeUnitInput(knowledgeUnitRequest.name(), knowledgeUnitRequest.description()));

  // Handle new documents
  if (knowledgeUnitRequest.newDocuments() != null && !knowledgeUnitRequest.newDocuments().isEmpty()) {
   List<UUID> newDocIds = documentRepository.createDocuments(knowledgeUnitRequest.newDocuments());
   knowledgeUnitRepository.addDocumentsToKU(kuId, newDocIds);
  }

  // Handle adding existing documents
  if (knowledgeUnitRequest.addedDocumentsIds() != null && !knowledgeUnitRequest.addedDocumentsIds().isEmpty()) {
   knowledgeUnitRepository.addDocumentsToKU(kuId, knowledgeUnitRequest.addedDocumentsIds());
  }

  return kuId;
 }

 public void updateKnowledgeUnit(UUID kuId, KnowledgeUnitRequest knowledgeUnitRequest) {
  // Update KU name and description if provided
  if (knowledgeUnitRequest.name() != null || knowledgeUnitRequest.description() != null) {
   knowledgeUnitRepository.updateKnowledgeUnit(kuId, knowledgeUnitRequest.name(), knowledgeUnitRequest.description());
  }

  // Handle new documents
  if (knowledgeUnitRequest.newDocuments() != null && !knowledgeUnitRequest.newDocuments().isEmpty()) {
   List<UUID> newDocIds = documentRepository.createDocuments(knowledgeUnitRequest.newDocuments());
   knowledgeUnitRepository.addDocumentsToKU(kuId, newDocIds);
  }

  // Handle adding existing documents
  if (knowledgeUnitRequest.addedDocumentsIds() != null && !knowledgeUnitRequest.addedDocumentsIds().isEmpty()) {
   knowledgeUnitRepository.addDocumentsToKU(kuId, knowledgeUnitRequest.addedDocumentsIds());
  }

  // Handle removing documents
  if (knowledgeUnitRequest.removedDocumentsIds() != null && !knowledgeUnitRequest.removedDocumentsIds().isEmpty()) {
   knowledgeUnitRepository.removeDocumentsFromKU(kuId, knowledgeUnitRequest.removedDocumentsIds());
  }
 }
}
