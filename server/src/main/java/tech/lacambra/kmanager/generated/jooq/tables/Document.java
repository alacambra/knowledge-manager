/*
 * This file is generated by jOOQ.
 */
package tech.lacambra.kmanager.generated.jooq.tables;


import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.PlainSQL;
import org.jooq.QueryPart;
import org.jooq.SQL;
import org.jooq.Schema;
import org.jooq.Select;
import org.jooq.Stringly;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import tech.lacambra.kmanager.generated.jooq.Public;
import tech.lacambra.kmanager.generated.jooq.tables.records.DocumentRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Document extends TableImpl<DocumentRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.document</code>
     */
    public static final Document DOCUMENT = new Document();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<DocumentRecord> getRecordType() {
        return DocumentRecord.class;
    }

    /**
     * The column <code>public.document.id</code>.
     */
    public final TableField<DocumentRecord, UUID> ID = createField(DSL.name("id"), SQLDataType.UUID.nullable(false).defaultValue(DSL.field(DSL.raw("uuid_generate_v4()"), SQLDataType.UUID)), this, "");

    /**
     * The column <code>public.document.title</code>.
     */
    public final TableField<DocumentRecord, String> TITLE = createField(DSL.name("title"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.document.file_name</code>.
     */
    public final TableField<DocumentRecord, String> FILE_NAME = createField(DSL.name("file_name"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.document.uri</code>.
     */
    public final TableField<DocumentRecord, String> URI = createField(DSL.name("uri"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.document.content</code>.
     */
    public final TableField<DocumentRecord, String> CONTENT = createField(DSL.name("content"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * @deprecated Unknown data type. If this is a qualified, user-defined type,
     * it may have been excluded from code generation. If this is a built-in
     * type, you can define an explicit {@link org.jooq.Binding} to specify how
     * this type should be handled. Deprecation can be turned off using
     * {@literal <deprecationOnUnknownTypes/>} in your code generator
     * configuration.
     */
    @Deprecated
    public final TableField<DocumentRecord, Object> EMBEDDING = createField(DSL.name("embedding"), DefaultDataType.getDefaultDataType("\"public\".\"vector\""), this, "");

    /**
     * The column <code>public.document.created_at</code>.
     */
    public final TableField<DocumentRecord, LocalDateTime> CREATED_AT = createField(DSL.name("created_at"), SQLDataType.LOCALDATETIME(6).nullable(false).defaultValue(DSL.field(DSL.raw("CURRENT_TIMESTAMP"), SQLDataType.LOCALDATETIME)), this, "");

    /**
     * The column <code>public.document.updated_at</code>.
     */
    public final TableField<DocumentRecord, LocalDateTime> UPDATED_AT = createField(DSL.name("updated_at"), SQLDataType.LOCALDATETIME(6).nullable(false).defaultValue(DSL.field(DSL.raw("CURRENT_TIMESTAMP"), SQLDataType.LOCALDATETIME)), this, "");

    private Document(Name alias, Table<DocumentRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private Document(Name alias, Table<DocumentRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>public.document</code> table reference
     */
    public Document(String alias) {
        this(DSL.name(alias), DOCUMENT);
    }

    /**
     * Create an aliased <code>public.document</code> table reference
     */
    public Document(Name alias) {
        this(alias, DOCUMENT);
    }

    /**
     * Create a <code>public.document</code> table reference
     */
    public Document() {
        this(DSL.name("document"), null);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public Document as(String alias) {
        return new Document(DSL.name(alias), this);
    }

    @Override
    public Document as(Name alias) {
        return new Document(alias, this);
    }

    @Override
    public Document as(Table<?> alias) {
        return new Document(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public Document rename(String name) {
        return new Document(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Document rename(Name name) {
        return new Document(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public Document rename(Table<?> name) {
        return new Document(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Document where(Condition condition) {
        return new Document(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Document where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Document where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Document where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Document where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Document where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Document where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Document where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Document whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Document whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
