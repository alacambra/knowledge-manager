package tech.lacambra.kmanager.business.documents;

import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.lacambra.kmanager.generated.jooq.tables.pojos.Document;
import tech.lacambra.kmanager.resource.knowlege_manager.DocumentGroupRequest;

@ApplicationScoped
public class DocumentService {

    private DocumentRepositoryOld documentRepository;

    @Inject
    public DocumentService(DocumentRepositoryOld documentRepository) {
        this.documentRepository = documentRepository;
    }

    // public List<UUID> uploadDocuments(List<DocumentGroupRequest> documents) {
    //     return documentRepository.createDocuments(documents);
    // }

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