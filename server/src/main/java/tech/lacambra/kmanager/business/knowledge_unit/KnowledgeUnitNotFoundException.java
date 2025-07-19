package tech.lacambra.kmanager.business.knowledge_unit;

import java.util.UUID;

public class KnowledgeUnitNotFoundException extends RuntimeException {

    public KnowledgeUnitNotFoundException(UUID knowledgeUnitId) {
        super("Knowledge unit not found with ID: " + knowledgeUnitId);
    }

    public KnowledgeUnitNotFoundException(String message) {
        super(message);
    }

    public KnowledgeUnitNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}