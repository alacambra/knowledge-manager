package tech.lacambra.kmanager.resource.knowlege_manager;

import tech.lacambra.kmanager.generated.jooq.tables.pojos.KnowledgeUnit;

import java.util.List;

public record KnowledgeUnitWithResourcesResponse(
    KnowledgeUnit knowledgeUnit,
    List<KnowledgeUnitResourceWithDocumentGroupsResponse> resources
) {
}