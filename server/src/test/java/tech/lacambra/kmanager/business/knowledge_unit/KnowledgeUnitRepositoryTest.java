package tech.lacambra.kmanager.business.knowledge_unit;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.lacambra.kmanager.PostgresTestResource;
import tech.lacambra.kmanager.business.kuResource.KuResourceRepository;
import tech.lacambra.kmanager.generated.jooq.tables.pojos.KnowledgeUnit;
import tech.lacambra.kmanager.resource.knowlege_manager.KnowledgeUnitWithDocumentsResponse;
import tech.lacambra.kmanager.resource.knowlege_manager.KnowledgeUnitWithResourcesResponse;
import tech.lacambra.kmanager.resource.knowlege_manager.KnowledgeUnitWithDocumentGroupUrisResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static tech.lacambra.kmanager.generated.jooq.tables.Document.DOCUMENT;
import static tech.lacambra.kmanager.generated.jooq.tables.DocumentGroup.DOCUMENT_GROUP;
import static tech.lacambra.kmanager.generated.jooq.tables.DocumentGroupDocument.DOCUMENT_GROUP_DOCUMENT;
import static tech.lacambra.kmanager.generated.jooq.tables.KnowledgeUnit.KNOWLEDGE_UNIT;
import static tech.lacambra.kmanager.generated.jooq.tables.KnowledgeUnitResource.KNOWLEDGE_UNIT_RESOURCE;
import static tech.lacambra.kmanager.generated.jooq.tables.ResourceContainedDocumentGroup.RESOURCE_CONTAINED_DOCUMENT_GROUP;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class)
class KnowledgeUnitRepositoryTest {

 @Inject
 KnowledgeUnitRepository knowledgeUnitRepository;

 @Inject
 KuResourceRepository kuResourceRepository;

 @Inject
 DSLContext dslContext;

 private UUID testKuId;
 private UUID testResourceId;
 private UUID testDocumentGroupId;
 private UUID testDocumentId;

 @BeforeEach
 void setUp() {
  cleanUpData();
  createTestData();
 }

 @Test
 void testCreateKnowledgeUnit() {
  KnowledgeUnitInput input = new KnowledgeUnitInput("Test KU", "Test Description");

  UUID createdId = knowledgeUnitRepository.create(input);

  assertNotNull(createdId);

  KnowledgeUnit ku = dslContext.select()
    .from(KNOWLEDGE_UNIT)
    .where(KNOWLEDGE_UNIT.ID.eq(createdId))
    .fetchOneInto(KnowledgeUnit.class);

  assertNotNull(ku);
  assertEquals("Test KU", ku.getName());
  assertEquals("Test Description", ku.getDescription());
  assertNotNull(ku.getCreatedAt());
 }

 @Test
 void testUpdateKnowledgeUnit() {
  String newName = "Updated Name";
  String newDescription = "Updated Description";

  knowledgeUnitRepository.updateKnowledgeUnit(testKuId, newName, newDescription);

  KnowledgeUnit updatedKu = dslContext.select()
    .from(KNOWLEDGE_UNIT)
    .where(KNOWLEDGE_UNIT.ID.eq(testKuId))
    .fetchOneInto(KnowledgeUnit.class);

  assertNotNull(updatedKu);
  assertEquals(newName, updatedKu.getName());
  assertEquals(newDescription, updatedKu.getDescription());
 }

 @Test
 void testAddResourcesToKU() {
  UUID newResourceId = createTestResource("New Resource");
  List<UUID> resourceIds = List.of(newResourceId);

  knowledgeUnitRepository.addResourcesToKU(testKuId, resourceIds);

  long count = dslContext.selectCount()
    .from(tech.lacambra.kmanager.generated.jooq.tables.KnowledgeUnitContainedResource.KNOWLEDGE_UNIT_CONTAINED_RESOURCE)
    .where(
      tech.lacambra.kmanager.generated.jooq.tables.KnowledgeUnitContainedResource.KNOWLEDGE_UNIT_CONTAINED_RESOURCE.KNOWLEDGE_UNIT_ID
        .eq(testKuId))
    .fetchOne(0, Long.class);

  assertEquals(2, count); // 1 existing + 1 new
 }

 @Test
 void testRemoveResourcesFromKU() {
  List<UUID> resourceIds = List.of(testResourceId);

  knowledgeUnitRepository.removeResourcesFromKU(testKuId, resourceIds);

  long count = dslContext.selectCount()
    .from(tech.lacambra.kmanager.generated.jooq.tables.KnowledgeUnitContainedResource.KNOWLEDGE_UNIT_CONTAINED_RESOURCE)
    .where(
      tech.lacambra.kmanager.generated.jooq.tables.KnowledgeUnitContainedResource.KNOWLEDGE_UNIT_CONTAINED_RESOURCE.KNOWLEDGE_UNIT_ID
        .eq(testKuId))
    .fetchOne(0, Long.class);

  assertEquals(0, count);
 }

 @Test
 void testGetKnowledgeUnitWithResources() {
  Optional<KnowledgeUnitWithResourcesResponse> result = knowledgeUnitRepository.getKnowledgeUnitWithResources(testKuId);

  assertTrue(result.isPresent());

  KnowledgeUnitWithResourcesResponse response = result.get();
  assertNotNull(response.knowledgeUnit());
  assertEquals("Test KU", response.knowledgeUnit().getName());
  assertNotNull(response.resources());
  assertFalse(response.resources().isEmpty());
 }

 @Test
 void testGetKnowledgeUnitWithResourcesNotFound() {
  UUID nonExistentId = UUID.randomUUID();

  Optional<KnowledgeUnitWithResourcesResponse> result = knowledgeUnitRepository
    .getKnowledgeUnitWithResources(nonExistentId);

  assertTrue(result.isEmpty());
 }

 @Test
 void testFindByIdWithDocumentsOrdered() {
  Optional<KnowledgeUnitWithDocumentsResponse> result = knowledgeUnitRepository.findByIdWithDocumentsOrdered(testKuId);

  assertTrue(result.isPresent());

  KnowledgeUnitWithDocumentsResponse response = result.get();
  assertNotNull(response.knowledgeUnit());
  assertEquals("Test KU", response.knowledgeUnit().getName());
  assertNotNull(response.documents());
  assertFalse(response.documents().isEmpty());
 }

 @Test
 void testFindByIdWithDocumentGroupUris() {
  Optional<KnowledgeUnitWithDocumentGroupUrisResponse> result = knowledgeUnitRepository
    .findByIdWithDocumentGroupUris(testKuId);

  assertTrue(result.isPresent());

  KnowledgeUnitWithDocumentGroupUrisResponse response = result.get();
  assertNotNull(response.knowledgeUnit());
  assertEquals("Test KU", response.knowledgeUnit().getName());
  assertNotNull(response.documentGroupUris());
  assertFalse(response.documentGroupUris().isEmpty());
 }

 @Test
 void testFindAll() {
  List<KnowledgeUnit> allKus = knowledgeUnitRepository.findAll();

  assertNotNull(allKus);
  assertFalse(allKus.isEmpty());
  assertTrue(allKus.stream().anyMatch(ku -> ku.getId().equals(testKuId)));
 }

 @Test
 void testDeleteKnowledgeUnit() {
  knowledgeUnitRepository.deleteKnowledgeUnit(testKuId);

  KnowledgeUnit deletedKu = dslContext.select()
    .from(KNOWLEDGE_UNIT)
    .where(KNOWLEDGE_UNIT.ID.eq(testKuId))
    .fetchOneInto(KnowledgeUnit.class);

  assertNull(deletedKu);

  long resourceCount = dslContext.selectCount()
    .from(tech.lacambra.kmanager.generated.jooq.tables.KnowledgeUnitContainedResource.KNOWLEDGE_UNIT_CONTAINED_RESOURCE)
    .where(
      tech.lacambra.kmanager.generated.jooq.tables.KnowledgeUnitContainedResource.KNOWLEDGE_UNIT_CONTAINED_RESOURCE.KNOWLEDGE_UNIT_ID
        .eq(testKuId))
    .fetchOne(0, Long.class);

  assertEquals(0, resourceCount);
 }

 @Test
 void testAddKuResourceToKnowledgeUnit() {
  UUID newKuId = createTestKnowledgeUnit("New KU");
  UUID newResourceId = createTestResource("New Resource");

  knowledgeUnitRepository.addKuResourceToKnowledgeUnit(newKuId, newResourceId);

  long count = dslContext.selectCount()
    .from(tech.lacambra.kmanager.generated.jooq.tables.KnowledgeUnitContainedResource.KNOWLEDGE_UNIT_CONTAINED_RESOURCE)
    .where(
      tech.lacambra.kmanager.generated.jooq.tables.KnowledgeUnitContainedResource.KNOWLEDGE_UNIT_CONTAINED_RESOURCE.KNOWLEDGE_UNIT_ID
        .eq(newKuId))
    .and(
      tech.lacambra.kmanager.generated.jooq.tables.KnowledgeUnitContainedResource.KNOWLEDGE_UNIT_CONTAINED_RESOURCE.KNOWLEDGE_UNIT_RESOURCE_ID
        .eq(newResourceId))
    .fetchOne(0, Long.class);

  assertEquals(1, count);
 }

 @Test
 void testAddDocumentsToDefaultDocumentGroup() {
  UUID newDocumentId = createTestDocument("New Document");
  List<UUID> documentIds = List.of(newDocumentId);

  knowledgeUnitRepository.addDocumentsToDefaultDocumentGroup(testKuId, documentIds);

  long count = dslContext.selectCount()
    .from(DOCUMENT_GROUP_DOCUMENT)
    .where(DOCUMENT_GROUP_DOCUMENT.DOCUMENT_GROUP_ID.eq(testDocumentGroupId))
    .and(DOCUMENT_GROUP_DOCUMENT.DOCUMENT_ID.eq(newDocumentId))
    .fetchOne(0, Long.class);

  assertEquals(1, count);
 }

 @Test
 void testRemoveDocumentsFromDefaultDocumentGroup() {
  List<UUID> documentIds = List.of(testDocumentId);

  knowledgeUnitRepository.removeDocumentsFromDefaultDocumentGroup(testKuId, documentIds);

  long count = dslContext.selectCount()
    .from(DOCUMENT_GROUP_DOCUMENT)
    .where(DOCUMENT_GROUP_DOCUMENT.DOCUMENT_GROUP_ID.eq(testDocumentGroupId))
    .and(DOCUMENT_GROUP_DOCUMENT.DOCUMENT_ID.eq(testDocumentId))
    .fetchOne(0, Long.class);

  assertEquals(0, count);
 }

 private void createTestData() {
  testKuId = createTestKnowledgeUnit("Test KU");
  testResourceId = createTestResource("Test Resource");
  testDocumentGroupId = createTestDocumentGroup("Test Document Group");
  testDocumentId = createTestDocument("Test Document");

  linkKuWithResource();
  linkResourceWithDocumentGroup();
  linkDocumentGroupWithDocument();
 }

 private UUID createTestKnowledgeUnit(String name) {
  return dslContext.insertInto(KNOWLEDGE_UNIT)
    .set(KNOWLEDGE_UNIT.NAME, name)
    .set(KNOWLEDGE_UNIT.DESCRIPTION, "Test Description")
    .set(KNOWLEDGE_UNIT.CREATED_AT, LocalDateTime.now())
    .returningResult(KNOWLEDGE_UNIT.ID)
    .fetchOne()
    .value1();
 }

 private UUID createTestResource(String name) {
  return dslContext.insertInto(KNOWLEDGE_UNIT_RESOURCE)
    .set(KNOWLEDGE_UNIT_RESOURCE.NAME, name)
    .set(KNOWLEDGE_UNIT_RESOURCE.DESCRIPTION, "Test Resource Description")
    .set(KNOWLEDGE_UNIT_RESOURCE.CREATED_AT, LocalDateTime.now())
    .returningResult(KNOWLEDGE_UNIT_RESOURCE.ID)
    .fetchOne()
    .value1();
 }

 private UUID createTestDocumentGroup(String name) {
  return dslContext.insertInto(DOCUMENT_GROUP)
    .set(DOCUMENT_GROUP.URI, "/test/uri/" + name)
    .set(DOCUMENT_GROUP.CREATED_AT, LocalDateTime.now())
    .returningResult(DOCUMENT_GROUP.ID)
    .fetchOne()
    .value1();
 }

 private UUID createTestDocument(String name) {
  return dslContext.insertInto(DOCUMENT)
    .set(DOCUMENT.TITLE, name)
    .set(DOCUMENT.FILE_NAME, name + ".txt")
    .set(DOCUMENT.CONTENT, "Test content")
    .set(DOCUMENT.CREATED_AT, LocalDateTime.now())
    .returningResult(DOCUMENT.ID)
    .fetchOne()
    .value1();
 }

 private void linkKuWithResource() {
  dslContext
    .insertInto(
      tech.lacambra.kmanager.generated.jooq.tables.KnowledgeUnitContainedResource.KNOWLEDGE_UNIT_CONTAINED_RESOURCE)
    .set(
      tech.lacambra.kmanager.generated.jooq.tables.KnowledgeUnitContainedResource.KNOWLEDGE_UNIT_CONTAINED_RESOURCE.KNOWLEDGE_UNIT_ID,
      testKuId)
    .set(
      tech.lacambra.kmanager.generated.jooq.tables.KnowledgeUnitContainedResource.KNOWLEDGE_UNIT_CONTAINED_RESOURCE.KNOWLEDGE_UNIT_RESOURCE_ID,
      testResourceId)
    .set(
      tech.lacambra.kmanager.generated.jooq.tables.KnowledgeUnitContainedResource.KNOWLEDGE_UNIT_CONTAINED_RESOURCE.IS_DEFAULT,
      true)
    .execute();
 }

 private void linkResourceWithDocumentGroup() {
  dslContext.insertInto(RESOURCE_CONTAINED_DOCUMENT_GROUP)
    .set(RESOURCE_CONTAINED_DOCUMENT_GROUP.KNOWLEDGE_UNIT_RESOURCE_ID, testResourceId)
    .set(RESOURCE_CONTAINED_DOCUMENT_GROUP.DOCUMENT_GROUP_ID, testDocumentGroupId)
    .set(RESOURCE_CONTAINED_DOCUMENT_GROUP.IS_DEFAULT, true)
    .execute();
 }

 private void linkDocumentGroupWithDocument() {
  dslContext.insertInto(DOCUMENT_GROUP_DOCUMENT)
    .set(DOCUMENT_GROUP_DOCUMENT.DOCUMENT_GROUP_ID, testDocumentGroupId)
    .set(DOCUMENT_GROUP_DOCUMENT.DOCUMENT_ID, testDocumentId)
    .execute();
 }

 private void cleanUpData() {
  dslContext.deleteFrom(DOCUMENT_GROUP_DOCUMENT).execute();
  dslContext.deleteFrom(RESOURCE_CONTAINED_DOCUMENT_GROUP).execute();
  dslContext
    .deleteFrom(
      tech.lacambra.kmanager.generated.jooq.tables.KnowledgeUnitContainedResource.KNOWLEDGE_UNIT_CONTAINED_RESOURCE)
    .execute();
  dslContext.deleteFrom(DOCUMENT).execute();
  dslContext.deleteFrom(DOCUMENT_GROUP).execute();
  dslContext.deleteFrom(KNOWLEDGE_UNIT_RESOURCE).execute();
  dslContext.deleteFrom(KNOWLEDGE_UNIT).execute();
 }
}