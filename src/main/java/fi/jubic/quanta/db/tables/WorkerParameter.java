/*
 * This file is generated by jOOQ.
 */
package fi.jubic.quanta.db.tables;


import fi.jubic.quanta.db.DefaultSchema;
import fi.jubic.quanta.db.Indexes;
import fi.jubic.quanta.db.Keys;
import fi.jubic.quanta.db.tables.records.WorkerParameterRecord;

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
public class WorkerParameter extends TableImpl<WorkerParameterRecord> {

    private static final long serialVersionUID = -668153759;

    /**
     * The reference instance of <code>worker_parameter</code>
     */
    public static final WorkerParameter WORKER_PARAMETER = new WorkerParameter();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<WorkerParameterRecord> getRecordType() {
        return WorkerParameterRecord.class;
    }

    /**
     * The column <code>worker_parameter.id</code>.
     */
    public final TableField<WorkerParameterRecord, Long> ID = createField("id", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>worker_parameter.name</code>.
     */
    public final TableField<WorkerParameterRecord, String> NAME = createField("name", org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>worker_parameter.description</code>.
     */
    public final TableField<WorkerParameterRecord, String> DESCRIPTION = createField("description", org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>worker_parameter.type</code>.
     */
    public final TableField<WorkerParameterRecord, String> TYPE = createField("type", org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>worker_parameter.default_value</code>.
     */
    public final TableField<WorkerParameterRecord, String> DEFAULT_VALUE = createField("default_value", org.jooq.impl.SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>worker_parameter.nullable</code>.
     */
    public final TableField<WorkerParameterRecord, Boolean> NULLABLE = createField("nullable", org.jooq.impl.SQLDataType.BOOLEAN.nullable(false).defaultValue(org.jooq.impl.DSL.field("false", org.jooq.impl.SQLDataType.BOOLEAN)), this, "");

    /**
     * The column <code>worker_parameter.worker_definition_id</code>.
     */
    public final TableField<WorkerParameterRecord, Long> WORKER_DEFINITION_ID = createField("worker_definition_id", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * Create a <code>worker_parameter</code> table reference
     */
    public WorkerParameter() {
        this(DSL.name("worker_parameter"), null);
    }

    /**
     * Create an aliased <code>worker_parameter</code> table reference
     */
    public WorkerParameter(String alias) {
        this(DSL.name(alias), WORKER_PARAMETER);
    }

    /**
     * Create an aliased <code>worker_parameter</code> table reference
     */
    public WorkerParameter(Name alias) {
        this(alias, WORKER_PARAMETER);
    }

    private WorkerParameter(Name alias, Table<WorkerParameterRecord> aliased) {
        this(alias, aliased, null);
    }

    private WorkerParameter(Name alias, Table<WorkerParameterRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> WorkerParameter(Table<O> child, ForeignKey<O, WorkerParameterRecord> key) {
        super(child, key, WORKER_PARAMETER);
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
        return Arrays.<Index>asList(Indexes.WORKER_PARAMETER_PKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<WorkerParameterRecord> getPrimaryKey() {
        return Keys.WORKER_PARAMETER_PKEY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<WorkerParameterRecord>> getKeys() {
        return Arrays.<UniqueKey<WorkerParameterRecord>>asList(Keys.WORKER_PARAMETER_PKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<WorkerParameterRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<WorkerParameterRecord, ?>>asList(Keys.WORKER_PARAMETER__FK_WORKER_PARAMETER_WORKER_DEFINITION_ID);
    }

    public WorkerDefinition workerDefinition() {
        return new WorkerDefinition(this, Keys.WORKER_PARAMETER__FK_WORKER_PARAMETER_WORKER_DEFINITION_ID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkerParameter as(String alias) {
        return new WorkerParameter(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkerParameter as(Name alias) {
        return new WorkerParameter(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public WorkerParameter rename(String name) {
        return new WorkerParameter(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public WorkerParameter rename(Name name) {
        return new WorkerParameter(name, null);
    }
}
