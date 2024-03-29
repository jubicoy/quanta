/*
 * This file is generated by jOOQ.
 */
package fi.jubic.quanta.db.tables.records;


import fi.jubic.quanta.db.tables.WorkerDefinitionColumn;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record10;
import org.jooq.Row10;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class WorkerDefinitionColumnRecord extends UpdatableRecordImpl<WorkerDefinitionColumnRecord> implements Record10<Long, String, String, String, Boolean, String, Integer, String, Long, String> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>worker_definition_column.id</code>.
     */
    public void setId(Long value) {
        set(0, value);
    }

    /**
     * Getter for <code>worker_definition_column.id</code>.
     */
    public Long getId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>worker_definition_column.name</code>.
     */
    public void setName(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>worker_definition_column.name</code>.
     */
    public String getName() {
        return (String) get(1);
    }

    /**
     * Setter for <code>worker_definition_column.class</code>.
     */
    public void setClass_(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>worker_definition_column.class</code>.
     */
    public String getClass_() {
        return (String) get(2);
    }

    /**
     * Setter for <code>worker_definition_column.format</code>.
     */
    public void setFormat(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>worker_definition_column.format</code>.
     */
    public String getFormat() {
        return (String) get(3);
    }

    /**
     * Setter for <code>worker_definition_column.nullable</code>.
     */
    public void setNullable(Boolean value) {
        set(4, value);
    }

    /**
     * Getter for <code>worker_definition_column.nullable</code>.
     */
    public Boolean getNullable() {
        return (Boolean) get(4);
    }

    /**
     * Setter for <code>worker_definition_column.column_type</code>.
     */
    public void setColumnType(String value) {
        set(5, value);
    }

    /**
     * Getter for <code>worker_definition_column.column_type</code>.
     */
    public String getColumnType() {
        return (String) get(5);
    }

    /**
     * Setter for <code>worker_definition_column.index</code>.
     */
    public void setIndex(Integer value) {
        set(6, value);
    }

    /**
     * Getter for <code>worker_definition_column.index</code>.
     */
    public Integer getIndex() {
        return (Integer) get(6);
    }

    /**
     * Setter for <code>worker_definition_column.description</code>.
     */
    public void setDescription(String value) {
        set(7, value);
    }

    /**
     * Getter for <code>worker_definition_column.description</code>.
     */
    public String getDescription() {
        return (String) get(7);
    }

    /**
     * Setter for <code>worker_definition_column.definition_id</code>.
     */
    public void setDefinitionId(Long value) {
        set(8, value);
    }

    /**
     * Getter for <code>worker_definition_column.definition_id</code>.
     */
    public Long getDefinitionId() {
        return (Long) get(8);
    }

    /**
     * Setter for <code>worker_definition_column.series_key</code>.
     */
    public void setSeriesKey(String value) {
        set(9, value);
    }

    /**
     * Getter for <code>worker_definition_column.series_key</code>.
     */
    public String getSeriesKey() {
        return (String) get(9);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record10 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row10<Long, String, String, String, Boolean, String, Integer, String, Long, String> fieldsRow() {
        return (Row10) super.fieldsRow();
    }

    @Override
    public Row10<Long, String, String, String, Boolean, String, Integer, String, Long, String> valuesRow() {
        return (Row10) super.valuesRow();
    }

    @Override
    public Field<Long> field1() {
        return WorkerDefinitionColumn.WORKER_DEFINITION_COLUMN.ID;
    }

    @Override
    public Field<String> field2() {
        return WorkerDefinitionColumn.WORKER_DEFINITION_COLUMN.NAME;
    }

    @Override
    public Field<String> field3() {
        return WorkerDefinitionColumn.WORKER_DEFINITION_COLUMN.CLASS;
    }

    @Override
    public Field<String> field4() {
        return WorkerDefinitionColumn.WORKER_DEFINITION_COLUMN.FORMAT;
    }

    @Override
    public Field<Boolean> field5() {
        return WorkerDefinitionColumn.WORKER_DEFINITION_COLUMN.NULLABLE;
    }

    @Override
    public Field<String> field6() {
        return WorkerDefinitionColumn.WORKER_DEFINITION_COLUMN.COLUMN_TYPE;
    }

    @Override
    public Field<Integer> field7() {
        return WorkerDefinitionColumn.WORKER_DEFINITION_COLUMN.INDEX;
    }

    @Override
    public Field<String> field8() {
        return WorkerDefinitionColumn.WORKER_DEFINITION_COLUMN.DESCRIPTION;
    }

    @Override
    public Field<Long> field9() {
        return WorkerDefinitionColumn.WORKER_DEFINITION_COLUMN.DEFINITION_ID;
    }

    @Override
    public Field<String> field10() {
        return WorkerDefinitionColumn.WORKER_DEFINITION_COLUMN.SERIES_KEY;
    }

    @Override
    public Long component1() {
        return getId();
    }

    @Override
    public String component2() {
        return getName();
    }

    @Override
    public String component3() {
        return getClass_();
    }

    @Override
    public String component4() {
        return getFormat();
    }

    @Override
    public Boolean component5() {
        return getNullable();
    }

    @Override
    public String component6() {
        return getColumnType();
    }

    @Override
    public Integer component7() {
        return getIndex();
    }

    @Override
    public String component8() {
        return getDescription();
    }

    @Override
    public Long component9() {
        return getDefinitionId();
    }

    @Override
    public String component10() {
        return getSeriesKey();
    }

    @Override
    public Long value1() {
        return getId();
    }

    @Override
    public String value2() {
        return getName();
    }

    @Override
    public String value3() {
        return getClass_();
    }

    @Override
    public String value4() {
        return getFormat();
    }

    @Override
    public Boolean value5() {
        return getNullable();
    }

    @Override
    public String value6() {
        return getColumnType();
    }

    @Override
    public Integer value7() {
        return getIndex();
    }

    @Override
    public String value8() {
        return getDescription();
    }

    @Override
    public Long value9() {
        return getDefinitionId();
    }

    @Override
    public String value10() {
        return getSeriesKey();
    }

    @Override
    public WorkerDefinitionColumnRecord value1(Long value) {
        setId(value);
        return this;
    }

    @Override
    public WorkerDefinitionColumnRecord value2(String value) {
        setName(value);
        return this;
    }

    @Override
    public WorkerDefinitionColumnRecord value3(String value) {
        setClass_(value);
        return this;
    }

    @Override
    public WorkerDefinitionColumnRecord value4(String value) {
        setFormat(value);
        return this;
    }

    @Override
    public WorkerDefinitionColumnRecord value5(Boolean value) {
        setNullable(value);
        return this;
    }

    @Override
    public WorkerDefinitionColumnRecord value6(String value) {
        setColumnType(value);
        return this;
    }

    @Override
    public WorkerDefinitionColumnRecord value7(Integer value) {
        setIndex(value);
        return this;
    }

    @Override
    public WorkerDefinitionColumnRecord value8(String value) {
        setDescription(value);
        return this;
    }

    @Override
    public WorkerDefinitionColumnRecord value9(Long value) {
        setDefinitionId(value);
        return this;
    }

    @Override
    public WorkerDefinitionColumnRecord value10(String value) {
        setSeriesKey(value);
        return this;
    }

    @Override
    public WorkerDefinitionColumnRecord values(Long value1, String value2, String value3, String value4, Boolean value5, String value6, Integer value7, String value8, Long value9, String value10) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        value9(value9);
        value10(value10);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached WorkerDefinitionColumnRecord
     */
    public WorkerDefinitionColumnRecord() {
        super(WorkerDefinitionColumn.WORKER_DEFINITION_COLUMN);
    }

    /**
     * Create a detached, initialised WorkerDefinitionColumnRecord
     */
    public WorkerDefinitionColumnRecord(Long id, String name, String class_, String format, Boolean nullable, String columnType, Integer index, String description, Long definitionId, String seriesKey) {
        super(WorkerDefinitionColumn.WORKER_DEFINITION_COLUMN);

        setId(id);
        setName(name);
        setClass_(class_);
        setFormat(format);
        setNullable(nullable);
        setColumnType(columnType);
        setIndex(index);
        setDescription(description);
        setDefinitionId(definitionId);
        setSeriesKey(seriesKey);
    }
}
