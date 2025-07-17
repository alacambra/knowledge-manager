package tech.lacambra.kmanager.business.knowledge_unit;

import static tech.lacambra.kmanager.generated.jooq.tables.KnowledgeUnit.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import tech.lacambra.kmanager.generated.jooq.tables.pojos.KnowledgeUnit;

@ApplicationScoped
public class knowledgeUnitRepository {
    
    @Inject
    DSLContext dslContext;
    
    public UUID create(KnowledgeUnitRequest request) {
        return dslContext.insertInto(KNOWLEDGE_UNIT)
            .set(KNOWLEDGE_UNIT.NAME, request.name())
            .set(KNOWLEDGE_UNIT.DESCRIPTION, request.description())
            .returningResult(KNOWLEDGE_UNIT.ID)
            .fetchOne()
            .value1();
    }
    
    public List<KnowledgeUnit> findAll() {
        return dslContext.select()
            .from(KNOWLEDGE_UNIT)
            .fetchInto(KnowledgeUnit.class);
    }
}