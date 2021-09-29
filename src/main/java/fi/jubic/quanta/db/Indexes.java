/*
 * This file is generated by jOOQ.
 */
package fi.jubic.quanta.db;


import fi.jubic.quanta.db.tables.Task;

import org.jooq.Index;
import org.jooq.OrderField;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;


/**
 * A class modelling indexes of tables in the default schema.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Indexes {

    // -------------------------------------------------------------------------
    // INDEX definitions
    // -------------------------------------------------------------------------

    public static final Index TASK_NAME_KEY = Internal.createIndex(DSL.name("task_name_key"), Task.TASK, new OrderField[] { Task.TASK.NAME }, true);
}
