package tech.lacambra.kmanager.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import tech.lacambra.kmanager.business.documents.DocumentRepositoryOld;

import java.util.logging.Logger;

@Path("/hello")
@Produces(MediaType.APPLICATION_JSON)
public class GreetingResource {
    
    private static final Logger LOGGER = Logger.getLogger(GreetingResource.class.getName());
    
    @Inject
    DocumentRepositoryOld documentRepository;
    
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        LOGGER.info("Hello endpoint called");
        var docs = documentRepository.getAllDocuments();
        LOGGER.info("Document retrieved successfully" + docs);
        return "Hello from Quarkus REST-albert";
    }
}