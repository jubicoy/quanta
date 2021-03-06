/*
 * This file is generated by jOOQ.
 */
package fi.jubic.quanta.db.tables.records;


import fi.jubic.quanta.db.tables.InvocationColumnSelector;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record11;
import org.jooq.Row11;
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
public class InvocationColumnSelectorRecord extends UpdatableRecordImpl<InvocationColumnSelectorRecord> implements Record11<Long, Integer, String, String, String, Boolean, String, String, Long, Long, Long> {

    private static final long serialVersionUID = 1682269050;

    /**
     * Setter for <code>invocation_column_selector.id</code>.
     */
    public void setId(Long value) {
        set(0, value);
    }

    /**
     * Getter for <code>invocation_column_selector.id</code>.
     */
    public Long getId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>invocation_column_selector.column_index</code>.
     */
    public void setColumnIndex(Integer value) {
        set(1, value);
    }

    /**
     * Getter for <code>invocation_column_selector.column_index</code>.
     */
    public Integer getColumnIndex() {
        return (Integer) get(1);
    }

    /**
     * Setter for <code>invocation_column_selector.column_name</code>.
     */
    public void setColumnName(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>invocation_column_selector.column_name</code>.
     */
    public String getColumnName() {
        return (String) get(2);
    }

    /**
     * Setter for <code>invocation_column_selector.class</code>.
     */
    public void setClass_(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>invocation_column_selector.class</code>.
     */
    public String getClass_() {
        return (String) get(3);
    }

    /**
     * Setter for <code>invocation_column_selector.format</code>.
     */
    public void setFormat(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>invocation_column_selector.format</code>.
     */
    public String getFormat() {
        return (String) get(4);
    }

    /**
     * Setter for <code>invocation_column_selector.nullable</code>.
     */
    public void setNullable(Boolean value) {
        set(5, value);
    }

    /**
     * Getter for <code>invocation_column_selector.nullable</code>.
     */
    public Boolean getNullable() {
        return (Boolean) get(5);
    }

    /**
     * Setter for <code>invocation_column_selector.modifier</code>.
     */
    public void setModifier(String value) {
        set(6, value);
    }

    /**
     * Getter for <code>invocation_column_selector.modifier</code>.
     */
    public String getModifier() {
        return (String) get(6);
    }

    /**
     * Setter for <code>invocation_column_selector.alias</code>.
     */
    public void setAlias(String value) {
        set(7, value);
    }

    /**
     * Getter for <code>invocation_column_selector.alias</code>.
     */
    public String getAlias() {
        return (String) get(7);
    }

    /**
     * Setter for <code>invocation_column_selector.data_series_id</code>.
     */
    public void setDataSeriesId(Long value) {
        set(8, value);
    }

    /**
     * Getter for <code>invocation_column_selector.data_series_id</code>.
     */
    public Long getDataSeriesId() {
        return (Long) get(8);
    }

    /**
     * Setter for <code>invocation_column_selector.invocation_id</code>.
     */
    public void setInvocationId(Long value) {
        set(9, value);
    }

    /**
     * Getter for <code>invocation_column_selector.invocation_id</code>.
     */
    public Long getInvocationId() {
        return (Long) get(9);
    }

    /**
     * Setter for <code>invocation_column_selector.worker_definition_column_id</code>.
     */
    public void setWorkerDefinitionColumnId(Long value) {
        set(10, value);
    }

    /**
     * Getter for <code>invocation_column_selector.worker_definition_column_id</code>.
     */
    public Long getWorkerDefinitionColumnId() {
        return (Long) get(10);
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
    // Record11 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row11<Long, Integer, String, String, String, Boolean, String, String, Long, Long, Long> fieldsRow() {
        return (Row11) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row11<Long, Integer, String, String, String, Boolean, String, String, Long, Long, Long> valuesRow() {
        return (Row11) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field1() {
        return InvocationColumnSelector.INVOCATION_COLUMN_SELECTOR.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field2() {
        return InvocationColumnSelector.INVOCATION_COLUMN_SELECTOR.COLUMN_INDEX;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field3() {
        return InvocationColumnSelector.INVOCATION_COLUMN_SELECTOR.COLUMN_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field4() {
        return InvocationColumnSelector.INVOCATION_COLUMN_SELECTOR.CLASS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field5() {
        return InvocationColumnSelector.INVOCATION_COLUMN_SELECTOR.FORMAT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Boolean> field6() {
        return InvocationColumnSelector.INVOCATION_COLUMN_SELECTOR.NULLABLE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field7() {
        return InvocationColumnSelector.INVOCATION_COLUMN_SELECTOR.MODIFIER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field8() {
        return InvocationColumnSelector.INVOCATION_COLUMN_SELECTOR.ALIAS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field9() {
        return InvocationColumnSelector.INVOCATION_COLUMN_SELECTOR.DATA_SERIES_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field10() {
        return InvocationColumnSelector.INVOCATION_COLUMN_SELECTOR.INVOCATION_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field11() {
        return InvocationColumnSelector.INVOCATION_COLUMN_SELECTOR.WORKER_DEFINITION_COLUMN_ID;
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
    public Integer component2() {
        return getColumnIndex();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component3() {
        return getColumnName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component4() {
        return getClass_();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component5() {
        return getFormat();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean component6() {
        return getNullable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component7() {
        return getModifier();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component8() {
        return getAlias();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component9() {
        return getDataSeriesId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component10() {
        return getInvocationId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component11() {
        return getWorkerDefinitionColumnId();
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
    public Integer value2() {
        return getColumnIndex();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value3() {
        return getColumnName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value4() {
        return getClass_();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value5() {
        return getFormat();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean value6() {
        return getNullable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value7() {
        return getModifier();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value8() {
        return getAlias();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value9() {
        return getDataSeriesId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value10() {
        return getInvocationId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value11() {
        return getWorkerDefinitionColumnId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvocationColumnSelectorRecord value1(Long value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvocationColumnSelectorRecord value2(Integer value) {
        setColumnIndex(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvocationColumnSelectorRecord value3(String value) {
        setColumnName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvocationColumnSelectorRecord value4(String value) {
        setClass_(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvocationColumnSelectorRecord value5(String value) {
        setFormat(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvocationColumnSelectorRecord value6(Boolean value) {
        setNullable(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvocationColumnSelectorRecord value7(String value) {
        setModifier(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvocationColumnSelectorRecord value8(String value) {
        setAlias(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvocationColumnSelectorRecord value9(Long value) {
        setDataSeriesId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvocationColumnSelectorRecord value10(Long value) {
        setInvocationId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvocationColumnSelectorRecord value11(Long value) {
        setWorkerDefinitionColumnId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvocationColumnSelectorRecord values(Long value1, Integer value2, String value3, String value4, String value5, Boolean value6, String value7, String value8, Long value9, Long value10, Long value11) {
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
        value11(value11);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached InvocationColumnSelectorRecord
     */
    public InvocationColumnSelectorRecord() {
        super(InvocationColumnSelector.INVOCATION_COLUMN_SELECTOR);
    }

    /**
     * Create a detached, initialised InvocationColumnSelectorRecord
     */
    public InvocationColumnSelectorRecord(Long id, Integer columnIndex, String columnName, String class_, String format, Boolean nullable, String modifier, String alias, Long dataSeriesId, Long invocationId, Long workerDefinitionColumnId) {
        super(InvocationColumnSelector.INVOCATION_COLUMN_SELECTOR);

        set(0, id);
        set(1, columnIndex);
        set(2, columnName);
        set(3, class_);
        set(4, format);
        set(5, nullable);
        set(6, modifier);
        set(7, alias);
        set(8, dataSeriesId);
        set(9, invocationId);
        set(10, workerDefinitionColumnId);
    }
}
