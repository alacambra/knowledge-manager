/*
 * This file is generated by jOOQ.
 */
package tech.lacambra.kmanager.generated.jooq.tables.records;


import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

import org.jooq.impl.TableRecordImpl;

import tech.lacambra.kmanager.generated.jooq.tables.KnowledgeUnit;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class KnowledgeUnitRecord extends TableRecordImpl<KnowledgeUnitRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.knowledge_unit.id</code>.
     */
    public void setId(UUID value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.knowledge_unit.id</code>.
     */
    public UUID getId() {
        return (UUID) get(0);
    }

    /**
     * Setter for <code>public.knowledge_unit.name</code>.
     */
    public void setName(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.knowledge_unit.name</code>.
     */
    @NotNull
    public String getName() {
        return (String) get(1);
    }

    /**
     * Setter for <code>public.knowledge_unit.description</code>.
     */
    public void setDescription(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.knowledge_unit.description</code>.
     */
    public String getDescription() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.knowledge_unit.created_at</code>.
     */
    public void setCreatedAt(LocalDateTime value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.knowledge_unit.created_at</code>.
     */
    public LocalDateTime getCreatedAt() {
        return (LocalDateTime) get(3);
    }

    /**
     * Setter for <code>public.knowledge_unit.updated_at</code>.
     */
    public void setUpdatedAt(LocalDateTime value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.knowledge_unit.updated_at</code>.
     */
    public LocalDateTime getUpdatedAt() {
        return (LocalDateTime) get(4);
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached KnowledgeUnitRecord
     */
    public KnowledgeUnitRecord() {
        super(KnowledgeUnit.KNOWLEDGE_UNIT);
    }

    /**
     * Create a detached, initialised KnowledgeUnitRecord
     */
    public KnowledgeUnitRecord(UUID id, String name, String description, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(KnowledgeUnit.KNOWLEDGE_UNIT);

        setId(id);
        setName(name);
        setDescription(description);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        resetTouchedOnNotNull();
    }

    /**
     * Create a detached, initialised KnowledgeUnitRecord
     */
    public KnowledgeUnitRecord(tech.lacambra.kmanager.generated.jooq.tables.pojos.KnowledgeUnit value) {
        super(KnowledgeUnit.KNOWLEDGE_UNIT);

        if (value != null) {
            setId(value.getId());
            setName(value.getName());
            setDescription(value.getDescription());
            setCreatedAt(value.getCreatedAt());
            setUpdatedAt(value.getUpdatedAt());
            resetTouchedOnNotNull();
        }
    }
}
