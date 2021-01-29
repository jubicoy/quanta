/*
 * This file is generated by jOOQ.
 */
package fi.jubic.quanta.db;


import fi.jubic.quanta.db.tables.Column;
import fi.jubic.quanta.db.tables.DataConnection;
import fi.jubic.quanta.db.tables.DataSeries;
import fi.jubic.quanta.db.tables.DetectionResult;
import fi.jubic.quanta.db.tables.ExternalClient;
import fi.jubic.quanta.db.tables.Invocation;
import fi.jubic.quanta.db.tables.InvocationColumnSelector;
import fi.jubic.quanta.db.tables.InvocationOutputColumn;
import fi.jubic.quanta.db.tables.InvocationParameter;
import fi.jubic.quanta.db.tables.SeriesResult;
import fi.jubic.quanta.db.tables.SeriesTable;
import fi.jubic.quanta.db.tables.Task;
import fi.jubic.quanta.db.tables.TaskColumnSelector;
import fi.jubic.quanta.db.tables.TaskOutputColumn;
import fi.jubic.quanta.db.tables.TaskParameter;
import fi.jubic.quanta.db.tables.User;
import fi.jubic.quanta.db.tables.Worker;
import fi.jubic.quanta.db.tables.WorkerDefinition;
import fi.jubic.quanta.db.tables.WorkerDefinitionColumn;
import fi.jubic.quanta.db.tables.WorkerParameter;
import fi.jubic.quanta.db.tables.records.ColumnRecord;
import fi.jubic.quanta.db.tables.records.DataConnectionRecord;
import fi.jubic.quanta.db.tables.records.DataSeriesRecord;
import fi.jubic.quanta.db.tables.records.DetectionResultRecord;
import fi.jubic.quanta.db.tables.records.ExternalClientRecord;
import fi.jubic.quanta.db.tables.records.InvocationColumnSelectorRecord;
import fi.jubic.quanta.db.tables.records.InvocationOutputColumnRecord;
import fi.jubic.quanta.db.tables.records.InvocationParameterRecord;
import fi.jubic.quanta.db.tables.records.InvocationRecord;
import fi.jubic.quanta.db.tables.records.SeriesResultRecord;
import fi.jubic.quanta.db.tables.records.SeriesTableRecord;
import fi.jubic.quanta.db.tables.records.TaskColumnSelectorRecord;
import fi.jubic.quanta.db.tables.records.TaskOutputColumnRecord;
import fi.jubic.quanta.db.tables.records.TaskParameterRecord;
import fi.jubic.quanta.db.tables.records.TaskRecord;
import fi.jubic.quanta.db.tables.records.UserRecord;
import fi.jubic.quanta.db.tables.records.WorkerDefinitionColumnRecord;
import fi.jubic.quanta.db.tables.records.WorkerDefinitionRecord;
import fi.jubic.quanta.db.tables.records.WorkerParameterRecord;
import fi.jubic.quanta.db.tables.records.WorkerRecord;

import javax.annotation.Generated;

import org.jooq.ForeignKey;
import org.jooq.UniqueKey;
import org.jooq.impl.Internal;


/**
 * A class modelling foreign key relationships and constraints of tables of 
 * the <code></code> schema.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.11"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // IDENTITY definitions
    // -------------------------------------------------------------------------


    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<ColumnRecord> COLUMN_PKEY = UniqueKeys0.COLUMN_PKEY;
    public static final UniqueKey<DataConnectionRecord> DATA_CONNECTION_PKEY = UniqueKeys0.DATA_CONNECTION_PKEY;
    public static final UniqueKey<DataConnectionRecord> DATA_CONNECTION_NAME_KEY = UniqueKeys0.DATA_CONNECTION_NAME_KEY;
    public static final UniqueKey<DataSeriesRecord> DATA_SERIES_PKEY = UniqueKeys0.DATA_SERIES_PKEY;
    public static final UniqueKey<DataSeriesRecord> DATA_SERIES_TABLE_NAME_KEY = UniqueKeys0.DATA_SERIES_TABLE_NAME_KEY;
    public static final UniqueKey<DetectionResultRecord> DETECTION_RESULT_PKEY = UniqueKeys0.DETECTION_RESULT_PKEY;
    public static final UniqueKey<ExternalClientRecord> EXTERNAL_CLIENT_PKEY = UniqueKeys0.EXTERNAL_CLIENT_PKEY;
    public static final UniqueKey<InvocationRecord> INVOCATION_PKEY = UniqueKeys0.INVOCATION_PKEY;
    public static final UniqueKey<InvocationRecord> INVOCATION_TASK_ID_INVOCATION_NUMBER_KEY = UniqueKeys0.INVOCATION_TASK_ID_INVOCATION_NUMBER_KEY;
    public static final UniqueKey<InvocationColumnSelectorRecord> INVOCATION_COLUMN_SELECTOR_PKEY = UniqueKeys0.INVOCATION_COLUMN_SELECTOR_PKEY;
    public static final UniqueKey<InvocationOutputColumnRecord> INVOCATION_OUTPUT_COLUMN_PKEY = UniqueKeys0.INVOCATION_OUTPUT_COLUMN_PKEY;
    public static final UniqueKey<InvocationParameterRecord> INVOCATION_PARAMETER_PKEY = UniqueKeys0.INVOCATION_PARAMETER_PKEY;
    public static final UniqueKey<SeriesResultRecord> SERIES_RESULT_PKEY = UniqueKeys0.SERIES_RESULT_PKEY;
    public static final UniqueKey<SeriesTableRecord> SERIES_TABLE_PKEY = UniqueKeys0.SERIES_TABLE_PKEY;
    public static final UniqueKey<TaskRecord> TASK_PKEY = UniqueKeys0.TASK_PKEY;
    public static final UniqueKey<TaskRecord> TASK_NAME_KEY = UniqueKeys0.TASK_NAME_KEY;
    public static final UniqueKey<TaskColumnSelectorRecord> TASK_COLUMN_SELECTOR_PKEY = UniqueKeys0.TASK_COLUMN_SELECTOR_PKEY;
    public static final UniqueKey<TaskOutputColumnRecord> TASK_OUTPUT_COLUMN_PKEY = UniqueKeys0.TASK_OUTPUT_COLUMN_PKEY;
    public static final UniqueKey<TaskParameterRecord> TASK_PARAMETER_PKEY = UniqueKeys0.TASK_PARAMETER_PKEY;
    public static final UniqueKey<UserRecord> USER_PKEY = UniqueKeys0.USER_PKEY;
    public static final UniqueKey<WorkerRecord> WORKER_PKEY = UniqueKeys0.WORKER_PKEY;
    public static final UniqueKey<WorkerRecord> WORKER_TOKEN_KEY = UniqueKeys0.WORKER_TOKEN_KEY;
    public static final UniqueKey<WorkerDefinitionRecord> WORKER_DEFINITION_PKEY = UniqueKeys0.WORKER_DEFINITION_PKEY;
    public static final UniqueKey<WorkerDefinitionRecord> WORKER_DEFINITION_NAME_KEY = UniqueKeys0.WORKER_DEFINITION_NAME_KEY;
    public static final UniqueKey<WorkerDefinitionColumnRecord> WORKER_DEFINITION_COLUMN_PKEY = UniqueKeys0.WORKER_DEFINITION_COLUMN_PKEY;
    public static final UniqueKey<WorkerParameterRecord> WORKER_PARAMETER_PKEY = UniqueKeys0.WORKER_PARAMETER_PKEY;

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------

    public static final ForeignKey<ColumnRecord, DataSeriesRecord> COLUMN__FK_COLUMN_DATA_SERIES_ID = ForeignKeys0.COLUMN__FK_COLUMN_DATA_SERIES_ID;
    public static final ForeignKey<DataSeriesRecord, DataConnectionRecord> DATA_SERIES__FK_DATA_SERIES_DATA_CONNECTION_ID = ForeignKeys0.DATA_SERIES__FK_DATA_SERIES_DATA_CONNECTION_ID;
    public static final ForeignKey<DetectionResultRecord, InvocationRecord> DETECTION_RESULT__FK_RESULT_INVOCATION_ID = ForeignKeys0.DETECTION_RESULT__FK_RESULT_INVOCATION_ID;
    public static final ForeignKey<ExternalClientRecord, TaskRecord> EXTERNAL_CLIENT__FK_EXTERNAL_CLIENT_TASK_ID = ForeignKeys0.EXTERNAL_CLIENT__FK_EXTERNAL_CLIENT_TASK_ID;
    public static final ForeignKey<InvocationRecord, TaskRecord> INVOCATION__FK_INVOCATION_TASK_ID = ForeignKeys0.INVOCATION__FK_INVOCATION_TASK_ID;
    public static final ForeignKey<InvocationRecord, WorkerRecord> INVOCATION__FK_INVOCATION_WORKER_ID = ForeignKeys0.INVOCATION__FK_INVOCATION_WORKER_ID;
    public static final ForeignKey<InvocationColumnSelectorRecord, DataSeriesRecord> INVOCATION_COLUMN_SELECTOR__FK_INVOCATION_COLUMN_SELECTOR_DATA_SERIES_ID = ForeignKeys0.INVOCATION_COLUMN_SELECTOR__FK_INVOCATION_COLUMN_SELECTOR_DATA_SERIES_ID;
    public static final ForeignKey<InvocationColumnSelectorRecord, InvocationRecord> INVOCATION_COLUMN_SELECTOR__FK_INVOCATION_COLUMN_SELECTOR_INVOCATION_ID = ForeignKeys0.INVOCATION_COLUMN_SELECTOR__FK_INVOCATION_COLUMN_SELECTOR_INVOCATION_ID;
    public static final ForeignKey<InvocationColumnSelectorRecord, WorkerDefinitionColumnRecord> INVOCATION_COLUMN_SELECTOR__FK_INVOCATION_COLUMN_SELECTOR_WORKER_DEFINITION_COLUMN_ID = ForeignKeys0.INVOCATION_COLUMN_SELECTOR__FK_INVOCATION_COLUMN_SELECTOR_WORKER_DEFINITION_COLUMN_ID;
    public static final ForeignKey<InvocationOutputColumnRecord, InvocationRecord> INVOCATION_OUTPUT_COLUMN__FK_INVOCATION_OUTPUT_COLUMN_INVOCATION_ID = ForeignKeys0.INVOCATION_OUTPUT_COLUMN__FK_INVOCATION_OUTPUT_COLUMN_INVOCATION_ID;
    public static final ForeignKey<InvocationParameterRecord, InvocationRecord> INVOCATION_PARAMETER__FK_INVOCATION_PARAMETER = ForeignKeys0.INVOCATION_PARAMETER__FK_INVOCATION_PARAMETER;
    public static final ForeignKey<SeriesResultRecord, InvocationRecord> SERIES_RESULT__FK_RESULT_INVOCATION_ID = ForeignKeys0.SERIES_RESULT__FK_RESULT_INVOCATION_ID;
    public static final ForeignKey<SeriesTableRecord, DataSeriesRecord> SERIES_TABLE__FK_SERIES_TABLE_DATA_SERIES_ID = ForeignKeys0.SERIES_TABLE__FK_SERIES_TABLE_DATA_SERIES_ID;
    public static final ForeignKey<TaskRecord, WorkerDefinitionRecord> TASK__FK_TASK_WORKER_DEF_ID = ForeignKeys0.TASK__FK_TASK_WORKER_DEF_ID;
    public static final ForeignKey<TaskColumnSelectorRecord, DataSeriesRecord> TASK_COLUMN_SELECTOR__FK_TASK_COLUMN_SELECTOR_DATA_SERIES_ID = ForeignKeys0.TASK_COLUMN_SELECTOR__FK_TASK_COLUMN_SELECTOR_DATA_SERIES_ID;
    public static final ForeignKey<TaskColumnSelectorRecord, TaskRecord> TASK_COLUMN_SELECTOR__FK_TASK_COLUMN_SELECTOR_TASK_ID = ForeignKeys0.TASK_COLUMN_SELECTOR__FK_TASK_COLUMN_SELECTOR_TASK_ID;
    public static final ForeignKey<TaskColumnSelectorRecord, WorkerDefinitionColumnRecord> TASK_COLUMN_SELECTOR__FK_TASK_COLUMN_SELECTOR_WORKER_DEFINITION_COLUMN_ID = ForeignKeys0.TASK_COLUMN_SELECTOR__FK_TASK_COLUMN_SELECTOR_WORKER_DEFINITION_COLUMN_ID;
    public static final ForeignKey<TaskOutputColumnRecord, TaskRecord> TASK_OUTPUT_COLUMN__FK_TASK_OUTPUT_COLUMN_TASK_ID = ForeignKeys0.TASK_OUTPUT_COLUMN__FK_TASK_OUTPUT_COLUMN_TASK_ID;
    public static final ForeignKey<TaskParameterRecord, TaskRecord> TASK_PARAMETER__FK_TASK_PARAMETER_TASK_ID = ForeignKeys0.TASK_PARAMETER__FK_TASK_PARAMETER_TASK_ID;
    public static final ForeignKey<WorkerRecord, WorkerDefinitionRecord> WORKER__FK_WORKER_WORKER_DEF_ID = ForeignKeys0.WORKER__FK_WORKER_WORKER_DEF_ID;
    public static final ForeignKey<WorkerDefinitionColumnRecord, WorkerDefinitionRecord> WORKER_DEFINITION_COLUMN__FK_WORKER_DEFINITION_INPUT_COLUMN_WORKER_DEFINITION_ID = ForeignKeys0.WORKER_DEFINITION_COLUMN__FK_WORKER_DEFINITION_INPUT_COLUMN_WORKER_DEFINITION_ID;
    public static final ForeignKey<WorkerParameterRecord, WorkerDefinitionRecord> WORKER_PARAMETER__FK_WORKER_PARAMETER_WORKER_DEFINITION_ID = ForeignKeys0.WORKER_PARAMETER__FK_WORKER_PARAMETER_WORKER_DEFINITION_ID;

    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class UniqueKeys0 {
        public static final UniqueKey<ColumnRecord> COLUMN_PKEY = Internal.createUniqueKey(Column.COLUMN, "column_pkey", Column.COLUMN.ID);
        public static final UniqueKey<DataConnectionRecord> DATA_CONNECTION_PKEY = Internal.createUniqueKey(DataConnection.DATA_CONNECTION, "data_connection_pkey", DataConnection.DATA_CONNECTION.ID);
        public static final UniqueKey<DataConnectionRecord> DATA_CONNECTION_NAME_KEY = Internal.createUniqueKey(DataConnection.DATA_CONNECTION, "data_connection_name_key", DataConnection.DATA_CONNECTION.NAME);
        public static final UniqueKey<DataSeriesRecord> DATA_SERIES_PKEY = Internal.createUniqueKey(DataSeries.DATA_SERIES, "data_series_pkey", DataSeries.DATA_SERIES.ID);
        public static final UniqueKey<DataSeriesRecord> DATA_SERIES_TABLE_NAME_KEY = Internal.createUniqueKey(DataSeries.DATA_SERIES, "data_series_table_name_key", DataSeries.DATA_SERIES.TABLE_NAME);
        public static final UniqueKey<DetectionResultRecord> DETECTION_RESULT_PKEY = Internal.createUniqueKey(DetectionResult.DETECTION_RESULT, "detection_result_pkey", DetectionResult.DETECTION_RESULT.ID);
        public static final UniqueKey<ExternalClientRecord> EXTERNAL_CLIENT_PKEY = Internal.createUniqueKey(ExternalClient.EXTERNAL_CLIENT, "external_client_pkey", ExternalClient.EXTERNAL_CLIENT.ID);
        public static final UniqueKey<InvocationRecord> INVOCATION_PKEY = Internal.createUniqueKey(Invocation.INVOCATION, "invocation_pkey", Invocation.INVOCATION.ID);
        public static final UniqueKey<InvocationRecord> INVOCATION_TASK_ID_INVOCATION_NUMBER_KEY = Internal.createUniqueKey(Invocation.INVOCATION, "invocation_task_id_invocation_number_key", Invocation.INVOCATION.TASK_ID, Invocation.INVOCATION.INVOCATION_NUMBER);
        public static final UniqueKey<InvocationColumnSelectorRecord> INVOCATION_COLUMN_SELECTOR_PKEY = Internal.createUniqueKey(InvocationColumnSelector.INVOCATION_COLUMN_SELECTOR, "invocation_column_selector_pkey", InvocationColumnSelector.INVOCATION_COLUMN_SELECTOR.ID);
        public static final UniqueKey<InvocationOutputColumnRecord> INVOCATION_OUTPUT_COLUMN_PKEY = Internal.createUniqueKey(InvocationOutputColumn.INVOCATION_OUTPUT_COLUMN, "invocation_output_column_pkey", InvocationOutputColumn.INVOCATION_OUTPUT_COLUMN.ID);
        public static final UniqueKey<InvocationParameterRecord> INVOCATION_PARAMETER_PKEY = Internal.createUniqueKey(InvocationParameter.INVOCATION_PARAMETER, "invocation_parameter_pkey", InvocationParameter.INVOCATION_PARAMETER.ID);
        public static final UniqueKey<SeriesResultRecord> SERIES_RESULT_PKEY = Internal.createUniqueKey(SeriesResult.SERIES_RESULT, "series_result_pkey", SeriesResult.SERIES_RESULT.ID);
        public static final UniqueKey<SeriesTableRecord> SERIES_TABLE_PKEY = Internal.createUniqueKey(SeriesTable.SERIES_TABLE, "series_table_pkey", SeriesTable.SERIES_TABLE.ID);
        public static final UniqueKey<TaskRecord> TASK_PKEY = Internal.createUniqueKey(Task.TASK, "task_pkey", Task.TASK.ID);
        public static final UniqueKey<TaskRecord> TASK_NAME_KEY = Internal.createUniqueKey(Task.TASK, "task_name_key", Task.TASK.NAME);
        public static final UniqueKey<TaskColumnSelectorRecord> TASK_COLUMN_SELECTOR_PKEY = Internal.createUniqueKey(TaskColumnSelector.TASK_COLUMN_SELECTOR, "task_column_selector_pkey", TaskColumnSelector.TASK_COLUMN_SELECTOR.ID);
        public static final UniqueKey<TaskOutputColumnRecord> TASK_OUTPUT_COLUMN_PKEY = Internal.createUniqueKey(TaskOutputColumn.TASK_OUTPUT_COLUMN, "task_output_column_pkey", TaskOutputColumn.TASK_OUTPUT_COLUMN.ID);
        public static final UniqueKey<TaskParameterRecord> TASK_PARAMETER_PKEY = Internal.createUniqueKey(TaskParameter.TASK_PARAMETER, "task_parameter_pkey", TaskParameter.TASK_PARAMETER.ID);
        public static final UniqueKey<UserRecord> USER_PKEY = Internal.createUniqueKey(User.USER, "user_pkey", User.USER.ID);
        public static final UniqueKey<WorkerRecord> WORKER_PKEY = Internal.createUniqueKey(Worker.WORKER, "worker_pkey", Worker.WORKER.ID);
        public static final UniqueKey<WorkerRecord> WORKER_TOKEN_KEY = Internal.createUniqueKey(Worker.WORKER, "worker_token_key", Worker.WORKER.TOKEN);
        public static final UniqueKey<WorkerDefinitionRecord> WORKER_DEFINITION_PKEY = Internal.createUniqueKey(WorkerDefinition.WORKER_DEFINITION, "worker_definition_pkey", WorkerDefinition.WORKER_DEFINITION.ID);
        public static final UniqueKey<WorkerDefinitionRecord> WORKER_DEFINITION_NAME_KEY = Internal.createUniqueKey(WorkerDefinition.WORKER_DEFINITION, "worker_definition_name_key", WorkerDefinition.WORKER_DEFINITION.NAME);
        public static final UniqueKey<WorkerDefinitionColumnRecord> WORKER_DEFINITION_COLUMN_PKEY = Internal.createUniqueKey(WorkerDefinitionColumn.WORKER_DEFINITION_COLUMN, "worker_definition_column_pkey", WorkerDefinitionColumn.WORKER_DEFINITION_COLUMN.ID);
        public static final UniqueKey<WorkerParameterRecord> WORKER_PARAMETER_PKEY = Internal.createUniqueKey(WorkerParameter.WORKER_PARAMETER, "worker_parameter_pkey", WorkerParameter.WORKER_PARAMETER.ID);
    }

    private static class ForeignKeys0 {
        public static final ForeignKey<ColumnRecord, DataSeriesRecord> COLUMN__FK_COLUMN_DATA_SERIES_ID = Internal.createForeignKey(fi.jubic.quanta.db.Keys.DATA_SERIES_PKEY, Column.COLUMN, "column__fk_column_data_series_id", Column.COLUMN.DATA_SERIES_ID);
        public static final ForeignKey<DataSeriesRecord, DataConnectionRecord> DATA_SERIES__FK_DATA_SERIES_DATA_CONNECTION_ID = Internal.createForeignKey(fi.jubic.quanta.db.Keys.DATA_CONNECTION_PKEY, DataSeries.DATA_SERIES, "data_series__fk_data_series_data_connection_id", DataSeries.DATA_SERIES.DATA_CONNECTION_ID);
        public static final ForeignKey<DetectionResultRecord, InvocationRecord> DETECTION_RESULT__FK_RESULT_INVOCATION_ID = Internal.createForeignKey(fi.jubic.quanta.db.Keys.INVOCATION_PKEY, DetectionResult.DETECTION_RESULT, "detection_result__fk_result_invocation_id", DetectionResult.DETECTION_RESULT.INVOCATION_ID);
        public static final ForeignKey<ExternalClientRecord, TaskRecord> EXTERNAL_CLIENT__FK_EXTERNAL_CLIENT_TASK_ID = Internal.createForeignKey(fi.jubic.quanta.db.Keys.TASK_PKEY, ExternalClient.EXTERNAL_CLIENT, "external_client__fk_external_client_task_id", ExternalClient.EXTERNAL_CLIENT.TASK_ID);
        public static final ForeignKey<InvocationRecord, TaskRecord> INVOCATION__FK_INVOCATION_TASK_ID = Internal.createForeignKey(fi.jubic.quanta.db.Keys.TASK_PKEY, Invocation.INVOCATION, "invocation__fk_invocation_task_id", Invocation.INVOCATION.TASK_ID);
        public static final ForeignKey<InvocationRecord, WorkerRecord> INVOCATION__FK_INVOCATION_WORKER_ID = Internal.createForeignKey(fi.jubic.quanta.db.Keys.WORKER_PKEY, Invocation.INVOCATION, "invocation__fk_invocation_worker_id", Invocation.INVOCATION.WORKER_ID);
        public static final ForeignKey<InvocationColumnSelectorRecord, DataSeriesRecord> INVOCATION_COLUMN_SELECTOR__FK_INVOCATION_COLUMN_SELECTOR_DATA_SERIES_ID = Internal.createForeignKey(fi.jubic.quanta.db.Keys.DATA_SERIES_PKEY, InvocationColumnSelector.INVOCATION_COLUMN_SELECTOR, "invocation_column_selector__fk_invocation_column_selector_data_series_id", InvocationColumnSelector.INVOCATION_COLUMN_SELECTOR.DATA_SERIES_ID);
        public static final ForeignKey<InvocationColumnSelectorRecord, InvocationRecord> INVOCATION_COLUMN_SELECTOR__FK_INVOCATION_COLUMN_SELECTOR_INVOCATION_ID = Internal.createForeignKey(fi.jubic.quanta.db.Keys.INVOCATION_PKEY, InvocationColumnSelector.INVOCATION_COLUMN_SELECTOR, "invocation_column_selector__fk_invocation_column_selector_invocation_id", InvocationColumnSelector.INVOCATION_COLUMN_SELECTOR.INVOCATION_ID);
        public static final ForeignKey<InvocationColumnSelectorRecord, WorkerDefinitionColumnRecord> INVOCATION_COLUMN_SELECTOR__FK_INVOCATION_COLUMN_SELECTOR_WORKER_DEFINITION_COLUMN_ID = Internal.createForeignKey(fi.jubic.quanta.db.Keys.WORKER_DEFINITION_COLUMN_PKEY, InvocationColumnSelector.INVOCATION_COLUMN_SELECTOR, "invocation_column_selector__fk_invocation_column_selector_worker_definition_column_id", InvocationColumnSelector.INVOCATION_COLUMN_SELECTOR.WORKER_DEFINITION_COLUMN_ID);
        public static final ForeignKey<InvocationOutputColumnRecord, InvocationRecord> INVOCATION_OUTPUT_COLUMN__FK_INVOCATION_OUTPUT_COLUMN_INVOCATION_ID = Internal.createForeignKey(fi.jubic.quanta.db.Keys.INVOCATION_PKEY, InvocationOutputColumn.INVOCATION_OUTPUT_COLUMN, "invocation_output_column__fk_invocation_output_column_invocation_id", InvocationOutputColumn.INVOCATION_OUTPUT_COLUMN.INVOCATION_ID);
        public static final ForeignKey<InvocationParameterRecord, InvocationRecord> INVOCATION_PARAMETER__FK_INVOCATION_PARAMETER = Internal.createForeignKey(fi.jubic.quanta.db.Keys.INVOCATION_PKEY, InvocationParameter.INVOCATION_PARAMETER, "invocation_parameter__fk_invocation_parameter", InvocationParameter.INVOCATION_PARAMETER.INVOCATION_ID);
        public static final ForeignKey<SeriesResultRecord, InvocationRecord> SERIES_RESULT__FK_RESULT_INVOCATION_ID = Internal.createForeignKey(fi.jubic.quanta.db.Keys.INVOCATION_PKEY, SeriesResult.SERIES_RESULT, "series_result__fk_result_invocation_id", SeriesResult.SERIES_RESULT.INVOCATION_ID);
        public static final ForeignKey<SeriesTableRecord, DataSeriesRecord> SERIES_TABLE__FK_SERIES_TABLE_DATA_SERIES_ID = Internal.createForeignKey(fi.jubic.quanta.db.Keys.DATA_SERIES_PKEY, SeriesTable.SERIES_TABLE, "series_table__fk_series_table_data_series_id", SeriesTable.SERIES_TABLE.DATA_SERIES_ID);
        public static final ForeignKey<TaskRecord, WorkerDefinitionRecord> TASK__FK_TASK_WORKER_DEF_ID = Internal.createForeignKey(fi.jubic.quanta.db.Keys.WORKER_DEFINITION_PKEY, Task.TASK, "task__fk_task_worker_def_id", Task.TASK.WORKER_DEF_ID);
        public static final ForeignKey<TaskColumnSelectorRecord, DataSeriesRecord> TASK_COLUMN_SELECTOR__FK_TASK_COLUMN_SELECTOR_DATA_SERIES_ID = Internal.createForeignKey(fi.jubic.quanta.db.Keys.DATA_SERIES_PKEY, TaskColumnSelector.TASK_COLUMN_SELECTOR, "task_column_selector__fk_task_column_selector_data_series_id", TaskColumnSelector.TASK_COLUMN_SELECTOR.DATA_SERIES_ID);
        public static final ForeignKey<TaskColumnSelectorRecord, TaskRecord> TASK_COLUMN_SELECTOR__FK_TASK_COLUMN_SELECTOR_TASK_ID = Internal.createForeignKey(fi.jubic.quanta.db.Keys.TASK_PKEY, TaskColumnSelector.TASK_COLUMN_SELECTOR, "task_column_selector__fk_task_column_selector_task_id", TaskColumnSelector.TASK_COLUMN_SELECTOR.TASK_ID);
        public static final ForeignKey<TaskColumnSelectorRecord, WorkerDefinitionColumnRecord> TASK_COLUMN_SELECTOR__FK_TASK_COLUMN_SELECTOR_WORKER_DEFINITION_COLUMN_ID = Internal.createForeignKey(fi.jubic.quanta.db.Keys.WORKER_DEFINITION_COLUMN_PKEY, TaskColumnSelector.TASK_COLUMN_SELECTOR, "task_column_selector__fk_task_column_selector_worker_definition_column_id", TaskColumnSelector.TASK_COLUMN_SELECTOR.WORKER_DEFINITION_COLUMN_ID);
        public static final ForeignKey<TaskOutputColumnRecord, TaskRecord> TASK_OUTPUT_COLUMN__FK_TASK_OUTPUT_COLUMN_TASK_ID = Internal.createForeignKey(fi.jubic.quanta.db.Keys.TASK_PKEY, TaskOutputColumn.TASK_OUTPUT_COLUMN, "task_output_column__fk_task_output_column_task_id", TaskOutputColumn.TASK_OUTPUT_COLUMN.TASK_ID);
        public static final ForeignKey<TaskParameterRecord, TaskRecord> TASK_PARAMETER__FK_TASK_PARAMETER_TASK_ID = Internal.createForeignKey(fi.jubic.quanta.db.Keys.TASK_PKEY, TaskParameter.TASK_PARAMETER, "task_parameter__fk_task_parameter_task_id", TaskParameter.TASK_PARAMETER.TASK_ID);
        public static final ForeignKey<WorkerRecord, WorkerDefinitionRecord> WORKER__FK_WORKER_WORKER_DEF_ID = Internal.createForeignKey(fi.jubic.quanta.db.Keys.WORKER_DEFINITION_PKEY, Worker.WORKER, "worker__fk_worker_worker_def_id", Worker.WORKER.DEFINITION_ID);
        public static final ForeignKey<WorkerDefinitionColumnRecord, WorkerDefinitionRecord> WORKER_DEFINITION_COLUMN__FK_WORKER_DEFINITION_INPUT_COLUMN_WORKER_DEFINITION_ID = Internal.createForeignKey(fi.jubic.quanta.db.Keys.WORKER_DEFINITION_PKEY, WorkerDefinitionColumn.WORKER_DEFINITION_COLUMN, "worker_definition_column__fk_worker_definition_input_column_worker_definition_id", WorkerDefinitionColumn.WORKER_DEFINITION_COLUMN.DEFINITION_ID);
        public static final ForeignKey<WorkerParameterRecord, WorkerDefinitionRecord> WORKER_PARAMETER__FK_WORKER_PARAMETER_WORKER_DEFINITION_ID = Internal.createForeignKey(fi.jubic.quanta.db.Keys.WORKER_DEFINITION_PKEY, WorkerParameter.WORKER_PARAMETER, "worker_parameter__fk_worker_parameter_worker_definition_id", WorkerParameter.WORKER_PARAMETER.WORKER_DEFINITION_ID);
    }
}
