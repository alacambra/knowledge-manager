package tech.lacambra.kmanager.resource.knowlege_manager;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.lacambra.kmanager.business.knowledge_unit.KnowledgeUnitRepository;
import tech.lacambra.kmanager.business.knowledge_unit.KnowledgeUnitService;
import tech.lacambra.kmanager.generated.jooq.tables.pojos.KnowledgeUnit;

import java.util.List;
import java.util.UUID;

@Path("/knowledge-units")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class KnowledgeUnitResource {
    
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
    
    @GET
    public List<KnowledgeUnit> getKnowledgeUnits() {
        return repository.findAll();
    }
}
