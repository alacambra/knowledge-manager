package tech.lacambra.kmanager.business.knowledge_unit;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.StreamingOutput;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.lacambra.kmanager.MinioTestResource;
import tech.lacambra.kmanager.PostgresTestResource;
import tech.lacambra.kmanager.business.documents.DocumentRepositoryOld;
import tech.lacambra.kmanager.business.kuResource.KuResourceRepository;
import tech.lacambra.kmanager.generated.jooq.tables.pojos.KnowledgeUnit;
import tech.lacambra.kmanager.resource.knowlege_manager.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
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
@QuarkusTestResource(value = MinioTestResource.class)
class KnowledgeUnitServiceTest {

 @Inject
 KnowledgeUnitService knowledgeUnitService;

 @Inject
 KnowledgeUnitRepository knowledgeUnitRepository;

 @Inject
 KuResourceRepository kuResourceRepository;

 @Inject
 DocumentRepositoryOld documentRepository;

 @Inject
 DSLContext dslContext;

 @Inject
 MinioClient minioClient;

 private static final String TEST_BUCKET = "test-bucket";
 private UUID testKuId;
 private UUID testResourceId;
 private UUID testDocumentGroupId;

 @BeforeEach
 void setUp() throws Exception {
  cleanUpData();
  createMinIOBucket();
  createTestData();
 }

 @Test
 void testCreateKnowledgeUnit_withResources() {
  DocumentGroupRequest documentGroupRequest = new DocumentGroupRequest("test-bucket/new-path");
  KuResourceRequest resourceRequest = new KuResourceRequest(
    "Resource 1",
    "Resource Description",
    List.of(documentGroupRequest),
    null,
    null);

  KnowledgeUnitRequest request = new KnowledgeUnitRequest(
    "New KU",
    "New Description",
    List.of(resourceRequest));

  UUID createdId = knowledgeUnitService.createKnowledgeUnit(request);

  assertNotNull(createdId);

  KnowledgeUnit ku = dslContext.select()
    .from(KNOWLEDGE_UNIT)
    .where(KNOWLEDGE_UNIT.ID.eq(createdId))
    .fetchOneInto(KnowledgeUnit.class);

  assertNotNull(ku);
  assertEquals("New KU", ku.getName());
  assertEquals("New Description", ku.getDescription());
 }

 @Test
 void testCreateKnowledgeUnit_withoutResources() {
  KnowledgeUnitRequest request = new KnowledgeUnitRequest(
    "KU Without Resources",
    "Description",
    null);

  UUID createdId = knowledgeUnitService.createKnowledgeUnit(request);

  assertNotNull(createdId);

  long resourceCount = dslContext.selectCount()
    .from(tech.lacambra.kmanager.generated.jooq.tables.KnowledgeUnitContainedResource.KNOWLEDGE_UNIT_CONTAINED_RESOURCE)
    .where(
      tech.lacambra.kmanager.generated.jooq.tables.KnowledgeUnitContainedResource.KNOWLEDGE_UNIT_CONTAINED_RESOURCE.KNOWLEDGE_UNIT_ID
        .eq(createdId))
    .fetchOne(0, Long.class);

  assertEquals(1, resourceCount); // Default resource should be created
 }

 @Test
 void testUpdateKnowledgeUnit() {
  DocumentGroupRequest documentGroupRequest = new DocumentGroupRequest("test-bucket/updated-path");
  KuResourceRequest resourceRequest = new KuResourceRequest(
    "Updated Resource",
    "Updated Description",
    List.of(documentGroupRequest),
    null,
    null);

  KnowledgeUnitRequest request = new KnowledgeUnitRequest(
    "Updated KU",
    "Updated Description",
    List.of(resourceRequest));

  knowledgeUnitService.updateKnowledgeUnit(testKuId, request);

  KnowledgeUnit updatedKu = dslContext.select()
    .from(KNOWLEDGE_UNIT)
    .where(KNOWLEDGE_UNIT.ID.eq(testKuId))
    .fetchOneInto(KnowledgeUnit.class);

  assertNotNull(updatedKu);
  assertEquals("Updated KU", updatedKu.getName());
  assertEquals("Updated Description", updatedKu.getDescription());
 }

 @Test
 void testGenerateConcatenatedText_withMinIOFiles() throws Exception {
  uploadTestFilesToMinIO();

  String result = knowledgeUnitService.generateConcatenatedText(testKuId);

  assertNotNull(result);
  assertFalse(result.isEmpty());
 }

 @Test
 void testGenerateConcatenatedText_knowledgeUnitNotFound() {
  UUID nonExistentId = UUID.randomUUID();

  assertThrows(KnowledgeUnitNotFoundException.class,
    () -> knowledgeUnitService.generateConcatenatedText(nonExistentId));
 }

 @Test
 void testGenerateConcatenatedPDF_withMinIOFiles() throws Exception {
  uploadTestPDFToMinIO();

  byte[] result = knowledgeUnitService.generateConcatenatedPDF(testKuId);

  assertNotNull(result);
  assertTrue(result.length > 0);

  persistTestPDF(result, "generated-knowledge-unit-" + testKuId + ".pdf");
 }

 @Test
 void testHasPdfFiles_withPdfFiles() throws Exception {
  uploadTestPDFToMinIO();

  boolean result = knowledgeUnitService.hasPdfFiles(testKuId);

  assertTrue(result);
 }

 @Test
 void testHasPdfFiles_withoutPdfFiles() throws Exception {
  uploadTestTextFileToMinIO();

  boolean result = knowledgeUnitService.hasPdfFiles(testKuId);

  assertFalse(result);
 }

 @Test
 void testHasTextFiles_withTextFiles() throws Exception {
  uploadTestTextFileToMinIO();

  boolean result = knowledgeUnitService.hasTextFiles(testKuId);

  assertTrue(result);
 }

 @Test
 void testHasTextFiles_withoutTextFiles() throws Exception {
  uploadTestPDFToMinIO();

  boolean result = knowledgeUnitService.hasTextFiles(testKuId);

  assertFalse(result);
 }

 @Test
 void testExportToFile_withCustomFilename() throws Exception {
  uploadTestTextFileToMinIO();
  String customFilename = "custom-export.txt";

  Path result = knowledgeUnitService.exportToFile(testKuId, customFilename);

  assertNotNull(result);
  assertTrue(Files.exists(result));
  assertTrue(result.toString().contains(customFilename));

  String content = Files.readString(result);
  assertFalse(content.isEmpty());

  Files.deleteIfExists(result);
 }

 @Test
 void testExportToFile_withDefaultFilename() throws Exception {
  uploadTestTextFileToMinIO();

  Path result = knowledgeUnitService.exportToFile(testKuId, null);

  assertNotNull(result);
  assertTrue(Files.exists(result));

  String content = Files.readString(result);
  assertFalse(content.isEmpty());

  Files.deleteIfExists(result);
 }

 @Test
 void testGenerateDownloadStream() throws Exception {
  uploadTestTextFileToMinIO();

  StreamingOutput streamingOutput = knowledgeUnitService.generateDownloadStream(testKuId);

  assertNotNull(streamingOutput);

  ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
  streamingOutput.write(outputStream);

  String result = outputStream.toString();
  assertFalse(result.isEmpty());
 }

 @Test
 void testGenerateDownloadFilename() {
  String filename = knowledgeUnitService.generateDownloadFilename(testKuId);

  assertNotNull(filename);
  assertTrue(filename.endsWith(".txt"));
  assertTrue(filename.contains("Test_KU"));
 }

 @Test
 void testGetDocumentUrlsForKnowledgeUnit() throws Exception {
  uploadTestFilesToMinIO();

  List<String> urls = knowledgeUnitService.getDocumentUrlsForKnowledgeUnit(testKuId);

  assertNotNull(urls);
  assertFalse(urls.isEmpty());
 }

 @Test
 void testGetDocumentUrlsForKnowledgeUnit_knowledgeUnitNotFound() {
  UUID nonExistentId = UUID.randomUUID();

  assertThrows(KnowledgeUnitNotFoundException.class,
    () -> knowledgeUnitService.getDocumentUrlsForKnowledgeUnit(nonExistentId));
 }

 private void createTestData() {
  testKuId = createTestKnowledgeUnit("Test KU");
  testResourceId = createTestResource("Test Resource");
  testDocumentGroupId = createTestDocumentGroup("test-bucket/test-path");

  linkKuWithResource();
  linkResourceWithDocumentGroup();
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

 private UUID createTestDocumentGroup(String uri) {
  return dslContext.insertInto(DOCUMENT_GROUP)
    .set(DOCUMENT_GROUP.URI, uri)
    .set(DOCUMENT_GROUP.CREATED_AT, LocalDateTime.now())
    .returningResult(DOCUMENT_GROUP.ID)
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

 private void createMinIOBucket() throws Exception {
  if (!minioClient.bucketExists(io.minio.BucketExistsArgs.builder().bucket(TEST_BUCKET).build())) {
   minioClient.makeBucket(io.minio.MakeBucketArgs.builder().bucket(TEST_BUCKET).build());
  }
 }

 private void uploadTestFilesToMinIO() throws Exception {
  uploadTestTextFileToMinIO();
  uploadTestPDFToMinIO();
 }

 private void uploadTestTextFileToMinIO() throws Exception {
  String content = "This is test content for the knowledge unit.";
  ByteArrayInputStream stream = new ByteArrayInputStream(content.getBytes());

  minioClient.putObject(PutObjectArgs.builder()
    .bucket(TEST_BUCKET)
    .object("test-path/test-file.txt")
    .stream(stream, content.length(), -1)
    .contentType("text/plain")
    .build());
 }

 private void uploadTestPDFToMinIO() throws Exception {
  byte[] pdfContent = createMinimalPDFContent();
  ByteArrayInputStream stream = new ByteArrayInputStream(pdfContent);

  minioClient.putObject(PutObjectArgs.builder()
    .bucket(TEST_BUCKET)
    .object("test-path/test-file.pdf")
    .stream(stream, pdfContent.length, -1)
    .contentType("application/pdf")
    .build());
 }

 private byte[] createMinimalPDFContent() {
  String pdfHeader = "%PDF-1.4\n1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] >>\nendobj\nxref\n0 4\n0000000000 65535 f \n0000000009 00000 n \n0000000058 00000 n \n0000000115 00000 n \ntrailer\n<< /Size 4 /Root 1 0 R >>\nstartxref\n174\n%%EOF";
  return pdfHeader.getBytes();
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

 private void persistTestPDF(byte[] pdfData, String filename) throws Exception {
  Path testPdfDir = Path.of("target/testpdf");
  Files.createDirectories(testPdfDir);

  Path pdfFile = testPdfDir.resolve("test-" + System.currentTimeMillis() + "__" + filename);
  Files.write(pdfFile, pdfData);

  assertTrue(Files.exists(pdfFile));
  assertTrue(Files.size(pdfFile) > 0);
 }
}