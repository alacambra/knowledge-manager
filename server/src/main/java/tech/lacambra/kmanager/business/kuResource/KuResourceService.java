package tech.lacambra.kmanager.business.kuResource;

import static tech.lacambra.kmanager.generated.jooq.tables.DocumentGroupDocument.DOCUMENT_GROUP_DOCUMENT;
import static tech.lacambra.kmanager.generated.jooq.tables.KnowledgeUnitResource.KNOWLEDGE_UNIT_RESOURCE;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.jooq.DSLContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class KuResourceService {

 private DSLContext dslContext;
 private KuResourceRepository kuResourceRepository;

 @Inject
 public KuResourceService(DSLContext dslContext, KuResourceRepository kuResourceRepository) {
  this.dslContext = dslContext;
  this.kuResourceRepository = kuResourceRepository;
 }

 public void updateKuResource(UUID kuResourceId, KuResourceInput input) {
  kuResourceRepository.updateKuResource(kuResourceId, input);
 }

 public void addDocumentGroupToKuResource(UUID kurId, UUID dgId) {
  kuResourceRepository.addDocumentGroupToKuResource(kurId, dgId);
 }

 public void addDocumentGroupsToKuResource(UUID kurId, List<UUID> dgIds) {
  kuResourceRepository.addDocumentGroupsToKuResource(kurId, dgIds);
 }
}
