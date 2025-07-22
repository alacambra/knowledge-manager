package tech.lacambra.kmanager.services.file_storage;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.lacambra.kmanager.MinioTestResource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(value = MinioTestResource.class)
class MinioScanServiceTest {

    private static final String TEST_BUCKET = "test-bucket";
    private static final String TEST_PATH = "documents/";

    @Inject
    MinioScanService minioScanService;

    @Inject
    MinioClient minioClient;

    @BeforeEach
    void setUp() throws Exception {
        createBucketIfNotExists();
        setupTestFiles();
    }

    @Test
    void testScanPathWithMultipleFiles() {
        List<MinioScanService.MinioFileInfo> files = minioScanService.scanPath(TEST_BUCKET, TEST_PATH);

        assertNotNull(files);
        assertEquals(3, files.size());

        List<String> fileNames = files.stream()
                .map(MinioScanService.MinioFileInfo::getObjectName)
                .toList();

        assertTrue(fileNames.contains("documents/file1.txt"));
        assertTrue(fileNames.contains("documents/file2.txt"));
        assertTrue(fileNames.contains("documents/subfolder/file3.txt"));
    }

    @Test
    void testScanPathWithSpecificFile() {
        List<MinioScanService.MinioFileInfo> files = minioScanService.scanPath(TEST_BUCKET, "documents/file1.txt");

        assertNotNull(files);
        assertEquals(1, files.size());
        assertEquals("documents/file1.txt", files.get(0).getObjectName());
        assertTrue(files.get(0).getSize() > 0);
    }

    @Test
    void testGetFileContent() throws Exception {
        String testContent = "This is test file content";
        
        try (InputStream content = minioScanService.getFileContent(TEST_BUCKET, "documents/file1.txt")) {
            assertNotNull(content);
            
            String actualContent = new String(content.readAllBytes());
            assertEquals(testContent, actualContent);
        }
    }

    @Test
    void testScanEmptyPath() {
        List<MinioScanService.MinioFileInfo> files = minioScanService.scanPath(TEST_BUCKET, "empty/");

        assertNotNull(files);
        assertTrue(files.isEmpty());
    }

    private void createBucketIfNotExists() throws Exception {
        boolean exists = minioClient.bucketExists(
                io.minio.BucketExistsArgs.builder()
                        .bucket(TEST_BUCKET)
                        .build()
        );
        
        if (!exists) {
            minioClient.makeBucket(
                    io.minio.MakeBucketArgs.builder()
                            .bucket(TEST_BUCKET)
                            .build()
            );
        }
    }

    private void setupTestFiles() throws Exception {
        uploadTestFile("documents/file1.txt", "This is test file content");
        uploadTestFile("documents/file2.txt", "Another test file with different content");
        uploadTestFile("documents/subfolder/file3.txt", "File in subfolder");
    }

    private void uploadTestFile(String objectName, String content) throws Exception {
        byte[] contentBytes = content.getBytes();
        
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(TEST_BUCKET)
                        .object(objectName)
                        .stream(new ByteArrayInputStream(contentBytes), contentBytes.length, -1)
                        .build()
        );
    }
}