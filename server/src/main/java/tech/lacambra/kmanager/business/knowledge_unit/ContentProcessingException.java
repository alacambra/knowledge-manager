package tech.lacambra.kmanager.business.knowledge_unit;

public class ContentProcessingException extends RuntimeException {

    public ContentProcessingException(String message) {
        super(message);
    }

    public ContentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}