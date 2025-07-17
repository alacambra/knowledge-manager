package tech.lacambra.kmanager.resource.knowlege_manager;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import io.restassured.http.ContentType;
import tech.lacambra.kmanager.PostgresTestResource;
import tech.lacambra.kmanager.services.ai.EmbeddingService;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasItems;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class)
class KnowledgeUnitResourceTest {

 @InjectMock
 EmbeddingService embeddingService;

 private void createKnowledgeUnit(String name, String description) {
  given()
    .contentType(ContentType.JSON)
    .body("""
      {
          "name": "%s",
          "description": "%s",
          "documents": [
              {
                  "name": "Sample Document 1",
                  "content": "This is the content of the first document"
              },
              {
                  "name": "Sample Document 2",
                  "content": "This is the content of the second document"
              }
          ]
      }
      """.formatted(name, description))
    .when().post("/knowledge-units")
    .then()
    .statusCode(201);
 }

 @Test
 void testCreateKnowledgeUnitsAndGetList() {
  String backendName = "Backend Development Process";
  String backendDescription = "Complete guide for backend development including architecture, patterns, and best practices";

  String databaseName = "Database Design Principles";
  String databaseDescription = "Guidelines for designing efficient and scalable database schemas";

  String apiSecurityName = "API Security Best Practices";
  String apiSecurityDescription = "Security measures and practices for REST API development";

  createKnowledgeUnit(backendName, backendDescription);
  createKnowledgeUnit(databaseName, databaseDescription);
  createKnowledgeUnit(apiSecurityName, apiSecurityDescription);

  given()
    .when().get("/knowledge-units")
    .then()
    .statusCode(200)
    .body("", hasSize(3))
    .body("name", hasItems(backendName, databaseName, apiSecurityName));

 }

}