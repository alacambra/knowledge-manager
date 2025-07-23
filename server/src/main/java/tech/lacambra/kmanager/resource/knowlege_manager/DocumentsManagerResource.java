package tech.lacambra.kmanager.resource.knowlege_manager;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.lacambra.kmanager.business.documents.DocumentService;
import tech.lacambra.kmanager.generated.jooq.tables.pojos.Document;

import java.util.List;
import java.util.UUID;

@Path("/documents")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DocumentsManagerResource {
    
    @Inject
    DocumentService documentService;
    
    // @POST
    // @Path("/upload")
    // public Response uploadDocuments(List<DocumentGroupRequest> documents) {
    //     List<UUID> documentIds = documentService.uploadDocuments(documents);
    //     return Response.status(Response.Status.CREATED)
    //             .entity(documentIds)
    //             .build();
    // }
    
    @GET
    public List<Document> getAllDocuments() {
        return documentService.getAllDocuments();
    }
    
    @DELETE
    @Path("/{id}")
    public Response removeDocument(@PathParam("id") UUID documentId) {
        boolean deleted = documentService.removeDocument(documentId);
        if (deleted) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}