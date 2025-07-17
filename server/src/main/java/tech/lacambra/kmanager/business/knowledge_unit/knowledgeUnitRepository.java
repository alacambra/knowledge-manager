package tech.lacambra.kmanager.business.knowledge_unit;

import static tech.lacambra.kmanager.generated.jooq.tables.KnowledgeUnit.*;
import static tech.lacambra.kmanager.generated.jooq.tables.KnowledgeUnitDocument.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import tech.lacambra.kmanager.generated.jooq.tables.pojos.KnowledgeUnit;
import tech.lacambra.kmanager.generated.jooq.tables.pojos.KnowledgeUnitDocument;

@ApplicationScoped
public class KnowledgeUnitRepository {

 @Inject
 DSLContext dslContext;

 public UUID create(KnowledgeUnitInput input) {
  return dslContext.insertInto(KNOWLEDGE_UNIT)
    .set(KNOWLEDGE_UNIT.NAME, input.name())
    .set(KNOWLEDGE_UNIT.DESCRIPTION, input.description())
    .returningResult(KNOWLEDGE_UNIT.ID)
    .fetchOne()
    .value1();
 }

 public void addDocumentsToKU(UUID kuId, List<UUID> documentsId) {
  dslContext.insertInto(KNOWLEDGE_UNIT_DOCUMENT)
    .columns(KNOWLEDGE_UNIT_DOCUMENT.KNOWLEDGE_UNIT_ID,
      KNOWLEDGE_UNIT_DOCUMENT.DOCUMENT_ID)
    .valuesOfRows(documentsId.stream()
      .map(docId -> DSL.row(kuId, docId))
      .toList())
    .execute();
 }

 public List<KnowledgeUnit> findAll() {
  return dslContext.select()
    .from(KNOWLEDGE_UNIT)
    .fetchInto(KnowledgeUnit.class);
 }
}