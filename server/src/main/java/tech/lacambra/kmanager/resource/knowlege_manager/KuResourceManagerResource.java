package tech.lacambra.kmanager.resource.knowlege_manager;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.lacambra.kmanager.business.kuResource.KuResourceInput;
import tech.lacambra.kmanager.business.kuResource.KuResourceService;

import java.util.UUID;

@Path("/ku-resources")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class KuResourceManagerResource {

    private final KuResourceService kuResourceService;

    @Inject
    public KuResourceManagerResource(KuResourceService kuResourceService) {
        this.kuResourceService = kuResourceService;
    }

    @PUT
    @Path("/{id}")
    public Response updateKuResource(@PathParam("id") UUID kuResourceId, KuResourceInput input) {
        kuResourceService.updateKuResource(kuResourceId, input);
        return Response.status(Response.Status.OK).build();
    }
}