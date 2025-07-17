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

  List<UUID> docIds = documentRepository.createDocuments(knowledgeUnitRequest.documents());
  knowledgeUnitRepository.addDocumentsToKU(kuId, docIds);

  return kuId;
 }
}
