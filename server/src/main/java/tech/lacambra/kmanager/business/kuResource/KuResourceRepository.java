package tech.lacambra.kmanager.business.kuResource;

import static tech.lacambra.kmanager.generated.jooq.tables.KnowledgeUnitResource.KNOWLEDGE_UNIT_RESOURCE;
import static tech.lacambra.kmanager.generated.jooq.tables.ResourceContainedDocumentGroup.RESOURCE_CONTAINED_DOCUMENT_GROUP;
import java.util.List;
import java.util.UUID;
import org.jooq.DSLContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class KuResourceRepository {

 private DSLContext dslContext;

 @Inject
 public KuResourceRepository(DSLContext dslContext) {
  this.dslContext = dslContext;
 }

 public UUID createKuResource(KuResourceInput input) {
  UUID resourceId = dslContext.insertInto(KNOWLEDGE_UNIT_RESOURCE)
    .set(KNOWLEDGE_UNIT_RESOURCE.NAME, input.name())
    .set(KNOWLEDGE_UNIT_RESOURCE.DESCRIPTION, input.description())
    .returningResult(KNOWLEDGE_UNIT_RESOURCE.ID)
    .fetchOne()
    .value1();

  return resourceId;
 }

 public void updateKuResource(UUID kuResourceId, KuResourceInput input) {
  dslContext.update(KNOWLEDGE_UNIT_RESOURCE)
    .set(KNOWLEDGE_UNIT_RESOURCE.NAME, input.name())
    .set(KNOWLEDGE_UNIT_RESOURCE.DESCRIPTION, input.description())
    .where(KNOWLEDGE_UNIT_RESOURCE.ID.eq(kuResourceId))
    .execute();
 }

 public void addDocumentGroupToKuResource(UUID kuResourceId, UUID documentGroupId) {
  dslContext.insertInto(RESOURCE_CONTAINED_DOCUMENT_GROUP)
    .set(RESOURCE_CONTAINED_DOCUMENT_GROUP.KNOWLEDGE_UNIT_RESOURCE_ID, kuResourceId)
    .set(RESOURCE_CONTAINED_DOCUMENT_GROUP.DOCUMENT_GROUP_ID, documentGroupId)
    .set(RESOURCE_CONTAINED_DOCUMENT_GROUP.IS_DEFAULT, false)
    .execute();
 }

 public void addDocumentGroupsToKuResource(UUID kuResourceId, List<UUID> documentGroupIds) {
  if (documentGroupIds == null || documentGroupIds.isEmpty()) {
   return;
  }

  var query = dslContext.insertInto(RESOURCE_CONTAINED_DOCUMENT_GROUP)
    .columns(RESOURCE_CONTAINED_DOCUMENT_GROUP.KNOWLEDGE_UNIT_RESOURCE_ID,
             RESOURCE_CONTAINED_DOCUMENT_GROUP.DOCUMENT_GROUP_ID,
             RESOURCE_CONTAINED_DOCUMENT_GROUP.IS_DEFAULT);

  for (UUID documentGroupId : documentGroupIds) {
   query = query.values(kuResourceId, documentGroupId, false);
  }

  query.execute();
 }

 public void removeDocumentGroupFromKuResource(UUID kuResourceId, UUID documentGroupId) {
  dslContext.deleteFrom(RESOURCE_CONTAINED_DOCUMENT_GROUP)
    .where(RESOURCE_CONTAINED_DOCUMENT_GROUP.KNOWLEDGE_UNIT_RESOURCE_ID.eq(kuResourceId))
    .and(RESOURCE_CONTAINED_DOCUMENT_GROUP.DOCUMENT_GROUP_ID.eq(documentGroupId))
    .execute();
 }

}
