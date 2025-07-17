package tech.lacambra.kmanager.resource.knowlege_manager;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import io.restassured.http.ContentType;
import tech.lacambra.kmanager.PostgresTestResource;
import tech.lacambra.kmanager.services.ai.EmbeddingService;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class)
class KnowledgeUnitResourceTest {

    @InjectMock
    EmbeddingService embeddingService;

    @Test
    void testCreateKnowledgeUnitsAndGetList() {
        // Create first knowledge unit
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Backend Development Process",
                            "description": "Complete guide for backend development including architecture, patterns, and best practices"
                        }
                        """)
                .when().post("/knowledge-units")
                .then()
                .statusCode(201);

        // Create second knowledge unit
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Database Design Principles",
                            "description": "Guidelines for designing efficient and scalable database schemas"
                        }
                        """)
                .when().post("/knowledge-units")
                .then()
                .statusCode(201);

        // Create third knowledge unit
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "API Security Best Practices",
                            "description": "Security measures and practices for REST API development"
                        }
                        """)
                .when().post("/knowledge-units")
                .then()
                .statusCode(201);

        // Get all knowledge units and verify we have 3 different items
        given()
                .when().get("/knowledge-units")
                .then()
                .statusCode(200)
                .body("", hasSize(3))
                .body("[0].name", is("Backend Development Process"))
                .body("[0].description", is(
                        "Complete guide for backend development including architecture, patterns, and best practices"))
                .body("[1].name", is("Database Design Principles"))
                .body("[1].description", is("Guidelines for designing efficient and scalable database schemas"))
                .body("[2].name", is("API Security Best Practices"))
                .body("[2].description", is("Security measures and practices for REST API development"));
    }
}