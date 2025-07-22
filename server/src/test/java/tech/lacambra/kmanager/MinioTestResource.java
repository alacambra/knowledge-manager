package tech.lacambra.kmanager;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class MinioTestResource implements QuarkusTestResourceLifecycleManager {
    private static final Logger LOGGER = Logger.getLogger(MinioTestResource.class.getName());
    
    private static final String ACCESS_KEY = "testuser";
    private static final String SECRET_KEY = "testpass123";

    @Container
    private MinIOContainer minioContainer;

    @Override
    public Map<String, String> start() {
        LOGGER.info("Starting MinIO testcontainer...");
        
        minioContainer = new MinIOContainer("minio/minio:RELEASE.2023-09-04T19-57-37Z")
                .withUserName(ACCESS_KEY)
                .withPassword(SECRET_KEY);
        
        minioContainer.start();
        
        String minioUrl = String.format("http://%s:%d", 
                minioContainer.getHost(), 
                minioContainer.getFirstMappedPort());
        
        LOGGER.info("MinIO testcontainer started successfully! URL: " + minioUrl);
        
        Map<String, String> config = new HashMap<>();
        config.put("quarkus.minio.url", minioUrl);
        config.put("quarkus.minio.access-key", ACCESS_KEY);
        config.put("quarkus.minio.secret-key", SECRET_KEY);
        
        return config;
    }

    @Override
    public void stop() {
        if (minioContainer != null) {
            minioContainer.stop();
        }
    }
    
    public static String getAccessKey() {
        return ACCESS_KEY;
    }
    
    public static String getSecretKey() {
        return SECRET_KEY;
    }
}