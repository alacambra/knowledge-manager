package tech.lacambra.kmanager;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class PostgresTestResource implements QuarkusTestResourceLifecycleManager {
    private static final Logger LOGGER = Logger.getLogger(PostgresTestResource.class.getName());
    
    // Configuration property keys
    private static final String USE_EXTERNAL_DB_PROP = "tech.lacambra.kmanager.useExternalDb";
    private static final String EXTERNAL_DB_URL_PROP = "tech.lacambra.kmanager.externalDb.url";
    private static final String EXTERNAL_DB_USERNAME_PROP = "tech.lacambra.kmanager.externalDb.username";
    private static final String EXTERNAL_DB_PASSWORD_PROP = "tech.lacambra.kmanager.externalDb.password";

    // Default values
    private static final String DEFAULT_DB_NAME = "kmanager_test";
    private static final String DEFAULT_USERNAME = "test";
    private static final String DEFAULT_PASSWORD = "test";

    // Testcontainers PostgreSQL image with pgvector extension
    static DockerImageName pgvectorImage = DockerImageName.parse("pgvector/pgvector:pg16")
            .asCompatibleSubstituteFor("postgres");

    @Container
    private PostgreSQLContainer<?> database;

    @Override
    public Map<String, String> start() {
        Map<String, String> config = new HashMap<>();

        // Check if external database should be used
        boolean useExternalDb = Boolean.parseBoolean(
                System.getProperty(USE_EXTERNAL_DB_PROP, "false"));

        if (useExternalDb) {
            // Use external database properties
            String externalUrl = System.getProperty(EXTERNAL_DB_URL_PROP);
            String externalUsername = System.getProperty(EXTERNAL_DB_USERNAME_PROP);
            String externalPassword = System.getProperty(EXTERNAL_DB_PASSWORD_PROP);

            if (externalUrl == null || externalUsername == null || externalPassword == null) {
                throw new IllegalStateException(
                        "External database configuration is incomplete. " +
                                "Please provide all properties: " +
                                USE_EXTERNAL_DB_PROP + ", " +
                                EXTERNAL_DB_URL_PROP + ", " +
                                EXTERNAL_DB_USERNAME_PROP + ", " +
                                EXTERNAL_DB_PASSWORD_PROP);
            }

            config.put("quarkus.datasource.jdbc.url", externalUrl);
            config.put("quarkus.datasource.username", externalUsername);
            config.put("quarkus.datasource.password", externalPassword);
        } else {
            // Use Testcontainers PostgreSQL with pgvector
            LOGGER.info("Starting PostgreSQL testcontainer with pgvector...");
            
            database = new PostgreSQLContainer<>(pgvectorImage)
                    .withDatabaseName(DEFAULT_DB_NAME)
                    .withUsername(DEFAULT_USERNAME)
                    .withPassword(DEFAULT_PASSWORD)
                    .withCommand("postgres -c fsync=off") // Speeds up tests
                    .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*\\n", 2));

            database.start();

            LOGGER.info("PostgreSQL testcontainer started successfully! Container ID: " + database.getContainerId() + 
                       ", JDBC URL: " + database.getJdbcUrl() + 
                       ", Username: " + database.getUsername() + 
                       ", Database: " + database.getDatabaseName() + 
                       ", Host: " + database.getHost() + ":" + database.getFirstMappedPort());

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            config.put("quarkus.datasource.jdbc.url", database.getJdbcUrl());
            config.put("quarkus.datasource.username", database.getUsername());
            config.put("quarkus.datasource.password", database.getPassword());
        }


        return config;
    }

    @Override
    public void stop() {
        if (database != null) {
            database.stop();
        }
    }
}