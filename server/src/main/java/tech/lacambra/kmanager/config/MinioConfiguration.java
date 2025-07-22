package tech.lacambra.kmanager.config;

import io.minio.MinioClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class MinioConfiguration {
    
    @ConfigProperty(name = "quarkus.minio.url")
    String minioUrl;
    
    @ConfigProperty(name = "quarkus.minio.access-key")
    String accessKey;
    
    @ConfigProperty(name = "quarkus.minio.secret-key")
    String secretKey;
    
    @Produces
    @ApplicationScoped
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(accessKey, secretKey)
                .build();
    }
}