package tech.lacambra.kmanager.business.kuResource;

import static tech.lacambra.kmanager.generated.jooq.tables.DocumentGroupDocument.DOCUMENT_GROUP_DOCUMENT;
import static tech.lacambra.kmanager.generated.jooq.tables.KnowledgeUnitResource.KNOWLEDGE_UNIT_RESOURCE;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.jooq.impl.DSL;
import jakarta.inject.Inject;

public class KuResourceService {

 private DSL dslContext;

 @Inject
 public KuResourceService(DSL dslContext) {
  this.dslContext = dslContext;
 }

 public void addDocumentGroupToKuResource(UUID kurId, UUID dgId) {
  addDocumentGroupsToKuResource(kurId, Collections.singletonList(dgId));
 }

 public void addDocumentGroupsToKuResource(UUID kurId, List<UUID> dgid) {

  var idRecords = dgid.stream()
    .map(id -> org.jooq.impl.DSL.row(kurId, id))
    .toArray(org.jooq.Row2[]::new);

  dslContext.insertInto(KNOWLEDGE_UNIT_RESOURCE)
    .columns(KNOWLEDGE_UNIT_RESOURCE.ID, DOCUMENT_GROUP_DOCUMENT.ID)
    .valuesOfRows(idRecords)
    .execute();
 }
}
