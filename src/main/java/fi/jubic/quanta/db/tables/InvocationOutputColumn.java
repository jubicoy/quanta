/*
 * This file is generated by jOOQ.
 */
package fi.jubic.quanta.db.tables;


import fi.jubic.quanta.db.DefaultSchema;
import fi.jubic.quanta.db.Keys;
import fi.jubic.quanta.db.tables.records.InvocationOutputColumnRecord;

import java.util.Arrays;
import java.util.List;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row8;
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
public class InvocationOutputColumn extends TableImpl<InvocationOutputColumnRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>invocation_output_column</code>
     */
    public static final InvocationOutputColumn INVOCATION_OUTPUT_COLUMN = new InvocationOutputColumn();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<InvocationOutputColumnRecord> getRecordType() {
        return InvocationOutputColumnRecord.class;
    }

    /**
     * The column <code>invocation_output_column.id</code>.
     */
    public final TableField<InvocationOutputColumnRecord, Long> ID = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false).identity(true), this, "");

    /**
     * The column <code>invocation_output_column.index</code>.
     */
    public final TableField<InvocationOutputColumnRecord, Integer> INDEX = createField(DSL.name("index"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>invocation_output_column.alias</code>.
     */
    public final TableField<InvocationOutputColumnRecord, String> ALIAS = createField(DSL.name("alias"), SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>invocation_output_column.column_name</code>.
     */
    public final TableField<InvocationOutputColumnRecord, String> COLUMN_NAME = createField(DSL.name("column_name"), SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>invocation_output_column.class</code>.
     */
    public final TableField<InvocationOutputColumnRecord, String> CLASS = createField(DSL.name("class"), SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>invocation_output_column.format</code>.
     */
    public final TableField<InvocationOutputColumnRecord, String> FORMAT = createField(DSL.name("format"), SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>invocation_output_column.nullable</code>.
     */
    public final TableField<InvocationOutputColumnRecord, Boolean> NULLABLE = createField(DSL.name("nullable"), SQLDataType.BOOLEAN.nullable(false).defaultValue(DSL.field("false", SQLDataType.BOOLEAN)), this, "");

    /**
     * The column <code>invocation_output_column.invocation_id</code>.
     */
    public final TableField<InvocationOutputColumnRecord, Long> INVOCATION_ID = createField(DSL.name("invocation_id"), SQLDataType.BIGINT.nullable(false), this, "");

    private InvocationOutputColumn(Name alias, Table<InvocationOutputColumnRecord> aliased) {
        this(alias, aliased, null);
    }

    private InvocationOutputColumn(Name alias, Table<InvocationOutputColumnRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>invocation_output_column</code> table reference
     */
    public InvocationOutputColumn(String alias) {
        this(DSL.name(alias), INVOCATION_OUTPUT_COLUMN);
    }

    /**
     * Create an aliased <code>invocation_output_column</code> table reference
     */
    public InvocationOutputColumn(Name alias) {
        this(alias, INVOCATION_OUTPUT_COLUMN);
    }

    /**
     * Create a <code>invocation_output_column</code> table reference
     */
    public InvocationOutputColumn() {
        this(DSL.name("invocation_output_column"), null);
    }

    public <O extends Record> InvocationOutputColumn(Table<O> child, ForeignKey<O, InvocationOutputColumnRecord> key) {
        super(child, key, INVOCATION_OUTPUT_COLUMN);
    }

    @Override
    public Schema getSchema() {
        return DefaultSchema.DEFAULT_SCHEMA;
    }

    @Override
    public Identity<InvocationOutputColumnRecord, Long> getIdentity() {
        return (Identity<InvocationOutputColumnRecord, Long>) super.getIdentity();
    }

    @Override
    public UniqueKey<InvocationOutputColumnRecord> getPrimaryKey() {
        return Keys.INVOCATION_OUTPUT_COLUMN_PKEY;
    }

    @Override
    public List<UniqueKey<InvocationOutputColumnRecord>> getKeys() {
        return Arrays.<UniqueKey<InvocationOutputColumnRecord>>asList(Keys.INVOCATION_OUTPUT_COLUMN_PKEY);
    }

    @Override
    public List<ForeignKey<InvocationOutputColumnRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<InvocationOutputColumnRecord, ?>>asList(Keys.INVOCATION_OUTPUT_COLUMN__FK_INVOCATION_OUTPUT_COLUMN_INVOCATION_ID);
    }

    public Invocation invocation() {
        return new Invocation(this, Keys.INVOCATION_OUTPUT_COLUMN__FK_INVOCATION_OUTPUT_COLUMN_INVOCATION_ID);
    }

    @Override
    public InvocationOutputColumn as(String alias) {
        return new InvocationOutputColumn(DSL.name(alias), this);
    }

    @Override
    public InvocationOutputColumn as(Name alias) {
        return new InvocationOutputColumn(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public InvocationOutputColumn rename(String name) {
        return new InvocationOutputColumn(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public InvocationOutputColumn rename(Name name) {
        return new InvocationOutputColumn(name, null);
    }

    // -------------------------------------------------------------------------
    // Row8 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row8<Long, Integer, String, String, String, String, Boolean, Long> fieldsRow() {
        return (Row8) super.fieldsRow();
    }
}
