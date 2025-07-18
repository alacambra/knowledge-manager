package tech.lacambra.kmanager.resource.knowlege_manager;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import tech.lacambra.kmanager.PostgresTestResource;
import tech.lacambra.kmanager.services.ai.EmbeddingService;

import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class)
class KnowledgeUnitUpdateTest {

    @InjectMock
    EmbeddingService embeddingService;

    @Test
    void testKnowledgeUnitLifecycle() {
        // Step 1: Create a KU with 2 documents
        String kuId = createKnowledgeUnitWithTwoDocuments();
        
        // Step 2: Verify KU was created with 2 documents
        verifyKnowledgeUnitHasDocuments(kuId, 2, "Initial KU", "Initial description");
        
        // Step 3: Create an additional standalone document to add later
        String standaloneDocId = createStandaloneDocument();
        
        // Step 4: Get one document ID from the KU to remove later
        Response kuResponse = given()
            .when().get("/knowledge-units/" + kuId)
            .then()
            .statusCode(200)
            .extract().response();
        
        List<String> documentIds = kuResponse.jsonPath().getList("documents.id");
        String documentToRemove = documentIds.get(0);
        
        // Step 5: Update the KU - change name/description, add new document, remove existing one
        updateKnowledgeUnit(kuId, standaloneDocId, documentToRemove);
        
        // Step 6: Verify the final state
        verifyFinalState(kuId, standaloneDocId, documentToRemove);
    }

    private String createKnowledgeUnitWithTwoDocuments() {
        Response response = given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "name": "Initial KU",
                    "description": "Initial description",
                    "newDocuments": [
                        {
                            "name": "Document 1",
                            "content": "Content of document 1"
                        },
                        {
                            "name": "Document 2", 
                            "content": "Content of document 2"
                        }
                    ]
                }
                """)
            .when().post("/knowledge-units")
            .then()
            .statusCode(201)
            .extract().response();
        
        return response.asString().replaceAll("\"", "");
    }

    private void verifyKnowledgeUnitHasDocuments(String kuId, int expectedDocCount, String expectedName, String expectedDescription) {
        given()
            .when().get("/knowledge-units/" + kuId)
            .then()
            .statusCode(200)
            .body("knowledgeUnit.name", equalTo(expectedName))
            .body("knowledgeUnit.description", equalTo(expectedDescription))
            .body("documents", hasSize(expectedDocCount));
    }

    private String createStandaloneDocument() {
        // First create a KU with one document to get a standalone document
        Response response = given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "name": "Temp KU",
                    "description": "Temporary KU for standalone document",
                    "newDocuments": [
                        {
                            "name": "Standalone Document",
                            "content": "This document will be moved to another KU"
                        }
                    ]
                }
                """)
            .when().post("/knowledge-units")
            .then()
            .statusCode(201)
            .extract().response();

        String tempKuId = response.asString().replaceAll("\"", "");
        
        // Get the document ID from the temp KU
        Response kuResponse = given()
            .when().get("/knowledge-units/" + tempKuId)
            .then()
            .statusCode(200)
            .extract().response();
        
        return kuResponse.jsonPath().getList("documents.id", String.class).get(0);
    }

    private void updateKnowledgeUnit(String kuId, String documentToAdd, String documentToRemove) {
        given()
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                    "name": "Updated KU Name",
                    "description": "Updated description",
                    "newDocuments": [
                        {
                            "name": "New Document",
                            "content": "Content of newly created document"
                        }
                    ],
                    "addedDocumentsIds": ["%s"],
                    "removedDocumentsIds": ["%s"]
                }
                """, documentToAdd, documentToRemove))
            .when().put("/knowledge-units/" + kuId)
            .then()
            .statusCode(200);
    }

    private void verifyFinalState(String kuId, String addedDocId, String removedDocId) {
        Response response = given()
            .when().get("/knowledge-units/" + kuId)
            .then()
            .statusCode(200)
            .body("knowledgeUnit.name", equalTo("Updated KU Name"))
            .body("knowledgeUnit.description", equalTo("Updated description"))
            .body("documents", hasSize(3)) // 2 original - 1 removed + 1 added + 1 new = 3
            .extract().response();
        
        List<String> finalDocumentIds = response.jsonPath().getList("documents.id", String.class);
        List<String> documentTitles = response.jsonPath().getList("documents.title", String.class);
        
        // Verify the added document is present
        assertTrue(finalDocumentIds.contains(addedDocId), "Added document should be present");
        
        // Verify the removed document is not present  
        assertFalse(finalDocumentIds.contains(removedDocId), "Removed document should not be present");
        
        // Verify the new document was created
        assertTrue(documentTitles.contains("New Document"), "New document should be present");
        
        // Verify we still have one of the original documents
        assertTrue(documentTitles.contains("Document 1") || documentTitles.contains("Document 2"), 
                  "At least one original document should remain");
    }
}