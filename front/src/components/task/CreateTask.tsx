import React, { useState, useEffect, useMemo, useCallback } from 'react';
import {
  Icon,
  InputLabel,
  Fab,
  MenuItem,
  Paper,
  Select,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography as T
} from '@material-ui/core';

import clsx from 'clsx';

import {
  commonStyles
} from '../common';
import {
  useDataConnections,
  useRouter,
  useDataConnection,
  useTasks,
  useNameCheck,
  useMultipleNamesCheck,
  useCronValidation,
  useMultipleParametersValidation
} from '../../hooks';
import { useAlerts } from '../../alert';
import {
  Task,
  WorkerType,
  DataSeries,
  TaskType,
  ColumnSelector,
  Parameter
} from '../../types';
import { TaskConfiguration } from './TaskConfiguration';

const initialTask = {
  id: -1,
  name: '',
  workerDef: {
    id: -1,
    type: 'Detect' as WorkerType,
    name: '',
    description: '',
    parameters: undefined,
    columns: []
  },
  columnSelectors: [],
  outputColumns: [],
  cronTrigger: null,
  taskTrigger: null,
  taskType: TaskType.process,
  parameters: undefined
};

interface Props {
  match: {
    params: {
      dataConnectionId: number;
    };
  };
}
export default ({
  match: {
    params
  }
}: Props) => {
  const [task, setTask] = useState<Task>(
    params.dataConnectionId
      ? {
        ...initialTask,
        taskType: TaskType.sync,
        workerDef: undefined
      }
      : initialTask
  );
  const [dataConnectionId, setDataConnectionId] = useState<number>(-1);
  const [dataSeries, setDataSeries] = useState<DataSeries | null>(null);
  const [triggersAreValid, setTriggersAreValid] = useState<boolean>(true);
  const [connectionOptions, setConnectionOptions] = useState<JSX.Element[]>([<MenuItem key={-1} value={-1}>Unset</MenuItem>]);

  const { create, tasks } = useTasks();
  const { dataConnection } = useDataConnection(dataConnectionId);
  const dataConnectionQuery = useMemo(() => ({ notDeleted: true }), []);
  const { dataConnections } = useDataConnections(dataConnectionQuery);
  const { history } = useRouter();

  const alertContext = useAlerts('CREATE-TASK');
  const setError = useCallback(
    (heading: string, message: string) => {
      alertContext.alertError(heading, message);
    },
    [alertContext]
  );

  const nameArray = useMemo(
    () => {
      return tasks?.filter(task => task.deletedAt === null).map(task => task.name) || [];
    },
    [tasks]
  );
  const { nameIsValid, helperText } = useNameCheck(
    task.name,
    nameArray
  );

  const common = commonStyles();

  const useConnections = useCallback(
    () => {
      if (dataConnections) {
        setConnectionOptions(
          [
            <MenuItem key={-1} value={-1}>Unset</MenuItem>,
            ...dataConnections
              .filter(({ type }) => task.taskType === TaskType.process || type === 'JDBC')
              .map((conn, idx) => (
                <MenuItem key={idx} value={conn.id}>{conn.name}</MenuItem>
              ))
          ]
        );
        if (params.dataConnectionId) {
          setDataConnectionId(params.dataConnectionId);
        }
      }
    },
    [dataConnections, params.dataConnectionId, task.taskType]
  );

  useEffect(() => {
    useConnections();
  }, [params.dataConnectionId, useConnections]);

  useEffect(() => {
    if (dataConnection && dataConnection.series.length > 0) {
      setDataSeries(dataConnection.series[0]);
    }
    else if (!dataConnection) {
      setDataSeries(null);
    }
  }, [dataConnection]);

  const validateTriggers = useCallback(
    () => {
      const bothTriggesAreSet = ((task.cronTrigger && task.cronTrigger.length > 0) && task.taskTrigger) as boolean;
      if (bothTriggesAreSet) {
        setTriggersAreValid(!bothTriggesAreSet);
        setError('Input Error', 'Task cannot have both triggers');
      }
      setTriggersAreValid(!bothTriggesAreSet);
    },
    [setError, task.cronTrigger, task.taskTrigger]
  );

  useEffect(
    () => {
      validateTriggers();
    },
    [validateTriggers]
  );

  const taskTypeChange = useCallback(
    () => {
      if (task.taskType === TaskType.sync) {
        // Unset the DataConnection if taskType sync is selected with non-JDBC DataConnection
        if (dataConnection && dataConnection?.type !== 'JDBC') {
          setDataConnectionId(-1);
        }
        setTask(({
          ...task,
          workerDef: undefined,
          columnSelectors: [],
          outputColumns: []
        }));
      }
    },
    [dataConnection, task]
  );

  useEffect(
    () => {
      taskTypeChange();
    },
    [taskTypeChange]
  );

  const { isCronTriggerValid, cronHelperText } = useCronValidation(
    task.cronTrigger ?? ''
  );

  const seriesOptions = dataConnection?.series.map(series => (
    <MenuItem key={series.id} value={series.id}>{series.name}</MenuItem>
  )) ?? [];

  const connectionColumns = dataSeries?.columns.map(column => ({
    ...column,
    series: dataSeries
  })) ?? [];

  const useMultipleNamesCheckProps = useMemo(
    () => {
      return {
        names: task.outputColumns.map(column => column.alias),
        array: [],
        allowEmpty: true
      };
    },
    [task.outputColumns]
  );

  const { namesIsValid: aliasesIsValid, helperTexts: aliasHelperTexts } = useMultipleNamesCheck(
    useMultipleNamesCheckProps.names,
    useMultipleNamesCheckProps.array,
    useMultipleNamesCheckProps.allowEmpty
  );

  const areAllAliasesValid = useMemo(
    () => {
      return aliasesIsValid.every(aliasValid => aliasValid);
    },
    [aliasesIsValid]
  );

  const {
    parametersIsValid: parametersValidation,
    helperTexts: parametersHelperTexts
  } = useMultipleParametersValidation(
    task.parameters,
    task.workerDef?.parameters
  );

  const areAllParametersValid = useMemo(
    () => {
      return parametersValidation.every(parameterValid => parameterValid);
    },
    [parametersValidation]
  );

  const validateTask = (): boolean => {
    switch (task.taskType) {
      case TaskType.process:
        return task.columnSelectors.length > 0
          && task.workerDef?.id !== -1
          && task.name !== ''
          && nameIsValid
          && areAllAliasesValid
          && areAllParametersValid
          && triggersAreValid
          && isCronTriggerValid;
      case TaskType.sync:
        return task.name !== ''
          && connectionColumns.length > 0
          && nameIsValid
          && triggersAreValid
          && isCronTriggerValid;
      default:
        return false;
    }
  };

  const createColumnSelectorForSyncTask = (): ColumnSelector[] => {
    if (dataSeries) {
      return connectionColumns
        .filter(({ index }) => index === 0)
        .map(({ index, name, type }) => ({
          id: -1,
          columnIndex: index,
          columnName: name,
          type: type,
          series: dataSeries
        })
        );
    }
    return [];
  };

  const onCreate = () => {
    if (validateTask()) {
      if (task.taskType === TaskType.sync) {
      // Data sync task requires one column selector because data series are bound to columns
        create({
          ...task,
          columnSelectors: createColumnSelectorForSyncTask()
        })
          .then(res => history.push(`/task/${res.id}`));
      }
      else {
        create(task)
          .then(res => history.push(`/task/${res.id}`));
      }
    }
  };

  const onCancel = () => {
    history.push('/task-list');
  };

  const handleTaskNameChange = (name: string) => {
    setTask({
      ...task,
      name
    });
  };

  function handleDataConnectionChange (
    value: number
  ) {
    setDataConnectionId(value);
    // Clear column selectors on data connection change
    setTask({
      ...task,
      columnSelectors: []
    });
  }

  return (
    <>
      <div className={clsx(common.verticalPadding, common.header)}>
        <div>
          <T variant='h4'>New task</T>
        </div>
        <div className={common.toggle}>
          <Fab
            disabled={!validateTask()}
            variant='extended'
            color='primary'
            onClick={onCreate}
          >
            <Icon className={common.icon}>
              add
            </Icon>
            Create
          </Fab>
          <Fab variant='extended' color='primary' onClick={onCancel} className={common.leftMargin}>
            <Icon className={common.icon}>
              close
            </Icon>
            Cancel
          </Fab>
        </div>
      </div>
      <T variant='h5'>Data Connection</T>
      <Paper className={clsx(common.topMargin, common.bottomMargin)}>
        <div className={common.padding}>
          <InputLabel
            htmlFor='task-connection-select'
            shrink
          >
            Data Connection
          </InputLabel>
          <Select
            fullWidth
            value={dataConnectionId || ''}
            onChange={e => {
              handleDataConnectionChange(e.target.value as number);
            }}
            inputProps={{ id: 'task-connection-select' }}
          >
            {connectionOptions}
          </Select>
          {dataConnection
          && dataConnection.series.length > 1
          && (
            <>
              <InputLabel
                htmlFor='task-series-select'
                shrink
              >
                Data Series
              </InputLabel>
              <Select
                fullWidth
                value={dataSeries ? dataSeries.id : undefined}
                onChange={e => {
                  const series = dataConnection && dataConnection.series.find(s => s.id === e.target.value as number);
                  setDataSeries(series || null);
                }}
                inputProps={{ id: 'task-series-select' }}
              >
                {seriesOptions}
              </Select>
            </>
          )}
        </div>
        <Table>
          <TableHead>
            <TableRow key={0}>
              <TableCell key={1}>Name</TableCell>
              <TableCell key={2}>Class</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {connectionColumns.map((column, i) => (
              <TableRow key={i}>
                <TableCell>{column.name}</TableCell>
                <TableCell>{column.type.className}</TableCell>
                <TableCell />
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Paper>
      <TaskConfiguration
        editable
        creatingTask
        nameIsValid={nameIsValid}
        helperText={helperText}
        aliasesIsValid={aliasesIsValid}
        aliasHelperTexts={aliasHelperTexts}
        name={task.name}
        setName={name => handleTaskNameChange(name)}
        workerDef={task.workerDef}
        setWorkerDef={(workerDef, outputColumns) => setTask({
          ...task,
          workerDef,
          outputColumns,
          columnSelectors: []
        })}
        cronTrigger={task.cronTrigger}
        setCronTrigger={cronTrigger => setTask({
          ...task,
          cronTrigger
        })}
        taskTrigger={task.taskTrigger}
        setTaskTrigger={taskTrigger => setTask({
          ...task,
          taskTrigger
        })}
        dataConnection={dataConnection}
        setTaskColumnSelectors={columnSelectors => setTask({
          ...task,
          columnSelectors
        })}
        setTaskOutputColumns={outputColumns => setTask({
          ...task,
          outputColumns
        })}
        taskType={task.taskType}
        setType={taskType => setTask({
          ...task,
          taskType
        })}
        parameters={task.parameters}
        setParameters={(parameters?: Parameter[]) => setTask({
          ...task,
          parameters
        })}
        parametersIsValid={parametersValidation}
        parametersHelperTexts={parametersHelperTexts}
        tasks={tasks}
        triggersAreValid={triggersAreValid}
        isCronTriggerValid={isCronTriggerValid}
        cronHelperText={cronHelperText}
      />
    </>
  );
};
