package tech.lacambra.kmanager.business.documents;

import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.lacambra.kmanager.generated.jooq.tables.pojos.Document;
import tech.lacambra.kmanager.resource.knowlege_manager.DocumentRequest;

@ApplicationScoped
public class DocumentService {

    private DocumentRepository documentRepository;

    @Inject
    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public List<UUID> uploadDocuments(List<DocumentRequest> documents) {
        return documentRepository.createDocuments(documents);
    }

    public List<Document> getAllDocuments() {
        return documentRepository.getAllDocuments();
    }

    public boolean removeDocument(UUID documentId) {
        Document document = documentRepository.findById(documentId);
        if (document == null) {
            return false;
        }
        return documentRepository.deleteDocument(documentId);
    }
}