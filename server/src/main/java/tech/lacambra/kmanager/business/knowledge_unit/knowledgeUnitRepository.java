package tech.lacambra.kmanager.business.knowledge_unit;

import static tech.lacambra.kmanager.generated.jooq.tables.KnowledgeUnit.*;
import static tech.lacambra.kmanager.generated.jooq.tables.KnowledgeUnitContainedResource.*;
import static tech.lacambra.kmanager.generated.jooq.tables.KnowledgeUnitResource.*;
import static tech.lacambra.kmanager.generated.jooq.tables.DocumentGroup.*;
import static tech.lacambra.kmanager.generated.jooq.tables.ResourceContainedDocumentGroup.*;
import static tech.lacambra.kmanager.generated.jooq.tables.DocumentGroupDocument.*;
import static tech.lacambra.kmanager.generated.jooq.tables.Document.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.groupingBy;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import tech.lacambra.kmanager.generated.jooq.tables.pojos.KnowledgeUnit;
import tech.lacambra.kmanager.generated.jooq.tables.pojos.KnowledgeUnitResource;
import tech.lacambra.kmanager.generated.jooq.tables.pojos.DocumentGroup;
import tech.lacambra.kmanager.generated.jooq.tables.pojos.Document;
import tech.lacambra.kmanager.resource.knowlege_manager.KnowledgeUnitResourceWithDocumentGroupsResponse;
import tech.lacambra.kmanager.resource.knowlege_manager.KnowledgeUnitWithDocumentsResponse;
import tech.lacambra.kmanager.resource.knowlege_manager.KnowledgeUnitWithResourcesResponse;
import tech.lacambra.kmanager.resource.knowlege_manager.KnowledgeUnitWithDocumentGroupUrisResponse;

import java.util.Optional;

@ApplicationScoped
public class KnowledgeUnitRepository {

 @Inject
 DSLContext dslContext;

 public UUID create(KnowledgeUnitInput input) {
  UUID kuId = dslContext.insertInto(KNOWLEDGE_UNIT)
    .set(KNOWLEDGE_UNIT.NAME, input.name())
    .set(KNOWLEDGE_UNIT.DESCRIPTION, input.description())
    .returningResult(KNOWLEDGE_UNIT.ID)
    .fetchOne()
    .value1();
  
  createDefaultResource(kuId, input.name());
  return kuId;
 }

 public void addResourcesToKU(UUID kuId, List<UUID> resourceIds) {
  dslContext.insertInto(KNOWLEDGE_UNIT_CONTAINED_RESOURCE)
    .columns(KNOWLEDGE_UNIT_CONTAINED_RESOURCE.KNOWLEDGE_UNIT_ID,
      KNOWLEDGE_UNIT_CONTAINED_RESOURCE.KNOWLEDGE_UNIT_RESOURCE_ID)
    .valuesOfRows(resourceIds.stream()
      .map(resourceId -> DSL.row(kuId, resourceId))
      .toList())
    .execute();
 }

 public void removeResourcesFromKU(UUID kuId, List<UUID> resourceIds) {
  dslContext.deleteFrom(KNOWLEDGE_UNIT_CONTAINED_RESOURCE)
    .where(KNOWLEDGE_UNIT_CONTAINED_RESOURCE.KNOWLEDGE_UNIT_ID.eq(kuId))
    .and(KNOWLEDGE_UNIT_CONTAINED_RESOURCE.KNOWLEDGE_UNIT_RESOURCE_ID.in(resourceIds))
    .execute();
 }

 public void updateKnowledgeUnit(UUID kuId, String name, String description) {
  dslContext.update(KNOWLEDGE_UNIT)
    .set(KNOWLEDGE_UNIT.NAME, name)
    .set(KNOWLEDGE_UNIT.DESCRIPTION, description)
    .where(KNOWLEDGE_UNIT.ID.eq(kuId))
    .execute();
 }

 public Optional<KnowledgeUnitWithResourcesResponse> getKnowledgeUnitWithResources(UUID kuId) {
  KnowledgeUnit ku = dslContext.select()
    .from(KNOWLEDGE_UNIT)
    .where(KNOWLEDGE_UNIT.ID.eq(kuId))
    .fetchOneInto(KnowledgeUnit.class);

  if (ku == null) {
   return Optional.empty();
  }

  List<KnowledgeUnitResourceWithDocumentGroupsResponse> resources = dslContext.select(
      KNOWLEDGE_UNIT_RESOURCE.fields())
    .select(DOCUMENT_GROUP.fields())
    .from(KNOWLEDGE_UNIT_RESOURCE)
    .join(KNOWLEDGE_UNIT_CONTAINED_RESOURCE).on(KNOWLEDGE_UNIT_RESOURCE.ID.eq(KNOWLEDGE_UNIT_CONTAINED_RESOURCE.KNOWLEDGE_UNIT_RESOURCE_ID))
    .join(RESOURCE_CONTAINED_DOCUMENT_GROUP).on(KNOWLEDGE_UNIT_RESOURCE.ID.eq(RESOURCE_CONTAINED_DOCUMENT_GROUP.KNOWLEDGE_UNIT_RESOURCE_ID))
    .join(DOCUMENT_GROUP).on(RESOURCE_CONTAINED_DOCUMENT_GROUP.DOCUMENT_GROUP_ID.eq(DOCUMENT_GROUP.ID))
    .where(KNOWLEDGE_UNIT_CONTAINED_RESOURCE.KNOWLEDGE_UNIT_ID.eq(kuId))
    .orderBy(KNOWLEDGE_UNIT_RESOURCE.CREATED_AT.asc(), DOCUMENT_GROUP.CREATED_AT.asc())
    .fetch()
    .stream()
    .collect(groupingBy(
        record -> record.into(KNOWLEDGE_UNIT_RESOURCE).into(KnowledgeUnitResource.class),
        Collectors.mapping(
            record -> record.into(DOCUMENT_GROUP).into(DocumentGroup.class),
            Collectors.toList())))
    .entrySet()
    .stream()
    .map(entry -> new KnowledgeUnitResourceWithDocumentGroupsResponse(entry.getKey(), entry.getValue()))
    .toList();

  return Optional.of(new KnowledgeUnitWithResourcesResponse(ku, resources));
 }

 public Optional<KnowledgeUnitWithDocumentsResponse> findByIdWithDocumentsOrdered(UUID kuId) {
  KnowledgeUnit ku = dslContext.select()
    .from(KNOWLEDGE_UNIT)
    .where(KNOWLEDGE_UNIT.ID.eq(kuId))
    .fetchOneInto(KnowledgeUnit.class);

  if (ku == null) {
   return Optional.empty();
  }

  List<Document> documents = dslContext.select(DOCUMENT.fields())
    .from(DOCUMENT)
    .join(DOCUMENT_GROUP_DOCUMENT).on(DOCUMENT.ID.eq(DOCUMENT_GROUP_DOCUMENT.DOCUMENT_ID))
    .join(DOCUMENT_GROUP).on(DOCUMENT_GROUP_DOCUMENT.DOCUMENT_GROUP_ID.eq(DOCUMENT_GROUP.ID))
    .join(RESOURCE_CONTAINED_DOCUMENT_GROUP).on(DOCUMENT_GROUP.ID.eq(RESOURCE_CONTAINED_DOCUMENT_GROUP.DOCUMENT_GROUP_ID))
    .join(KNOWLEDGE_UNIT_RESOURCE).on(RESOURCE_CONTAINED_DOCUMENT_GROUP.KNOWLEDGE_UNIT_RESOURCE_ID.eq(KNOWLEDGE_UNIT_RESOURCE.ID))
    .join(KNOWLEDGE_UNIT_CONTAINED_RESOURCE).on(KNOWLEDGE_UNIT_RESOURCE.ID.eq(KNOWLEDGE_UNIT_CONTAINED_RESOURCE.KNOWLEDGE_UNIT_RESOURCE_ID))
    .where(KNOWLEDGE_UNIT_CONTAINED_RESOURCE.KNOWLEDGE_UNIT_ID.eq(kuId))
    .orderBy(KNOWLEDGE_UNIT_RESOURCE.CREATED_AT.asc(), DOCUMENT_GROUP.CREATED_AT.asc(), DOCUMENT.CREATED_AT.asc())
    .fetchInto(Document.class);

  return Optional.of(new KnowledgeUnitWithDocumentsResponse(ku, documents));
 }

 public Optional<KnowledgeUnitWithDocumentGroupUrisResponse> findByIdWithDocumentGroupUris(UUID kuId) {
  KnowledgeUnit ku = dslContext.select()
    .from(KNOWLEDGE_UNIT)
    .where(KNOWLEDGE_UNIT.ID.eq(kuId))
    .fetchOneInto(KnowledgeUnit.class);

  if (ku == null) {
   return Optional.empty();
  }

  List<String> documentGroupUris = dslContext.select(DOCUMENT_GROUP.URI)
    .from(DOCUMENT_GROUP)
    .join(RESOURCE_CONTAINED_DOCUMENT_GROUP).on(DOCUMENT_GROUP.ID.eq(RESOURCE_CONTAINED_DOCUMENT_GROUP.DOCUMENT_GROUP_ID))
    .join(KNOWLEDGE_UNIT_RESOURCE).on(RESOURCE_CONTAINED_DOCUMENT_GROUP.KNOWLEDGE_UNIT_RESOURCE_ID.eq(KNOWLEDGE_UNIT_RESOURCE.ID))
    .join(KNOWLEDGE_UNIT_CONTAINED_RESOURCE).on(KNOWLEDGE_UNIT_RESOURCE.ID.eq(KNOWLEDGE_UNIT_CONTAINED_RESOURCE.KNOWLEDGE_UNIT_RESOURCE_ID))
    .where(KNOWLEDGE_UNIT_CONTAINED_RESOURCE.KNOWLEDGE_UNIT_ID.eq(kuId))
    .orderBy(KNOWLEDGE_UNIT_RESOURCE.CREATED_AT.asc(), DOCUMENT_GROUP.CREATED_AT.asc())
    .fetchInto(String.class);

  return Optional.of(new KnowledgeUnitWithDocumentGroupUrisResponse(ku, documentGroupUris));
 }

 public List<KnowledgeUnit> findAll() {
  return dslContext.select()
    .from(KNOWLEDGE_UNIT)
    .fetchInto(KnowledgeUnit.class);
 }

 public void deleteKnowledgeUnit(UUID kuId) {
  dslContext.deleteFrom(KNOWLEDGE_UNIT_CONTAINED_RESOURCE)
    .where(KNOWLEDGE_UNIT_CONTAINED_RESOURCE.KNOWLEDGE_UNIT_ID.eq(kuId))
    .execute();

  dslContext.deleteFrom(KNOWLEDGE_UNIT)
    .where(KNOWLEDGE_UNIT.ID.eq(kuId))
    .execute();
 }

 private void createDefaultResource(UUID kuId, String kuName) {
  UUID resourceId = dslContext.insertInto(KNOWLEDGE_UNIT_RESOURCE)
    .set(KNOWLEDGE_UNIT_RESOURCE.NAME, "Default Resource")
    .set(KNOWLEDGE_UNIT_RESOURCE.DESCRIPTION, "Default resource for " + kuName)
    .returningResult(KNOWLEDGE_UNIT_RESOURCE.ID)
    .fetchOne()
    .value1();

  dslContext.insertInto(KNOWLEDGE_UNIT_CONTAINED_RESOURCE)
    .set(KNOWLEDGE_UNIT_CONTAINED_RESOURCE.KNOWLEDGE_UNIT_ID, kuId)
    .set(KNOWLEDGE_UNIT_CONTAINED_RESOURCE.KNOWLEDGE_UNIT_RESOURCE_ID, resourceId)
    .set(KNOWLEDGE_UNIT_CONTAINED_RESOURCE.IS_DEFAULT, true)
    .execute();

  createDefaultDocumentGroup(resourceId);
 }

 private void createDefaultDocumentGroup(UUID resourceId) {
  UUID documentGroupId = dslContext.insertInto(DOCUMENT_GROUP)
    .set(DOCUMENT_GROUP.URI, "default-documents")
    .returningResult(DOCUMENT_GROUP.ID)
    .fetchOne()
    .value1();

  dslContext.insertInto(RESOURCE_CONTAINED_DOCUMENT_GROUP)
    .set(RESOURCE_CONTAINED_DOCUMENT_GROUP.KNOWLEDGE_UNIT_RESOURCE_ID, resourceId)
    .set(RESOURCE_CONTAINED_DOCUMENT_GROUP.DOCUMENT_GROUP_ID, documentGroupId)
    .set(RESOURCE_CONTAINED_DOCUMENT_GROUP.IS_DEFAULT, true)
    .execute();
 }

 public void addDocumentsToDefaultDocumentGroup(UUID kuId, List<UUID> documentIds) {
  UUID documentGroupId = getDefaultDocumentGroupId(kuId);
  
  dslContext.insertInto(DOCUMENT_GROUP_DOCUMENT)
    .columns(DOCUMENT_GROUP_DOCUMENT.DOCUMENT_GROUP_ID, DOCUMENT_GROUP_DOCUMENT.DOCUMENT_ID)
    .valuesOfRows(documentIds.stream()
      .map(docId -> DSL.row(documentGroupId, docId))
      .toList())
    .execute();
 }

 public void removeDocumentsFromDefaultDocumentGroup(UUID kuId, List<UUID> documentIds) {
  UUID documentGroupId = getDefaultDocumentGroupId(kuId);
  
  dslContext.deleteFrom(DOCUMENT_GROUP_DOCUMENT)
    .where(DOCUMENT_GROUP_DOCUMENT.DOCUMENT_GROUP_ID.eq(documentGroupId))
    .and(DOCUMENT_GROUP_DOCUMENT.DOCUMENT_ID.in(documentIds))
    .execute();
 }

 private UUID getDefaultDocumentGroupId(UUID kuId) {
  return dslContext.select(DOCUMENT_GROUP.ID)
    .from(DOCUMENT_GROUP)
    .join(RESOURCE_CONTAINED_DOCUMENT_GROUP).on(DOCUMENT_GROUP.ID.eq(RESOURCE_CONTAINED_DOCUMENT_GROUP.DOCUMENT_GROUP_ID))
    .join(KNOWLEDGE_UNIT_RESOURCE).on(RESOURCE_CONTAINED_DOCUMENT_GROUP.KNOWLEDGE_UNIT_RESOURCE_ID.eq(KNOWLEDGE_UNIT_RESOURCE.ID))
    .join(KNOWLEDGE_UNIT_CONTAINED_RESOURCE).on(KNOWLEDGE_UNIT_RESOURCE.ID.eq(KNOWLEDGE_UNIT_CONTAINED_RESOURCE.KNOWLEDGE_UNIT_RESOURCE_ID))
    .where(KNOWLEDGE_UNIT_CONTAINED_RESOURCE.KNOWLEDGE_UNIT_ID.eq(kuId))
    .and(KNOWLEDGE_UNIT_CONTAINED_RESOURCE.IS_DEFAULT.eq(true))
    .and(RESOURCE_CONTAINED_DOCUMENT_GROUP.IS_DEFAULT.eq(true))
    .fetchOne(DOCUMENT_GROUP.ID);
 }
}