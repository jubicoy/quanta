/*
 * This file is generated by jOOQ.
 */
package fi.jubic.quanta.db.tables.records;


import fi.jubic.quanta.db.tables.WorkerParameter;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record6;
import org.jooq.Row6;
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
public class WorkerParameterRecord extends UpdatableRecordImpl<WorkerParameterRecord> implements Record6<Long, String, String, String, String, Long> {

    private static final long serialVersionUID = -228485801;

    /**
     * Setter for <code>worker_parameter.id</code>.
     */
    public void setId(Long value) {
        set(0, value);
    }

    /**
     * Getter for <code>worker_parameter.id</code>.
     */
    public Long getId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>worker_parameter.name</code>.
     */
    public void setName(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>worker_parameter.name</code>.
     */
    public String getName() {
        return (String) get(1);
    }

    /**
     * Setter for <code>worker_parameter.description</code>.
     */
    public void setDescription(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>worker_parameter.description</code>.
     */
    public String getDescription() {
        return (String) get(2);
    }

    /**
     * Setter for <code>worker_parameter.type</code>.
     */
    public void setType(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>worker_parameter.type</code>.
     */
    public String getType() {
        return (String) get(3);
    }

    /**
     * Setter for <code>worker_parameter.default_value</code>.
     */
    public void setDefaultValue(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>worker_parameter.default_value</code>.
     */
    public String getDefaultValue() {
        return (String) get(4);
    }

    /**
     * Setter for <code>worker_parameter.worker_definition_id</code>.
     */
    public void setWorkerDefinitionId(Long value) {
        set(5, value);
    }

    /**
     * Getter for <code>worker_parameter.worker_definition_id</code>.
     */
    public Long getWorkerDefinitionId() {
        return (Long) get(5);
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
    // Record6 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row6<Long, String, String, String, String, Long> fieldsRow() {
        return (Row6) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row6<Long, String, String, String, String, Long> valuesRow() {
        return (Row6) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field1() {
        return WorkerParameter.WORKER_PARAMETER.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field2() {
        return WorkerParameter.WORKER_PARAMETER.NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field3() {
        return WorkerParameter.WORKER_PARAMETER.DESCRIPTION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field4() {
        return WorkerParameter.WORKER_PARAMETER.TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field5() {
        return WorkerParameter.WORKER_PARAMETER.DEFAULT_VALUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field6() {
        return WorkerParameter.WORKER_PARAMETER.WORKER_DEFINITION_ID;
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
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component3() {
        return getDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component4() {
        return getType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component5() {
        return getDefaultValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component6() {
        return getWorkerDefinitionId();
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
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value3() {
        return getDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value4() {
        return getType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value5() {
        return getDefaultValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value6() {
        return getWorkerDefinitionId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkerParameterRecord value1(Long value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkerParameterRecord value2(String value) {
        setName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkerParameterRecord value3(String value) {
        setDescription(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkerParameterRecord value4(String value) {
        setType(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkerParameterRecord value5(String value) {
        setDefaultValue(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkerParameterRecord value6(Long value) {
        setWorkerDefinitionId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkerParameterRecord values(Long value1, String value2, String value3, String value4, String value5, Long value6) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached WorkerParameterRecord
     */
    public WorkerParameterRecord() {
        super(WorkerParameter.WORKER_PARAMETER);
    }

    /**
     * Create a detached, initialised WorkerParameterRecord
     */
    public WorkerParameterRecord(Long id, String name, String description, String type, String defaultValue, Long workerDefinitionId) {
        super(WorkerParameter.WORKER_PARAMETER);

        set(0, id);
        set(1, name);
        set(2, description);
        set(3, type);
        set(4, defaultValue);
        set(5, workerDefinitionId);
    }
}
