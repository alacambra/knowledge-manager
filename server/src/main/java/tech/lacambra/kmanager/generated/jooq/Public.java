/*
 * This file is generated by jOOQ.
 */
package tech.lacambra.kmanager.generated.jooq;


import java.util.Arrays;
import java.util.List;

import org.jooq.Catalog;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.SchemaImpl;

import tech.lacambra.kmanager.generated.jooq.tables.Document;
import tech.lacambra.kmanager.generated.jooq.tables.DocumentGroup;
import tech.lacambra.kmanager.generated.jooq.tables.DocumentGroupDocument;
import tech.lacambra.kmanager.generated.jooq.tables.DocumentTemplates;
import tech.lacambra.kmanager.generated.jooq.tables.KnowledgeUnit;
import tech.lacambra.kmanager.generated.jooq.tables.KnowledgeUnitContainedResource;
import tech.lacambra.kmanager.generated.jooq.tables.KnowledgeUnitResource;
import tech.lacambra.kmanager.generated.jooq.tables.ResourceContainedDocumentGroup;


/**
 * standard public schema
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Public extends SchemaImpl {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public</code>
     */
    public static final Public PUBLIC = new Public();

    /**
     * No further instances allowed
     */
    private Public() {
        super(DSL.name("public"), null, DSL.comment("standard public schema"));
    }


    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Table<?>> getTables() {
        return Arrays.asList(
            Document.DOCUMENT,
            DocumentGroup.DOCUMENT_GROUP,
            DocumentGroupDocument.DOCUMENT_GROUP_DOCUMENT,
            DocumentTemplates.DOCUMENT_TEMPLATES,
            KnowledgeUnit.KNOWLEDGE_UNIT,
            KnowledgeUnitContainedResource.KNOWLEDGE_UNIT_CONTAINED_RESOURCE,
            KnowledgeUnitResource.KNOWLEDGE_UNIT_RESOURCE,
            ResourceContainedDocumentGroup.RESOURCE_CONTAINED_DOCUMENT_GROUP
        );
    }
}
