package tech.lacambra.kmanager.business.knowledge_unit;

import java.time.format.DateTimeFormatter;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.lacambra.kmanager.generated.jooq.tables.pojos.Document;
import tech.lacambra.kmanager.generated.jooq.tables.pojos.KnowledgeUnit;
import tech.lacambra.kmanager.resource.knowlege_manager.KnowledgeUnitWithDocumentsResponse;
import tech.lacambra.kmanager.util.ContentCleanupUtil;

@Deprecated
@ApplicationScoped
public class KnowledgeUnitContentProcessor {

    private final ContentCleanupUtil cleanupUtil;

    @Inject
    public KnowledgeUnitContentProcessor(ContentCleanupUtil cleanupUtil) {
        this.cleanupUtil = cleanupUtil;
    }

    public String processKnowledgeUnitToText(KnowledgeUnitWithDocumentsResponse data) {
        if (data == null || data.knowledgeUnit() == null) {
            throw new ContentProcessingException("Knowledge unit data cannot be null");
        }

        try {
            StringBuilder result = new StringBuilder();
            
            result.append(buildMetadataSection(data.knowledgeUnit(), data.documents()));
            result.append("\n");
            result.append(buildContentSection(data.documents()));
            
            return result.toString();
        } catch (Exception e) {
            throw new ContentProcessingException("Failed to process knowledge unit content", e);
        }
    }

    public String buildMetadataSection(KnowledgeUnit ku, List<Document> documents) {
        StringBuilder metadata = new StringBuilder();
        
        metadata.append("[KNOWLEDGE_UNIT_METADATA_START]\n");
        metadata.append("NAME: ").append(cleanupUtil.cleanMetadataValue(ku.getName())).append("\n");
        metadata.append("DESCRIPTION: ").append(cleanupUtil.cleanMetadataValue(ku.getDescription())).append("\n");
        metadata.append("DOCUMENT_COUNT: ").append(documents != null ? documents.size() : 0).append("\n");
        
        if (ku.getCreatedAt() != null) {
            metadata.append("CREATED_AT: ").append(ku.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\n");
        }
        
        if (ku.getUpdatedAt() != null) {
            metadata.append("UPDATED_AT: ").append(ku.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\n");
        }
        
        metadata.append("\n[KNOWLEDGE_UNIT_METADATA_END]");
        
        return metadata.toString();
    }

    public String buildContentSection(List<Document> documents) {
        StringBuilder content = new StringBuilder();
        
        content.append("[CONTENT_START]\n");
        
        if (documents != null && !documents.isEmpty()) {
            for (Document document : documents) {
                content.append("\n[DOCUMENT: ").append(cleanupUtil.cleanMetadataValue(document.getTitle())).append("]\n");
                content.append(cleanupUtil.cleanDocumentContent(document.getContent()));
                content.append("\n");
            }
        }
        
        content.append("[CONTENT_END]");
        
        return content.toString();
    }
}