/*
 * This file is generated by jOOQ.
 */
package fi.jubic.quanta.db.tables;


import fi.jubic.quanta.db.DefaultSchema;
import fi.jubic.quanta.db.Keys;
import fi.jubic.quanta.db.tables.records.DataSeriesRecord;

import java.time.LocalDateTime;
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
public class DataSeries extends TableImpl<DataSeriesRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>data_series</code>
     */
    public static final DataSeries DATA_SERIES = new DataSeries();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<DataSeriesRecord> getRecordType() {
        return DataSeriesRecord.class;
    }

    /**
     * The column <code>data_series.id</code>.
     */
    public final TableField<DataSeriesRecord, Long> ID = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false).identity(true), this, "");

    /**
     * The column <code>data_series.name</code>.
     */
    public final TableField<DataSeriesRecord, String> NAME = createField(DSL.name("name"), SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>data_series.description</code>.
     */
    public final TableField<DataSeriesRecord, String> DESCRIPTION = createField(DSL.name("description"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>data_series.table_name</code>.
     */
    public final TableField<DataSeriesRecord, String> TABLE_NAME = createField(DSL.name("table_name"), SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>data_series.type</code>.
     */
    public final TableField<DataSeriesRecord, String> TYPE = createField(DSL.name("type"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>data_series.configuration</code>.
     */
    public final TableField<DataSeriesRecord, String> CONFIGURATION = createField(DSL.name("configuration"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>data_series.deleted_at</code>.
     */
    public final TableField<DataSeriesRecord, LocalDateTime> DELETED_AT = createField(DSL.name("deleted_at"), SQLDataType.LOCALDATETIME(6), this, "");

    /**
     * The column <code>data_series.data_connection_id</code>.
     */
    public final TableField<DataSeriesRecord, Long> DATA_CONNECTION_ID = createField(DSL.name("data_connection_id"), SQLDataType.BIGINT.nullable(false), this, "");

    private DataSeries(Name alias, Table<DataSeriesRecord> aliased) {
        this(alias, aliased, null);
    }

    private DataSeries(Name alias, Table<DataSeriesRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>data_series</code> table reference
     */
    public DataSeries(String alias) {
        this(DSL.name(alias), DATA_SERIES);
    }

    /**
     * Create an aliased <code>data_series</code> table reference
     */
    public DataSeries(Name alias) {
        this(alias, DATA_SERIES);
    }

    /**
     * Create a <code>data_series</code> table reference
     */
    public DataSeries() {
        this(DSL.name("data_series"), null);
    }

    public <O extends Record> DataSeries(Table<O> child, ForeignKey<O, DataSeriesRecord> key) {
        super(child, key, DATA_SERIES);
    }

    @Override
    public Schema getSchema() {
        return DefaultSchema.DEFAULT_SCHEMA;
    }

    @Override
    public Identity<DataSeriesRecord, Long> getIdentity() {
        return (Identity<DataSeriesRecord, Long>) super.getIdentity();
    }

    @Override
    public UniqueKey<DataSeriesRecord> getPrimaryKey() {
        return Keys.DATA_SERIES_PKEY;
    }

    @Override
    public List<UniqueKey<DataSeriesRecord>> getKeys() {
        return Arrays.<UniqueKey<DataSeriesRecord>>asList(Keys.DATA_SERIES_PKEY, Keys.DATA_SERIES_NAME_KEY, Keys.DATA_SERIES_TABLE_NAME_KEY);
    }

    @Override
    public List<ForeignKey<DataSeriesRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<DataSeriesRecord, ?>>asList(Keys.DATA_SERIES__FK_DATA_SERIES_DATA_CONNECTION_ID);
    }

    public DataConnection dataConnection() {
        return new DataConnection(this, Keys.DATA_SERIES__FK_DATA_SERIES_DATA_CONNECTION_ID);
    }

    @Override
    public DataSeries as(String alias) {
        return new DataSeries(DSL.name(alias), this);
    }

    @Override
    public DataSeries as(Name alias) {
        return new DataSeries(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public DataSeries rename(String name) {
        return new DataSeries(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public DataSeries rename(Name name) {
        return new DataSeries(name, null);
    }

    // -------------------------------------------------------------------------
    // Row8 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row8<Long, String, String, String, String, String, LocalDateTime, Long> fieldsRow() {
        return (Row8) super.fieldsRow();
    }
}
