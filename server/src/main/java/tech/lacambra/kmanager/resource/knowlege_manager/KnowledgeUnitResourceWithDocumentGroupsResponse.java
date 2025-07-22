package tech.lacambra.kmanager.resource.knowlege_manager;

import tech.lacambra.kmanager.generated.jooq.tables.pojos.KnowledgeUnitResource;
import tech.lacambra.kmanager.generated.jooq.tables.pojos.DocumentGroup;

import java.util.List;

public record KnowledgeUnitResourceWithDocumentGroupsResponse(
    KnowledgeUnitResource knowledgeUnitResource,
    List<DocumentGroup> documentGroups
) {
}