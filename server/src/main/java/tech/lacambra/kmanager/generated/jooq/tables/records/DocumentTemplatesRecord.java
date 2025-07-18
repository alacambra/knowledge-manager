/*
 * This file is generated by jOOQ.
 */
package tech.lacambra.kmanager.generated.jooq.tables.records;


import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

import org.jooq.JSONB;
import org.jooq.impl.TableRecordImpl;

import tech.lacambra.kmanager.generated.jooq.tables.DocumentTemplates;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class DocumentTemplatesRecord extends TableRecordImpl<DocumentTemplatesRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.document_templates.id</code>.
     */
    public void setId(UUID value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.document_templates.id</code>.
     */
    public UUID getId() {
        return (UUID) get(0);
    }

    /**
     * Setter for <code>public.document_templates.name</code>.
     */
    public void setName(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.document_templates.name</code>.
     */
    @NotNull
    public String getName() {
        return (String) get(1);
    }

    /**
     * Setter for <code>public.document_templates.category</code>.
     */
    public void setCategory(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.document_templates.category</code>.
     */
    public String getCategory() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.document_templates.form_schema</code>.
     */
    public void setFormSchema(JSONB value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.document_templates.form_schema</code>.
     */
    @NotNull
    public JSONB getFormSchema() {
        return (JSONB) get(3);
    }

    /**
     * Setter for <code>public.document_templates.created_at</code>.
     */
    public void setCreatedAt(LocalDateTime value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.document_templates.created_at</code>.
     */
    public LocalDateTime getCreatedAt() {
        return (LocalDateTime) get(4);
    }

    /**
     * Setter for <code>public.document_templates.updated_at</code>.
     */
    public void setUpdatedAt(LocalDateTime value) {
        set(5, value);
    }

    /**
     * Getter for <code>public.document_templates.updated_at</code>.
     */
    public LocalDateTime getUpdatedAt() {
        return (LocalDateTime) get(5);
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached DocumentTemplatesRecord
     */
    public DocumentTemplatesRecord() {
        super(DocumentTemplates.DOCUMENT_TEMPLATES);
    }

    /**
     * Create a detached, initialised DocumentTemplatesRecord
     */
    public DocumentTemplatesRecord(UUID id, String name, String category, JSONB formSchema, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(DocumentTemplates.DOCUMENT_TEMPLATES);

        setId(id);
        setName(name);
        setCategory(category);
        setFormSchema(formSchema);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        resetTouchedOnNotNull();
    }

    /**
     * Create a detached, initialised DocumentTemplatesRecord
     */
    public DocumentTemplatesRecord(tech.lacambra.kmanager.generated.jooq.tables.pojos.DocumentTemplates value) {
        super(DocumentTemplates.DOCUMENT_TEMPLATES);

        if (value != null) {
            setId(value.getId());
            setName(value.getName());
            setCategory(value.getCategory());
            setFormSchema(value.getFormSchema());
            setCreatedAt(value.getCreatedAt());
            setUpdatedAt(value.getUpdatedAt());
            resetTouchedOnNotNull();
        }
    }
}
