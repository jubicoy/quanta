/*
 * This file is generated by jOOQ.
 */
package fi.jubic.quanta.db;


import fi.jubic.quanta.db.tables.Anomaly;
import fi.jubic.quanta.db.tables.Column;
import fi.jubic.quanta.db.tables.DataConnection;
import fi.jubic.quanta.db.tables.DataSeries;
import fi.jubic.quanta.db.tables.ExternalClient;
import fi.jubic.quanta.db.tables.Invocation;
import fi.jubic.quanta.db.tables.InvocationColumnSelector;
import fi.jubic.quanta.db.tables.InvocationOutputColumn;
import fi.jubic.quanta.db.tables.InvocationParameter;
import fi.jubic.quanta.db.tables.SeriesResult;
import fi.jubic.quanta.db.tables.SeriesTable;
import fi.jubic.quanta.db.tables.Tag;
import fi.jubic.quanta.db.tables.TagDataconnection;
import fi.jubic.quanta.db.tables.TagTask;
import fi.jubic.quanta.db.tables.Task;
import fi.jubic.quanta.db.tables.TaskColumnSelector;
import fi.jubic.quanta.db.tables.TaskOutputColumn;
import fi.jubic.quanta.db.tables.TaskParameter;
import fi.jubic.quanta.db.tables.User;
import fi.jubic.quanta.db.tables.Worker;
import fi.jubic.quanta.db.tables.WorkerDefinition;
import fi.jubic.quanta.db.tables.WorkerDefinitionColumn;
import fi.jubic.quanta.db.tables.WorkerParameter;
import fi.jubic.quanta.db.tables.records.AnomalyRecord;
import fi.jubic.quanta.db.tables.records.ColumnRecord;
import fi.jubic.quanta.db.tables.records.DataConnectionRecord;
import fi.jubic.quanta.db.tables.records.DataSeriesRecord;
import fi.jubic.quanta.db.tables.records.ExternalClientRecord;
import fi.jubic.quanta.db.tables.records.InvocationColumnSelectorRecord;
import fi.jubic.quanta.db.tables.records.InvocationOutputColumnRecord;
import fi.jubic.quanta.db.tables.records.InvocationParameterRecord;
import fi.jubic.quanta.db.tables.records.InvocationRecord;
import fi.jubic.quanta.db.tables.records.SeriesResultRecord;
import fi.jubic.quanta.db.tables.records.SeriesTableRecord;
import fi.jubic.quanta.db.tables.records.TagDataconnectionRecord;
import fi.jubic.quanta.db.tables.records.TagRecord;
import fi.jubic.quanta.db.tables.records.TagTaskRecord;
import fi.jubic.quanta.db.tables.records.TaskColumnSelectorRecord;
import fi.jubic.quanta.db.tables.records.TaskOutputColumnRecord;
import fi.jubic.quanta.db.tables.records.TaskParameterRecord;
import fi.jubic.quanta.db.tables.records.TaskRecord;
import fi.jubic.quanta.db.tables.records.UserRecord;
import fi.jubic.quanta.db.tables.records.WorkerDefinitionColumnRecord;
import fi.jubic.quanta.db.tables.records.WorkerDefinitionRecord;
import fi.jubic.quanta.db.tables.records.WorkerParameterRecord;
import fi.jubic.quanta.db.tables.records.WorkerRecord;

import org.jooq.ForeignKey;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;


/**
 * A class modelling foreign key relationships and constraints of tables in 
 * the default schema.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<AnomalyRecord> DETECTION_RESULT_PKEY = Internal.createUniqueKey(Anomaly.ANOMALY, DSL.name("detection_result_pkey"), new TableField[] { Anomaly.ANOMALY.ID }, true);
    public static final UniqueKey<ColumnRecord> COLUMN_PKEY = Internal.createUniqueKey(Column.COLUMN, DSL.name("column_pkey"), new TableField[] { Column.COLUMN.ID }, true);
    public static final UniqueKey<DataConnectionRecord> DATA_CONNECTION_NAME_KEY = Internal.createUniqueKey(DataConnection.DATA_CONNECTION, DSL.name("data_connection_name_key"), new TableField[] { DataConnection.DATA_CONNECTION.NAME }, true);
    public static final UniqueKey<DataConnectionRecord> DATA_CONNECTION_PKEY = Internal.createUniqueKey(DataConnection.DATA_CONNECTION, DSL.name("data_connection_pkey"), new TableField[] { DataConnection.DATA_CONNECTION.ID }, true);
    public static final UniqueKey<DataSeriesRecord> DATA_SERIES_NAME_KEY = Internal.createUniqueKey(DataSeries.DATA_SERIES, DSL.name("data_series_name_key"), new TableField[] { DataSeries.DATA_SERIES.NAME }, true);
    public static final UniqueKey<DataSeriesRecord> DATA_SERIES_PKEY = Internal.createUniqueKey(DataSeries.DATA_SERIES, DSL.name("data_series_pkey"), new TableField[] { DataSeries.DATA_SERIES.ID }, true);
    public static final UniqueKey<DataSeriesRecord> DATA_SERIES_TABLE_NAME_KEY = Internal.createUniqueKey(DataSeries.DATA_SERIES, DSL.name("data_series_table_name_key"), new TableField[] { DataSeries.DATA_SERIES.TABLE_NAME }, true);
    public static final UniqueKey<ExternalClientRecord> EXTERNAL_CLIENT_PKEY = Internal.createUniqueKey(ExternalClient.EXTERNAL_CLIENT, DSL.name("external_client_pkey"), new TableField[] { ExternalClient.EXTERNAL_CLIENT.ID }, true);
    public static final UniqueKey<InvocationRecord> INVOCATION_PKEY = Internal.createUniqueKey(Invocation.INVOCATION, DSL.name("invocation_pkey"), new TableField[] { Invocation.INVOCATION.ID }, true);
    public static final UniqueKey<InvocationRecord> INVOCATION_TASK_ID_INVOCATION_NUMBER_KEY = Internal.createUniqueKey(Invocation.INVOCATION, DSL.name("invocation_task_id_invocation_number_key"), new TableField[] { Invocation.INVOCATION.TASK_ID, Invocation.INVOCATION.INVOCATION_NUMBER }, true);
    public static final UniqueKey<InvocationColumnSelectorRecord> INVOCATION_COLUMN_SELECTOR_PKEY = Internal.createUniqueKey(InvocationColumnSelector.INVOCATION_COLUMN_SELECTOR, DSL.name("invocation_column_selector_pkey"), new TableField[] { InvocationColumnSelector.INVOCATION_COLUMN_SELECTOR.ID }, true);
    public static final UniqueKey<InvocationOutputColumnRecord> INVOCATION_OUTPUT_COLUMN_PKEY = Internal.createUniqueKey(InvocationOutputColumn.INVOCATION_OUTPUT_COLUMN, DSL.name("invocation_output_column_pkey"), new TableField[] { InvocationOutputColumn.INVOCATION_OUTPUT_COLUMN.ID }, true);
    public static final UniqueKey<InvocationParameterRecord> INVOCATION_PARAMETER_PKEY = Internal.createUniqueKey(InvocationParameter.INVOCATION_PARAMETER, DSL.name("invocation_parameter_pkey"), new TableField[] { InvocationParameter.INVOCATION_PARAMETER.ID }, true);
    public static final UniqueKey<SeriesResultRecord> SERIES_RESULT_PKEY = Internal.createUniqueKey(SeriesResult.SERIES_RESULT, DSL.name("series_result_pkey"), new TableField[] { SeriesResult.SERIES_RESULT.ID }, true);
    public static final UniqueKey<SeriesTableRecord> SERIES_TABLE_PKEY = Internal.createUniqueKey(SeriesTable.SERIES_TABLE, DSL.name("series_table_pkey"), new TableField[] { SeriesTable.SERIES_TABLE.ID }, true);
<<<<<<< HEAD
=======
    public static final UniqueKey<TagRecord> TAG_PKEY = Internal.createUniqueKey(Tag.TAG, DSL.name("tag_pkey"), new TableField[] { Tag.TAG.ID }, true);
    public static final UniqueKey<TagDataconnectionRecord> PK_TAG_DATACONNECTION = Internal.createUniqueKey(TagDataconnection.TAG_DATACONNECTION, DSL.name("pk_tag_dataconnection"), new TableField[] { TagDataconnection.TAG_DATACONNECTION.TAG_ID, TagDataconnection.TAG_DATACONNECTION.DATACONNECTION_ID }, true);
    public static final UniqueKey<TagTaskRecord> PK_TAG_TASK = Internal.createUniqueKey(TagTask.TAG_TASK, DSL.name("pk_tag_task"), new TableField[] { TagTask.TAG_TASK.TAG_ID, TagTask.TAG_TASK.TASK_ID }, true);
>>>>>>> 944ec4b (add tables for tagging feature)
    public static final UniqueKey<TaskRecord> TASK_PKEY = Internal.createUniqueKey(Task.TASK, DSL.name("task_pkey"), new TableField[] { Task.TASK.ID }, true);
    public static final UniqueKey<TaskColumnSelectorRecord> TASK_COLUMN_SELECTOR_PKEY = Internal.createUniqueKey(TaskColumnSelector.TASK_COLUMN_SELECTOR, DSL.name("task_column_selector_pkey"), new TableField[] { TaskColumnSelector.TASK_COLUMN_SELECTOR.ID }, true);
    public static final UniqueKey<TaskOutputColumnRecord> TASK_OUTPUT_COLUMN_PKEY = Internal.createUniqueKey(TaskOutputColumn.TASK_OUTPUT_COLUMN, DSL.name("task_output_column_pkey"), new TableField[] { TaskOutputColumn.TASK_OUTPUT_COLUMN.ID }, true);
    public static final UniqueKey<TaskParameterRecord> TASK_PARAMETER_PKEY = Internal.createUniqueKey(TaskParameter.TASK_PARAMETER, DSL.name("task_parameter_pkey"), new TableField[] { TaskParameter.TASK_PARAMETER.ID }, true);
    public static final UniqueKey<UserRecord> USER_PKEY = Internal.createUniqueKey(User.USER, DSL.name("user_pkey"), new TableField[] { User.USER.ID }, true);
    public static final UniqueKey<WorkerRecord> WORKER_PKEY = Internal.createUniqueKey(Worker.WORKER, DSL.name("worker_pkey"), new TableField[] { Worker.WORKER.ID }, true);
    public static final UniqueKey<WorkerRecord> WORKER_TOKEN_KEY = Internal.createUniqueKey(Worker.WORKER, DSL.name("worker_token_key"), new TableField[] { Worker.WORKER.TOKEN }, true);
    public static final UniqueKey<WorkerDefinitionRecord> WORKER_DEFINITION_NAME_KEY = Internal.createUniqueKey(WorkerDefinition.WORKER_DEFINITION, DSL.name("worker_definition_name_key"), new TableField[] { WorkerDefinition.WORKER_DEFINITION.NAME }, true);
    public static final UniqueKey<WorkerDefinitionRecord> WORKER_DEFINITION_PKEY = Internal.createUniqueKey(WorkerDefinition.WORKER_DEFINITION, DSL.name("worker_definition_pkey"), new TableField[] { WorkerDefinition.WORKER_DEFINITION.ID }, true);
    public static final UniqueKey<WorkerDefinitionColumnRecord> WORKER_DEFINITION_COLUMN_PKEY = Internal.createUniqueKey(WorkerDefinitionColumn.WORKER_DEFINITION_COLUMN, DSL.name("worker_definition_column_pkey"), new TableField[] { WorkerDefinitionColumn.WORKER_DEFINITION_COLUMN.ID }, true);
    public static final UniqueKey<WorkerParameterRecord> WORKER_PARAMETER_PKEY = Internal.createUniqueKey(WorkerParameter.WORKER_PARAMETER, DSL.name("worker_parameter_pkey"), new TableField[] { WorkerParameter.WORKER_PARAMETER.ID }, true);

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------

    public static final ForeignKey<AnomalyRecord, InvocationRecord> ANOMALY__FK_RESULT_INVOCATION_ID = Internal.createForeignKey(Anomaly.ANOMALY, DSL.name("fk_result_invocation_id"), new TableField[] { Anomaly.ANOMALY.INVOCATION_ID }, Keys.INVOCATION_PKEY, new TableField[] { Invocation.INVOCATION.ID }, true);
    public static final ForeignKey<ColumnRecord, DataSeriesRecord> COLUMN__FK_COLUMN_DATA_SERIES_ID = Internal.createForeignKey(Column.COLUMN, DSL.name("fk_column_data_series_id"), new TableField[] { Column.COLUMN.DATA_SERIES_ID }, Keys.DATA_SERIES_PKEY, new TableField[] { DataSeries.DATA_SERIES.ID }, true);
    public static final ForeignKey<DataSeriesRecord, DataConnectionRecord> DATA_SERIES__FK_DATA_SERIES_DATA_CONNECTION_ID = Internal.createForeignKey(DataSeries.DATA_SERIES, DSL.name("fk_data_series_data_connection_id"), new TableField[] { DataSeries.DATA_SERIES.DATA_CONNECTION_ID }, Keys.DATA_CONNECTION_PKEY, new TableField[] { DataConnection.DATA_CONNECTION.ID }, true);
    public static final ForeignKey<ExternalClientRecord, TaskRecord> EXTERNAL_CLIENT__FK_EXTERNAL_CLIENT_TASK_ID = Internal.createForeignKey(ExternalClient.EXTERNAL_CLIENT, DSL.name("fk_external_client_task_id"), new TableField[] { ExternalClient.EXTERNAL_CLIENT.TASK_ID }, Keys.TASK_PKEY, new TableField[] { Task.TASK.ID }, true);
    public static final ForeignKey<ExternalClientRecord, UserRecord> EXTERNAL_CLIENT__FK_EXTERNAL_CLIENT_USER_ID = Internal.createForeignKey(ExternalClient.EXTERNAL_CLIENT, DSL.name("fk_external_client_user_id"), new TableField[] { ExternalClient.EXTERNAL_CLIENT.USER_ID }, Keys.USER_PKEY, new TableField[] { User.USER.ID }, true);
    public static final ForeignKey<InvocationRecord, TaskRecord> INVOCATION__FK_INVOCATION_TASK_ID = Internal.createForeignKey(Invocation.INVOCATION, DSL.name("fk_invocation_task_id"), new TableField[] { Invocation.INVOCATION.TASK_ID }, Keys.TASK_PKEY, new TableField[] { Task.TASK.ID }, true);
    public static final ForeignKey<InvocationRecord, WorkerRecord> INVOCATION__FK_INVOCATION_WORKER_ID = Internal.createForeignKey(Invocation.INVOCATION, DSL.name("fk_invocation_worker_id"), new TableField[] { Invocation.INVOCATION.WORKER_ID }, Keys.WORKER_PKEY, new TableField[] { Worker.WORKER.ID }, true);
    public static final ForeignKey<InvocationColumnSelectorRecord, DataSeriesRecord> INVOCATION_COLUMN_SELECTOR__FK_INVOCATION_COLUMN_SELECTOR_DATA_SERIES_ID = Internal.createForeignKey(InvocationColumnSelector.INVOCATION_COLUMN_SELECTOR, DSL.name("fk_invocation_column_selector_data_series_id"), new TableField[] { InvocationColumnSelector.INVOCATION_COLUMN_SELECTOR.DATA_SERIES_ID }, Keys.DATA_SERIES_PKEY, new TableField[] { DataSeries.DATA_SERIES.ID }, true);
    public static final ForeignKey<InvocationColumnSelectorRecord, InvocationRecord> INVOCATION_COLUMN_SELECTOR__FK_INVOCATION_COLUMN_SELECTOR_INVOCATION_ID = Internal.createForeignKey(InvocationColumnSelector.INVOCATION_COLUMN_SELECTOR, DSL.name("fk_invocation_column_selector_invocation_id"), new TableField[] { InvocationColumnSelector.INVOCATION_COLUMN_SELECTOR.INVOCATION_ID }, Keys.INVOCATION_PKEY, new TableField[] { Invocation.INVOCATION.ID }, true);
    public static final ForeignKey<InvocationColumnSelectorRecord, WorkerDefinitionColumnRecord> INVOCATION_COLUMN_SELECTOR__FK_INVOCATION_COLUMN_SELECTOR_WORKER_DEFINITION_COLUMN_ID = Internal.createForeignKey(InvocationColumnSelector.INVOCATION_COLUMN_SELECTOR, DSL.name("fk_invocation_column_selector_worker_definition_column_id"), new TableField[] { InvocationColumnSelector.INVOCATION_COLUMN_SELECTOR.WORKER_DEFINITION_COLUMN_ID }, Keys.WORKER_DEFINITION_COLUMN_PKEY, new TableField[] { WorkerDefinitionColumn.WORKER_DEFINITION_COLUMN.ID }, true);
    public static final ForeignKey<InvocationOutputColumnRecord, InvocationRecord> INVOCATION_OUTPUT_COLUMN__FK_INVOCATION_OUTPUT_COLUMN_INVOCATION_ID = Internal.createForeignKey(InvocationOutputColumn.INVOCATION_OUTPUT_COLUMN, DSL.name("fk_invocation_output_column_invocation_id"), new TableField[] { InvocationOutputColumn.INVOCATION_OUTPUT_COLUMN.INVOCATION_ID }, Keys.INVOCATION_PKEY, new TableField[] { Invocation.INVOCATION.ID }, true);
    public static final ForeignKey<InvocationParameterRecord, InvocationRecord> INVOCATION_PARAMETER__FK_INVOCATION_PARAMETER = Internal.createForeignKey(InvocationParameter.INVOCATION_PARAMETER, DSL.name("fk_invocation_parameter"), new TableField[] { InvocationParameter.INVOCATION_PARAMETER.INVOCATION_ID }, Keys.INVOCATION_PKEY, new TableField[] { Invocation.INVOCATION.ID }, true);
    public static final ForeignKey<SeriesResultRecord, InvocationRecord> SERIES_RESULT__FK_RESULT_INVOCATION_ID = Internal.createForeignKey(SeriesResult.SERIES_RESULT, DSL.name("fk_result_invocation_id"), new TableField[] { SeriesResult.SERIES_RESULT.INVOCATION_ID }, Keys.INVOCATION_PKEY, new TableField[] { Invocation.INVOCATION.ID }, true);
    public static final ForeignKey<SeriesTableRecord, DataSeriesRecord> SERIES_TABLE__FK_SERIES_TABLE_DATA_SERIES_ID = Internal.createForeignKey(SeriesTable.SERIES_TABLE, DSL.name("fk_series_table_data_series_id"), new TableField[] { SeriesTable.SERIES_TABLE.DATA_SERIES_ID }, Keys.DATA_SERIES_PKEY, new TableField[] { DataSeries.DATA_SERIES.ID }, true);
    public static final ForeignKey<TaskRecord, DataSeriesRecord> TASK__FK_TASK_DATA_SERIES_ID = Internal.createForeignKey(Task.TASK, DSL.name("fk_task_data_series_id"), new TableField[] { Task.TASK.DATA_SERIES_ID }, Keys.DATA_SERIES_PKEY, new TableField[] { DataSeries.DATA_SERIES.ID }, true);
    public static final ForeignKey<TaskRecord, WorkerDefinitionRecord> TASK__FK_TASK_WORKER_DEF_ID = Internal.createForeignKey(Task.TASK, DSL.name("fk_task_worker_def_id"), new TableField[] { Task.TASK.WORKER_DEF_ID }, Keys.WORKER_DEFINITION_PKEY, new TableField[] { WorkerDefinition.WORKER_DEFINITION.ID }, true);
    public static final ForeignKey<TaskColumnSelectorRecord, DataSeriesRecord> TASK_COLUMN_SELECTOR__FK_TASK_COLUMN_SELECTOR_DATA_SERIES_ID = Internal.createForeignKey(TaskColumnSelector.TASK_COLUMN_SELECTOR, DSL.name("fk_task_column_selector_data_series_id"), new TableField[] { TaskColumnSelector.TASK_COLUMN_SELECTOR.DATA_SERIES_ID }, Keys.DATA_SERIES_PKEY, new TableField[] { DataSeries.DATA_SERIES.ID }, true);
    public static final ForeignKey<TaskColumnSelectorRecord, TaskRecord> TASK_COLUMN_SELECTOR__FK_TASK_COLUMN_SELECTOR_TASK_ID = Internal.createForeignKey(TaskColumnSelector.TASK_COLUMN_SELECTOR, DSL.name("fk_task_column_selector_task_id"), new TableField[] { TaskColumnSelector.TASK_COLUMN_SELECTOR.TASK_ID }, Keys.TASK_PKEY, new TableField[] { Task.TASK.ID }, true);
    public static final ForeignKey<TaskColumnSelectorRecord, WorkerDefinitionColumnRecord> TASK_COLUMN_SELECTOR__FK_TASK_COLUMN_SELECTOR_WORKER_DEFINITION_COLUMN_ID = Internal.createForeignKey(TaskColumnSelector.TASK_COLUMN_SELECTOR, DSL.name("fk_task_column_selector_worker_definition_column_id"), new TableField[] { TaskColumnSelector.TASK_COLUMN_SELECTOR.WORKER_DEFINITION_COLUMN_ID }, Keys.WORKER_DEFINITION_COLUMN_PKEY, new TableField[] { WorkerDefinitionColumn.WORKER_DEFINITION_COLUMN.ID }, true);
    public static final ForeignKey<TaskOutputColumnRecord, TaskRecord> TASK_OUTPUT_COLUMN__FK_TASK_OUTPUT_COLUMN_TASK_ID = Internal.createForeignKey(TaskOutputColumn.TASK_OUTPUT_COLUMN, DSL.name("fk_task_output_column_task_id"), new TableField[] { TaskOutputColumn.TASK_OUTPUT_COLUMN.TASK_ID }, Keys.TASK_PKEY, new TableField[] { Task.TASK.ID }, true);
    public static final ForeignKey<TaskParameterRecord, TaskRecord> TASK_PARAMETER__FK_TASK_PARAMETER_TASK_ID = Internal.createForeignKey(TaskParameter.TASK_PARAMETER, DSL.name("fk_task_parameter_task_id"), new TableField[] { TaskParameter.TASK_PARAMETER.TASK_ID }, Keys.TASK_PKEY, new TableField[] { Task.TASK.ID }, true);
    public static final ForeignKey<WorkerRecord, WorkerDefinitionRecord> WORKER__FK_WORKER_WORKER_DEF_ID = Internal.createForeignKey(Worker.WORKER, DSL.name("fk_worker_worker_def_id"), new TableField[] { Worker.WORKER.DEFINITION_ID }, Keys.WORKER_DEFINITION_PKEY, new TableField[] { WorkerDefinition.WORKER_DEFINITION.ID }, true);
    public static final ForeignKey<WorkerDefinitionColumnRecord, WorkerDefinitionRecord> WORKER_DEFINITION_COLUMN__FK_WORKER_DEFINITION_INPUT_COLUMN_WORKER_DEFINITION_ID = Internal.createForeignKey(WorkerDefinitionColumn.WORKER_DEFINITION_COLUMN, DSL.name("fk_worker_definition_input_column_worker_definition_id"), new TableField[] { WorkerDefinitionColumn.WORKER_DEFINITION_COLUMN.DEFINITION_ID }, Keys.WORKER_DEFINITION_PKEY, new TableField[] { WorkerDefinition.WORKER_DEFINITION.ID }, true);
    public static final ForeignKey<WorkerParameterRecord, WorkerDefinitionRecord> WORKER_PARAMETER__FK_WORKER_PARAMETER_WORKER_DEFINITION_ID = Internal.createForeignKey(WorkerParameter.WORKER_PARAMETER, DSL.name("fk_worker_parameter_worker_definition_id"), new TableField[] { WorkerParameter.WORKER_PARAMETER.WORKER_DEFINITION_ID }, Keys.WORKER_DEFINITION_PKEY, new TableField[] { WorkerDefinition.WORKER_DEFINITION.ID }, true);
}
