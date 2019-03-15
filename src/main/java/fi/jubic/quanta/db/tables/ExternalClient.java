/*
 * This file is generated by jOOQ.
 */
package fi.jubic.quanta.db.tables;


import fi.jubic.quanta.db.DefaultSchema;
import fi.jubic.quanta.db.Indexes;
import fi.jubic.quanta.db.Keys;
import fi.jubic.quanta.db.tables.records.ExternalClientRecord;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.11"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ExternalClient extends TableImpl<ExternalClientRecord> {

    private static final long serialVersionUID = 1325719332;

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
    public final TableField<ExternalClientRecord, Long> ID = createField("id", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>external_client.name</code>.
     */
    public final TableField<ExternalClientRecord, String> NAME = createField("name", org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>external_client.token</code>.
     */
    public final TableField<ExternalClientRecord, String> TOKEN = createField("token", org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>external_client.task_id</code>.
     */
    public final TableField<ExternalClientRecord, Long> TASK_ID = createField("task_id", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>external_client.deleted_at</code>.
     */
    public final TableField<ExternalClientRecord, Timestamp> DELETED_AT = createField("deleted_at", org.jooq.impl.SQLDataType.TIMESTAMP, this, "");

    /**
     * Create a <code>external_client</code> table reference
     */
    public ExternalClient() {
        this(DSL.name("external_client"), null);
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

    private ExternalClient(Name alias, Table<ExternalClientRecord> aliased) {
        this(alias, aliased, null);
    }

    private ExternalClient(Name alias, Table<ExternalClientRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> ExternalClient(Table<O> child, ForeignKey<O, ExternalClientRecord> key) {
        super(child, key, EXTERNAL_CLIENT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return DefaultSchema.DEFAULT_SCHEMA;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.EXTERNAL_CLIENT_PKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<ExternalClientRecord> getPrimaryKey() {
        return Keys.EXTERNAL_CLIENT_PKEY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<ExternalClientRecord>> getKeys() {
        return Arrays.<UniqueKey<ExternalClientRecord>>asList(Keys.EXTERNAL_CLIENT_PKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<ExternalClientRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<ExternalClientRecord, ?>>asList(Keys.EXTERNAL_CLIENT__FK_EXTERNAL_CLIENT_TASK_ID);
    }

    public Task task() {
        return new Task(this, Keys.EXTERNAL_CLIENT__FK_EXTERNAL_CLIENT_TASK_ID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExternalClient as(String alias) {
        return new ExternalClient(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
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
}
