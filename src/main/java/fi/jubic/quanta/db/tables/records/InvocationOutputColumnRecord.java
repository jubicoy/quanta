/*
 * This file is generated by jOOQ.
 */
package fi.jubic.quanta.db.tables.records;


import fi.jubic.quanta.db.tables.InvocationOutputColumn;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record8;
import org.jooq.Row8;
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
public class InvocationOutputColumnRecord extends UpdatableRecordImpl<InvocationOutputColumnRecord> implements Record8<Long, Integer, String, String, String, String, Boolean, Long> {

    private static final long serialVersionUID = -303896497;

    /**
     * Setter for <code>invocation_output_column.id</code>.
     */
    public void setId(Long value) {
        set(0, value);
    }

    /**
     * Getter for <code>invocation_output_column.id</code>.
     */
    public Long getId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>invocation_output_column.index</code>.
     */
    public void setIndex(Integer value) {
        set(1, value);
    }

    /**
     * Getter for <code>invocation_output_column.index</code>.
     */
    public Integer getIndex() {
        return (Integer) get(1);
    }

    /**
     * Setter for <code>invocation_output_column.alias</code>.
     */
    public void setAlias(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>invocation_output_column.alias</code>.
     */
    public String getAlias() {
        return (String) get(2);
    }

    /**
     * Setter for <code>invocation_output_column.column_name</code>.
     */
    public void setColumnName(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>invocation_output_column.column_name</code>.
     */
    public String getColumnName() {
        return (String) get(3);
    }

    /**
     * Setter for <code>invocation_output_column.class</code>.
     */
    public void setClass_(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>invocation_output_column.class</code>.
     */
    public String getClass_() {
        return (String) get(4);
    }

    /**
     * Setter for <code>invocation_output_column.format</code>.
     */
    public void setFormat(String value) {
        set(5, value);
    }

    /**
     * Getter for <code>invocation_output_column.format</code>.
     */
    public String getFormat() {
        return (String) get(5);
    }

    /**
     * Setter for <code>invocation_output_column.nullable</code>.
     */
    public void setNullable(Boolean value) {
        set(6, value);
    }

    /**
     * Getter for <code>invocation_output_column.nullable</code>.
     */
    public Boolean getNullable() {
        return (Boolean) get(6);
    }

    /**
     * Setter for <code>invocation_output_column.invocation_id</code>.
     */
    public void setInvocationId(Long value) {
        set(7, value);
    }

    /**
     * Getter for <code>invocation_output_column.invocation_id</code>.
     */
    public Long getInvocationId() {
        return (Long) get(7);
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
    // Record8 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row8<Long, Integer, String, String, String, String, Boolean, Long> fieldsRow() {
        return (Row8) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row8<Long, Integer, String, String, String, String, Boolean, Long> valuesRow() {
        return (Row8) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field1() {
        return InvocationOutputColumn.INVOCATION_OUTPUT_COLUMN.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field2() {
        return InvocationOutputColumn.INVOCATION_OUTPUT_COLUMN.INDEX;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field3() {
        return InvocationOutputColumn.INVOCATION_OUTPUT_COLUMN.ALIAS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field4() {
        return InvocationOutputColumn.INVOCATION_OUTPUT_COLUMN.COLUMN_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field5() {
        return InvocationOutputColumn.INVOCATION_OUTPUT_COLUMN.CLASS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field6() {
        return InvocationOutputColumn.INVOCATION_OUTPUT_COLUMN.FORMAT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Boolean> field7() {
        return InvocationOutputColumn.INVOCATION_OUTPUT_COLUMN.NULLABLE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field8() {
        return InvocationOutputColumn.INVOCATION_OUTPUT_COLUMN.INVOCATION_ID;
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
        return getIndex();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component3() {
        return getAlias();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component4() {
        return getColumnName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component5() {
        return getClass_();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component6() {
        return getFormat();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean component7() {
        return getNullable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component8() {
        return getInvocationId();
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
        return getIndex();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value3() {
        return getAlias();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value4() {
        return getColumnName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value5() {
        return getClass_();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value6() {
        return getFormat();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean value7() {
        return getNullable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value8() {
        return getInvocationId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvocationOutputColumnRecord value1(Long value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvocationOutputColumnRecord value2(Integer value) {
        setIndex(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvocationOutputColumnRecord value3(String value) {
        setAlias(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvocationOutputColumnRecord value4(String value) {
        setColumnName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvocationOutputColumnRecord value5(String value) {
        setClass_(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvocationOutputColumnRecord value6(String value) {
        setFormat(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvocationOutputColumnRecord value7(Boolean value) {
        setNullable(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvocationOutputColumnRecord value8(Long value) {
        setInvocationId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvocationOutputColumnRecord values(Long value1, Integer value2, String value3, String value4, String value5, String value6, Boolean value7, Long value8) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached InvocationOutputColumnRecord
     */
    public InvocationOutputColumnRecord() {
        super(InvocationOutputColumn.INVOCATION_OUTPUT_COLUMN);
    }

    /**
     * Create a detached, initialised InvocationOutputColumnRecord
     */
    public InvocationOutputColumnRecord(Long id, Integer index, String alias, String columnName, String class_, String format, Boolean nullable, Long invocationId) {
        super(InvocationOutputColumn.INVOCATION_OUTPUT_COLUMN);

        set(0, id);
        set(1, index);
        set(2, alias);
        set(3, columnName);
        set(4, class_);
        set(5, format);
        set(6, nullable);
        set(7, invocationId);
    }
}
