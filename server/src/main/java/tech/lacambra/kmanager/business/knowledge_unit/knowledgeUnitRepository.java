package tech.lacambra.kmanager.business.knowledge_unit;

import static tech.lacambra.kmanager.generated.jooq.tables.KnowledgeUnit.*;
import static tech.lacambra.kmanager.generated.jooq.tables.KnowledgeUnitDocument.*;
import static tech.lacambra.kmanager.generated.jooq.tables.Document.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import tech.lacambra.kmanager.generated.jooq.tables.pojos.KnowledgeUnit;
import tech.lacambra.kmanager.generated.jooq.tables.pojos.KnowledgeUnitDocument;
import tech.lacambra.kmanager.generated.jooq.tables.pojos.Document;
import tech.lacambra.kmanager.resource.knowlege_manager.KnowledgeUnitWithDocumentsResponse;

import java.util.Optional;

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

 public void removeDocumentsFromKU(UUID kuId, List<UUID> documentsId) {
  dslContext.deleteFrom(KNOWLEDGE_UNIT_DOCUMENT)
    .where(KNOWLEDGE_UNIT_DOCUMENT.KNOWLEDGE_UNIT_ID.eq(kuId))
    .and(KNOWLEDGE_UNIT_DOCUMENT.DOCUMENT_ID.in(documentsId))
    .execute();
 }

 public void updateKnowledgeUnit(UUID kuId, String name, String description) {
  dslContext.update(KNOWLEDGE_UNIT)
    .set(KNOWLEDGE_UNIT.NAME, name)
    .set(KNOWLEDGE_UNIT.DESCRIPTION, description)
    .where(KNOWLEDGE_UNIT.ID.eq(kuId))
    .execute();
 }

 public Optional<KnowledgeUnitWithDocumentsResponse> findByIdWithDocuments(UUID kuId) {
  KnowledgeUnit ku = dslContext.select()
    .from(KNOWLEDGE_UNIT)
    .where(KNOWLEDGE_UNIT.ID.eq(kuId))
    .fetchOneInto(KnowledgeUnit.class);
  
  if (ku == null) {
   return Optional.empty();
  }
  
  List<Document> documents = dslContext.select(DOCUMENT.fields())
    .from(DOCUMENT)
    .join(KNOWLEDGE_UNIT_DOCUMENT).on(DOCUMENT.ID.eq(KNOWLEDGE_UNIT_DOCUMENT.DOCUMENT_ID))
    .where(KNOWLEDGE_UNIT_DOCUMENT.KNOWLEDGE_UNIT_ID.eq(kuId))
    .fetchInto(Document.class);
  
  return Optional.of(new KnowledgeUnitWithDocumentsResponse(ku, documents));
 }

 public Optional<KnowledgeUnitWithDocumentsResponse> findByIdWithDocumentsOrdered(UUID kuId) {
  KnowledgeUnit ku = dslContext.select()
    .from(KNOWLEDGE_UNIT)
    .where(KNOWLEDGE_UNIT.ID.eq(kuId))
    .fetchOneInto(KnowledgeUnit.class);
  
  if (ku == null) {
   return Optional.empty();
  }
  
  List<Document> documents = dslContext.select(DOCUMENT.fields())
    .from(DOCUMENT)
    .join(KNOWLEDGE_UNIT_DOCUMENT).on(DOCUMENT.ID.eq(KNOWLEDGE_UNIT_DOCUMENT.DOCUMENT_ID))
    .where(KNOWLEDGE_UNIT_DOCUMENT.KNOWLEDGE_UNIT_ID.eq(kuId))
    .orderBy(DOCUMENT.CREATED_AT.asc())
    .fetchInto(Document.class);
  
  return Optional.of(new KnowledgeUnitWithDocumentsResponse(ku, documents));
 }

 public List<KnowledgeUnit> findAll() {
  return dslContext.select()
    .from(KNOWLEDGE_UNIT)
    .fetchInto(KnowledgeUnit.class);
 }

 public void deleteKnowledgeUnit(UUID kuId) {
  dslContext.deleteFrom(KNOWLEDGE_UNIT_DOCUMENT)
    .where(KNOWLEDGE_UNIT_DOCUMENT.KNOWLEDGE_UNIT_ID.eq(kuId))
    .execute();
  
  dslContext.deleteFrom(KNOWLEDGE_UNIT)
    .where(KNOWLEDGE_UNIT.ID.eq(kuId))
    .execute();
 }
}