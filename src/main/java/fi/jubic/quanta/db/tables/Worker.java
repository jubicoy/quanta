/*
 * This file is generated by jOOQ.
 */
package fi.jubic.quanta.db.tables;


import fi.jubic.quanta.db.DefaultSchema;
import fi.jubic.quanta.db.Keys;
import fi.jubic.quanta.db.tables.records.WorkerRecord;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row6;
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
public class Worker extends TableImpl<WorkerRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>worker</code>
     */
    public static final Worker WORKER = new Worker();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<WorkerRecord> getRecordType() {
        return WorkerRecord.class;
    }

    /**
     * The column <code>worker.id</code>.
     */
    public final TableField<WorkerRecord, Long> ID = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false).identity(true), this, "");

    /**
     * The column <code>worker.token</code>.
     */
    public final TableField<WorkerRecord, String> TOKEN = createField(DSL.name("token"), SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * The column <code>worker.accepted_on</code>.
     */
    public final TableField<WorkerRecord, LocalDateTime> ACCEPTED_ON = createField(DSL.name("accepted_on"), SQLDataType.LOCALDATETIME(6), this, "");

    /**
     * The column <code>worker.last_seen</code>.
     */
    public final TableField<WorkerRecord, LocalDateTime> LAST_SEEN = createField(DSL.name("last_seen"), SQLDataType.LOCALDATETIME(6), this, "");

    /**
     * The column <code>worker.deleted_at</code>.
     */
    public final TableField<WorkerRecord, LocalDateTime> DELETED_AT = createField(DSL.name("deleted_at"), SQLDataType.LOCALDATETIME(6), this, "");

    /**
     * The column <code>worker.definition_id</code>.
     */
    public final TableField<WorkerRecord, Long> DEFINITION_ID = createField(DSL.name("definition_id"), SQLDataType.BIGINT.nullable(false), this, "");

    private Worker(Name alias, Table<WorkerRecord> aliased) {
        this(alias, aliased, null);
    }

    private Worker(Name alias, Table<WorkerRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>worker</code> table reference
     */
    public Worker(String alias) {
        this(DSL.name(alias), WORKER);
    }

    /**
     * Create an aliased <code>worker</code> table reference
     */
    public Worker(Name alias) {
        this(alias, WORKER);
    }

    /**
     * Create a <code>worker</code> table reference
     */
    public Worker() {
        this(DSL.name("worker"), null);
    }

    public <O extends Record> Worker(Table<O> child, ForeignKey<O, WorkerRecord> key) {
        super(child, key, WORKER);
    }

    @Override
    public Schema getSchema() {
        return DefaultSchema.DEFAULT_SCHEMA;
    }

    @Override
    public Identity<WorkerRecord, Long> getIdentity() {
        return (Identity<WorkerRecord, Long>) super.getIdentity();
    }

    @Override
    public UniqueKey<WorkerRecord> getPrimaryKey() {
        return Keys.WORKER_PKEY;
    }

    @Override
    public List<UniqueKey<WorkerRecord>> getKeys() {
        return Arrays.<UniqueKey<WorkerRecord>>asList(Keys.WORKER_PKEY, Keys.WORKER_TOKEN_KEY);
    }

    @Override
    public List<ForeignKey<WorkerRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<WorkerRecord, ?>>asList(Keys.WORKER__FK_WORKER_WORKER_DEF_ID);
    }

    public WorkerDefinition workerDefinition() {
        return new WorkerDefinition(this, Keys.WORKER__FK_WORKER_WORKER_DEF_ID);
    }

    @Override
    public Worker as(String alias) {
        return new Worker(DSL.name(alias), this);
    }

    @Override
    public Worker as(Name alias) {
        return new Worker(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Worker rename(String name) {
        return new Worker(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Worker rename(Name name) {
        return new Worker(name, null);
    }

    // -------------------------------------------------------------------------
    // Row6 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row6<Long, String, LocalDateTime, LocalDateTime, LocalDateTime, Long> fieldsRow() {
        return (Row6) super.fieldsRow();
    }
}
