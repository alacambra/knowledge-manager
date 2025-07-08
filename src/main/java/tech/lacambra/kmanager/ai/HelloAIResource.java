package tech.lacambra.kmanager.ai;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.lacambra.kmanager.documents.DocumentRepository;
import tech.lacambra.kmanager.generated.jooq.tables.pojos.Documents;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * HelloAI Resource that demonstrates embedding service integration
 * with document creation and retrieval.
 */
@Path("/helloai")
@Produces(MediaType.APPLICATION_JSON)
public class HelloAIResource {
    
    private static final Logger LOGGER = Logger.getLogger(HelloAIResource.class.getName());
    
    @Inject
    DocumentRepository documentRepository;
    
    @Inject
    EmbeddingService embeddingService;
    
    /**
     * Simple greeting endpoint that returns basic information.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        LOGGER.info("HelloAI endpoint called");
        int documentCount = documentRepository.getDocumentCount();
        return String.format("Hello from HelloAI! Current documents in database: %d", documentCount);
    }
    
    /**
     * Creates a random document with auto-generated content and embeddings.
     */
    @POST
    @Path("/create-random")
    public Response createRandomDocument() {
        LOGGER.info("Creating random document with embeddings");
        
        try {
            Documents document = documentRepository.createRandomDocumentWithEmbedding();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Random document created successfully");
            response.put("document", createDocumentSummary(document));
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            LOGGER.severe("Error creating random document: " + e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create random document");
            errorResponse.put("error", e.getMessage());
            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorResponse)
                .build();
        }
    }
    
    /**
     * Creates a document with custom text content and generates embeddings.
     */
    @POST
    @Path("/create-with-text")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createDocumentWithText(CreateDocumentRequest request) {
        if (request.text == null || request.text.trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Text content is required");
            
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(errorResponse)
                .build();
        }
        
        LOGGER.info("Creating document with custom text: " + 
            request.text.substring(0, Math.min(50, request.text.length())) + "...");
        
        try {
            Documents document = documentRepository.createRandomDocumentWithEmbedding(request.text);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Document created successfully with custom text");
            response.put("document", createDocumentSummary(document));
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            LOGGER.severe("Error creating document with custom text: " + e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create document with custom text");
            errorResponse.put("error", e.getMessage());
            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorResponse)
                .build();
        }
    }
    
    /**
     * Gets all documents with basic information.
     */
    @GET
    @Path("/documents")
    public Response getAllDocuments() {
        LOGGER.info("Retrieving all documents");
        
        try {
            var documents = documentRepository.getAllDocuments();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", documents.size());
            response.put("documents", documents.stream()
                .map(this::createDocumentSummary)
                .toList());
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            LOGGER.severe("Error retrieving documents: " + e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to retrieve documents");
            errorResponse.put("error", e.getMessage());
            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorResponse)
                .build();
        }
    }
    
    /**
     * Gets a specific document by ID.
     */
    @GET
    @Path("/documents/{id}")
    public Response getDocument(@PathParam("id") Long id) {
        LOGGER.info("Retrieving document with ID: " + id);
        
        try {
            Documents document = documentRepository.findById(id);
            
            if (document == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Document not found");
                
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorResponse)
                    .build();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("document", createDetailedDocumentSummary(document));
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            LOGGER.severe("Error retrieving document: " + e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to retrieve document");
            errorResponse.put("error", e.getMessage());
            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorResponse)
                .build();
        }
    }
    
    /**
     * Test endpoint to verify embedding service is working.
     */
    @POST
    @Path("/test-embedding")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response testEmbedding(CreateDocumentRequest request) {
        if (request.text == null || request.text.trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Text content is required");
            
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(errorResponse)
                .build();
        }
        
        LOGGER.info("Testing embedding generation for text: " + 
            request.text.substring(0, Math.min(50, request.text.length())) + "...");
        
        try {
            float[] embedding = embeddingService.generateEmbedding(request.text);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Embedding generated successfully");
            response.put("embeddingDimension", embedding.length);
            response.put("embeddingPreview", java.util.Arrays.copyOfRange(embedding, 0, Math.min(10, embedding.length)));
            response.put("textLength", request.text.length());
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            LOGGER.severe("Error generating embedding: " + e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to generate embedding");
            errorResponse.put("error", e.getMessage());
            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorResponse)
                .build();
        }
    }
    
    /**
     * Creates a summary of a document without the full embedding data.
     */
    private Map<String, Object> createDocumentSummary(Documents document) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("id", document.getId());
        summary.put("title", document.getTitle());
        summary.put("contentPreview", document.getContent().substring(0, Math.min(100, document.getContent().length())) + "...");
        summary.put("contentLength", document.getContent().length());
        summary.put("hasEmbedding", document.getEmbedding() != null);
        summary.put("createdAt", document.getCreatedAt());
        summary.put("updatedAt", document.getUpdatedAt());
        return summary;
    }
    
    /**
     * Creates a detailed summary of a document including metadata.
     */
    private Map<String, Object> createDetailedDocumentSummary(Documents document) {
        Map<String, Object> summary = createDocumentSummary(document);
        summary.put("fullContent", document.getContent());
        summary.put("metadata", document.getMetadata());
        return summary;
    }
    
    /**
     * Request DTO for creating documents with custom text.
     */
    public static class CreateDocumentRequest {
        public String text;
    }
}