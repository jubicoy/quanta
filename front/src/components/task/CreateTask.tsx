import React, { useState, useEffect, useMemo, useCallback } from 'react';
import {
  Icon,
  Fab,
  Typography as T
} from '@material-ui/core';

import clsx from 'clsx';

import {
  commonStyles
} from '../common';
import {
  useRouter,
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
  TaskType,
  ColumnSelector,
  Parameter
} from '../../types';
import { TaskConfiguration } from './TaskConfiguration';
import { WorkerDefConfiguration } from './WorkerDefConfiguration';
import { ColumnsConfiguration } from './ColumnsConfiguration';

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
  const common = commonStyles();

  const [task, setTask] = useState<Task>(
    params.dataConnectionId
      ? {
        ...initialTask,
        taskType: TaskType.sync,
        workerDef: undefined
      }
      : initialTask
  );
  const [columnsForSyncTask, setColumnForSyncTask] = useState<ColumnSelector[]>([]);

  const [triggersAreValid, setTriggersAreValid] = useState<boolean>(true);

  const { create, tasks } = useTasks();
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

  const { isCronTriggerValid, cronHelperText } = useCronValidation(
    task.cronTrigger ?? ''
  );

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
          && columnsForSyncTask.length > 0
          && nameIsValid
          && triggersAreValid
          && isCronTriggerValid;
      default:
        return false;
    }
  };

  const onCreate = () => {
    if (validateTask()) {
      if (task.taskType === TaskType.sync) {
      // Data sync task requires one column selector because data series are bound to columns
        create({
          ...task,
          columnSelectors: columnsForSyncTask
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
      <TaskConfiguration
        editable
        creatingTask
        nameIsValid={nameIsValid}
        helperText={helperText}
        name={task.name}
        setName={name => handleTaskNameChange(name)}
        taskType={task.taskType}
        setType={taskType => setTask({
          ...task,
          taskType
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
        triggersAreValid={triggersAreValid}
        isCronTriggerValid={isCronTriggerValid}
        cronHelperText={cronHelperText}
        tasks={tasks}
        syncIntervalOffset={task.syncIntervalOffset}
        setSyncIntervalOffset={syncIntervalOffset => setTask({
          ...task,
          syncIntervalOffset
        })}
      />
      <WorkerDefConfiguration
        editable
        creatingTask
        workerDef={task.workerDef}
        setWorkerDef={(workerDef, outputColumns) => setTask({
          ...task,
          workerDef,
          outputColumns,
          columnSelectors: []
        })}
        parameters={task.parameters}
        setParameters={(parameters?: Parameter[]) => setTask({
          ...task,
          parameters
        })}
        parametersIsValid={parametersValidation}
        parametersHelperTexts={parametersHelperTexts}
        task={task}
      />
      <ColumnsConfiguration
        editable
        creatingTask
        aliasesIsValid={aliasesIsValid}
        aliasHelperTexts={aliasHelperTexts}
        workerDef={task.workerDef}
        setWorkerDef={(workerDef, outputColumns) => setTask({
          ...task,
          workerDef,
          outputColumns,
          columnSelectors: []
        })}
        setTaskColumnSelectors={columnSelectors => setTask({
          ...task,
          columnSelectors
        })}
        setTaskOutputColumns={outputColumns => setTask({
          ...task,
          outputColumns
        })}
        task={task}
        setTask={setTask}
        dataConnectionParam={params.dataConnectionId}
        setColumnForSyncTask={setColumnForSyncTask}
      />
    </>
  );
};
