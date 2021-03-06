/*
 * This file is generated by jOOQ.
 */
package fi.jubic.quanta.db.tables.records;


import fi.jubic.quanta.db.tables.SeriesTable;

import java.sql.Timestamp;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record4;
import org.jooq.Row4;
import org.jooq.impl.UpdatableRecordImpl;


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
public class SeriesTableRecord extends UpdatableRecordImpl<SeriesTableRecord> implements Record4<Long, String, Long, Timestamp> {

    private static final long serialVersionUID = 1095366966;

    /**
     * Setter for <code>series_table.id</code>.
     */
    public void setId(Long value) {
        set(0, value);
    }

    /**
     * Getter for <code>series_table.id</code>.
     */
    public Long getId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>series_table.table_name</code>.
     */
    public void setTableName(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>series_table.table_name</code>.
     */
    public String getTableName() {
        return (String) get(1);
    }

    /**
     * Setter for <code>series_table.data_series_id</code>.
     */
    public void setDataSeriesId(Long value) {
        set(2, value);
    }

    /**
     * Getter for <code>series_table.data_series_id</code>.
     */
    public Long getDataSeriesId() {
        return (Long) get(2);
    }

    /**
     * Setter for <code>series_table.delete_at</code>.
     */
    public void setDeleteAt(Timestamp value) {
        set(3, value);
    }

    /**
     * Getter for <code>series_table.delete_at</code>.
     */
    public Timestamp getDeleteAt() {
        return (Timestamp) get(3);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record4 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row4<Long, String, Long, Timestamp> fieldsRow() {
        return (Row4) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row4<Long, String, Long, Timestamp> valuesRow() {
        return (Row4) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field1() {
        return SeriesTable.SERIES_TABLE.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field2() {
        return SeriesTable.SERIES_TABLE.TABLE_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field3() {
        return SeriesTable.SERIES_TABLE.DATA_SERIES_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field4() {
        return SeriesTable.SERIES_TABLE.DELETE_AT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component2() {
        return getTableName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component3() {
        return getDataSeriesId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp component4() {
        return getDeleteAt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value2() {
        return getTableName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value3() {
        return getDataSeriesId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp value4() {
        return getDeleteAt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SeriesTableRecord value1(Long value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SeriesTableRecord value2(String value) {
        setTableName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SeriesTableRecord value3(Long value) {
        setDataSeriesId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SeriesTableRecord value4(Timestamp value) {
        setDeleteAt(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SeriesTableRecord values(Long value1, String value2, Long value3, Timestamp value4) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached SeriesTableRecord
     */
    public SeriesTableRecord() {
        super(SeriesTable.SERIES_TABLE);
    }

    /**
     * Create a detached, initialised SeriesTableRecord
     */
    public SeriesTableRecord(Long id, String tableName, Long dataSeriesId, Timestamp deleteAt) {
        super(SeriesTable.SERIES_TABLE);

        set(0, id);
        set(1, tableName);
        set(2, dataSeriesId);
        set(3, deleteAt);
    }
}
