/*
 * This file is generated by jOOQ.
 */
package fi.jubic.quanta.db.tables;


import fi.jubic.quanta.db.DefaultSchema;
import fi.jubic.quanta.db.Keys;
import fi.jubic.quanta.db.tables.records.ExternalClientRecord;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row7;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ExternalClient extends TableImpl<ExternalClientRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>external_client</code>
     */
    public static final ExternalClient EXTERNAL_CLIENT = new ExternalClient();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<ExternalClientRecord> getRecordType() {
        return ExternalClientRecord.class;
    }

    /**
     * The column <code>external_client.id</code>.
     */
    public final TableField<ExternalClientRecord, Long> ID = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false).identity(true), this, "");

    /**
     * The column <code>external_client.name</code>.
     */
    public final TableField<ExternalClientRecord, String> NAME = createField(DSL.name("name"), SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>external_client.token</code>.
     */
    public final TableField<ExternalClientRecord, String> TOKEN = createField(DSL.name("token"), SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>external_client.task_id</code>.
     */
    public final TableField<ExternalClientRecord, Long> TASK_ID = createField(DSL.name("task_id"), SQLDataType.BIGINT, this, "");

    /**
     * The column <code>external_client.deleted_at</code>.
     */
    public final TableField<ExternalClientRecord, LocalDateTime> DELETED_AT = createField(DSL.name("deleted_at"), SQLDataType.LOCALDATETIME(6), this, "");

    /**
     * The column <code>external_client.description</code>.
     */
    public final TableField<ExternalClientRecord, String> DESCRIPTION = createField(DSL.name("description"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>external_client.user_id</code>.
     */
    public final TableField<ExternalClientRecord, Long> USER_ID = createField(DSL.name("user_id"), SQLDataType.BIGINT.nullable(false), this, "");

    private ExternalClient(Name alias, Table<ExternalClientRecord> aliased) {
        this(alias, aliased, null);
    }

    private ExternalClient(Name alias, Table<ExternalClientRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>external_client</code> table reference
     */
    public ExternalClient(String alias) {
        this(DSL.name(alias), EXTERNAL_CLIENT);
    }

    /**
     * Create an aliased <code>external_client</code> table reference
     */
    public ExternalClient(Name alias) {
        this(alias, EXTERNAL_CLIENT);
    }

    /**
     * Create a <code>external_client</code> table reference
     */
    public ExternalClient() {
        this(DSL.name("external_client"), null);
    }

    public <O extends Record> ExternalClient(Table<O> child, ForeignKey<O, ExternalClientRecord> key) {
        super(child, key, EXTERNAL_CLIENT);
    }

    @Override
    public Schema getSchema() {
        return DefaultSchema.DEFAULT_SCHEMA;
    }

    @Override
    public Identity<ExternalClientRecord, Long> getIdentity() {
        return (Identity<ExternalClientRecord, Long>) super.getIdentity();
    }

    @Override
    public UniqueKey<ExternalClientRecord> getPrimaryKey() {
        return Keys.EXTERNAL_CLIENT_PKEY;
    }

    @Override
    public List<UniqueKey<ExternalClientRecord>> getKeys() {
        return Arrays.<UniqueKey<ExternalClientRecord>>asList(Keys.EXTERNAL_CLIENT_PKEY);
    }

    @Override
    public List<ForeignKey<ExternalClientRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<ExternalClientRecord, ?>>asList(Keys.EXTERNAL_CLIENT__FK_EXTERNAL_CLIENT_TASK_ID, Keys.EXTERNAL_CLIENT__FK_EXTERNAL_CLIENT_USER_ID);
    }

    public Task task() {
        return new Task(this, Keys.EXTERNAL_CLIENT__FK_EXTERNAL_CLIENT_TASK_ID);
    }

    public User user() {
        return new User(this, Keys.EXTERNAL_CLIENT__FK_EXTERNAL_CLIENT_USER_ID);
    }

    @Override
    public ExternalClient as(String alias) {
        return new ExternalClient(DSL.name(alias), this);
    }

    @Override
    public ExternalClient as(Name alias) {
        return new ExternalClient(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public ExternalClient rename(String name) {
        return new ExternalClient(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public ExternalClient rename(Name name) {
        return new ExternalClient(name, null);
    }

    // -------------------------------------------------------------------------
    // Row7 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row7<Long, String, String, Long, LocalDateTime, String, Long> fieldsRow() {
        return (Row7) super.fieldsRow();
    }
}
