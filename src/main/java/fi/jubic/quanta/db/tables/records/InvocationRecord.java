/*
 * This file is generated by jOOQ.
 */
package fi.jubic.quanta.db.tables.records;


import fi.jubic.quanta.db.tables.Invocation;

import java.sql.Timestamp;

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
public class InvocationRecord extends UpdatableRecordImpl<InvocationRecord> implements Record8<Long, Long, String, Long, Long, Timestamp, Timestamp, Timestamp> {

    private static final long serialVersionUID = 862484404;

    /**
     * Setter for <code>invocation.id</code>.
     */
    public void setId(Long value) {
        set(0, value);
    }

    /**
     * Getter for <code>invocation.id</code>.
     */
    public Long getId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>invocation.invocation_number</code>.
     */
    public void setInvocationNumber(Long value) {
        set(1, value);
    }

    /**
     * Getter for <code>invocation.invocation_number</code>.
     */
    public Long getInvocationNumber() {
        return (Long) get(1);
    }

    /**
     * Setter for <code>invocation.status</code>.
     */
    public void setStatus(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>invocation.status</code>.
     */
    public String getStatus() {
        return (String) get(2);
    }

    /**
     * Setter for <code>invocation.task_id</code>.
     */
    public void setTaskId(Long value) {
        set(3, value);
    }

    /**
     * Getter for <code>invocation.task_id</code>.
     */
    public Long getTaskId() {
        return (Long) get(3);
    }

    /**
     * Setter for <code>invocation.worker_id</code>.
     */
    public void setWorkerId(Long value) {
        set(4, value);
    }

    /**
     * Getter for <code>invocation.worker_id</code>.
     */
    public Long getWorkerId() {
        return (Long) get(4);
    }

    /**
     * Setter for <code>invocation.starting_time</code>.
     */
    public void setStartingTime(Timestamp value) {
        set(5, value);
    }

    /**
     * Getter for <code>invocation.starting_time</code>.
     */
    public Timestamp getStartingTime() {
        return (Timestamp) get(5);
    }

    /**
     * Setter for <code>invocation.ending_time</code>.
     */
    public void setEndingTime(Timestamp value) {
        set(6, value);
    }

    /**
     * Getter for <code>invocation.ending_time</code>.
     */
    public Timestamp getEndingTime() {
        return (Timestamp) get(6);
    }

    /**
     * Setter for <code>invocation.deleted_at</code>.
     */
    public void setDeletedAt(Timestamp value) {
        set(7, value);
    }

    /**
     * Getter for <code>invocation.deleted_at</code>.
     */
    public Timestamp getDeletedAt() {
        return (Timestamp) get(7);
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
    public Row8<Long, Long, String, Long, Long, Timestamp, Timestamp, Timestamp> fieldsRow() {
        return (Row8) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row8<Long, Long, String, Long, Long, Timestamp, Timestamp, Timestamp> valuesRow() {
        return (Row8) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field1() {
        return Invocation.INVOCATION.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field2() {
        return Invocation.INVOCATION.INVOCATION_NUMBER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field3() {
        return Invocation.INVOCATION.STATUS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field4() {
        return Invocation.INVOCATION.TASK_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field5() {
        return Invocation.INVOCATION.WORKER_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field6() {
        return Invocation.INVOCATION.STARTING_TIME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field7() {
        return Invocation.INVOCATION.ENDING_TIME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field8() {
        return Invocation.INVOCATION.DELETED_AT;
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
    public Long component2() {
        return getInvocationNumber();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component3() {
        return getStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component4() {
        return getTaskId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component5() {
        return getWorkerId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp component6() {
        return getStartingTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp component7() {
        return getEndingTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp component8() {
        return getDeletedAt();
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
    public Long value2() {
        return getInvocationNumber();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value3() {
        return getStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value4() {
        return getTaskId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value5() {
        return getWorkerId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp value6() {
        return getStartingTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp value7() {
        return getEndingTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp value8() {
        return getDeletedAt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvocationRecord value1(Long value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvocationRecord value2(Long value) {
        setInvocationNumber(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvocationRecord value3(String value) {
        setStatus(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvocationRecord value4(Long value) {
        setTaskId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvocationRecord value5(Long value) {
        setWorkerId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvocationRecord value6(Timestamp value) {
        setStartingTime(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvocationRecord value7(Timestamp value) {
        setEndingTime(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvocationRecord value8(Timestamp value) {
        setDeletedAt(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvocationRecord values(Long value1, Long value2, String value3, Long value4, Long value5, Timestamp value6, Timestamp value7, Timestamp value8) {
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
     * Create a detached InvocationRecord
     */
    public InvocationRecord() {
        super(Invocation.INVOCATION);
    }

    /**
     * Create a detached, initialised InvocationRecord
     */
    public InvocationRecord(Long id, Long invocationNumber, String status, Long taskId, Long workerId, Timestamp startingTime, Timestamp endingTime, Timestamp deletedAt) {
        super(Invocation.INVOCATION);

        set(0, id);
        set(1, invocationNumber);
        set(2, status);
        set(3, taskId);
        set(4, workerId);
        set(5, startingTime);
        set(6, endingTime);
        set(7, deletedAt);
    }
}
