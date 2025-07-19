package tech.lacambra.kmanager.resource.knowlege_manager;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import tech.lacambra.kmanager.business.knowledge_unit.KnowledgeUnitRepository;
import tech.lacambra.kmanager.business.knowledge_unit.KnowledgeUnitService;
import tech.lacambra.kmanager.generated.jooq.tables.pojos.KnowledgeUnit;

import java.util.List;
import java.util.UUID;

@Path("/knowledge-units")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class KnowledgeUnitManagerResource {
    
    @Inject
    KnowledgeUnitService service;
    
    @Inject
    KnowledgeUnitRepository repository;
    
    @POST
    public Response createKnowledgeUnit(KnowledgeUnitRequest request) {
        UUID id = service.createKnowledgeUnit(request);
        return Response.status(Response.Status.CREATED)
                .entity(id)
                .build();
    }
    
    @PUT
    @Path("/{id}")
    public Response updateKnowledgeUnit(@PathParam("id") UUID id, KnowledgeUnitRequest request) {
        service.updateKnowledgeUnit(id, request);
        return Response.status(Response.Status.OK)
                .build();
    }
     
    @GET
    public List<KnowledgeUnit> getKnowledgeUnits() {
        return repository.findAll();
    }
    
    @GET
    @Path("/{id}")
    public KnowledgeUnitWithDocumentsResponse getKnowledgeUnitWithDocuments(@PathParam("id") UUID id) {
        return repository.findByIdWithDocuments(id)
                .orElseThrow(() -> new NotFoundException("Knowledge unit not found"));
    }
    
    @DELETE
    @Path("/{id}")
    public Response deleteKnowledgeUnit(@PathParam("id") UUID id) {
        repository.deleteKnowledgeUnit(id);
        return Response.status(Response.Status.NO_CONTENT).build();
    }
    
    @GET
    @Path("/{id}/download")
    @Produces("application/octet-stream")
    public Response downloadKnowledgeUnit(@PathParam("id") UUID id) {
        StreamingOutput stream = service.generateDownloadStream(id);
        String filename = service.generateDownloadFilename(id);
        
        return Response.ok(stream)
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .header("Content-Type", "text/plain; charset=utf-8")
                .build();
    }
}
