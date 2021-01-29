import React from 'react';
import {
  Typography as T,
  Button,
  Icon,
  MenuItem,
  Paper,
  Select,
  Table,
  TableBody,
  TextField
} from '@material-ui/core';
import clsx from 'clsx';

import { commonStyles, TableRowItem } from '../common';
import {
  WorkerDef,
  DataConnection,
  ColumnSelector,
  OutputColumn,
  Task,
  TaskType,
  Parameter
} from '../../types';
import { WorkerDefConfiguration } from './WorkerDefConfiguration';

interface TaskProps {
  editable: boolean;
  creatingTask?: boolean;
  nameIsValid: boolean;
  helperText?: string;
  aliasesIsValid?: boolean[];
  aliasHelperTexts?: string[];
  name: string;
  setName: (name: string) => void;
  workerDef: WorkerDef | undefined;
  cronTrigger: string | null;
  setCronTrigger: (set: string | null) => void;
  taskTrigger: number | null;
  setTaskTrigger: (set: number | null) => void;
  dataConnection: DataConnection | null | undefined;
  setTaskColumnSelectors: (columnSelectors: ColumnSelector[]) => void;
  setTaskOutputColumns: (outputColumns: OutputColumn[]) => void;
  setWorkerDef: (workerDef: WorkerDef | undefined, outputColumns: OutputColumn[]) => void;
  taskType: TaskType;
  setType: (set: TaskType) => void;
  parameters?: Parameter[];
  setParameters: (parameters?: Parameter[]) => void;
  parametersIsValid?: boolean[];
  parametersHelperTexts?: string[];
  tasks: Task[] | undefined;
  triggersAreValid?: boolean;
  isCronTriggerValid?: boolean;
  cronHelperText?: string;
}

export const TaskConfiguration = ({
  editable,
  creatingTask,
  nameIsValid,
  helperText,
  aliasesIsValid,
  aliasHelperTexts,
  name,
  setName,
  workerDef,
  cronTrigger,
  setCronTrigger,
  taskTrigger,
  setTaskTrigger,
  dataConnection,
  setTaskColumnSelectors,
  setTaskOutputColumns,
  setWorkerDef,
  taskType,
  setType,
  parameters,
  setParameters,
  parametersIsValid,
  parametersHelperTexts,
  tasks,
  triggersAreValid,
  isCronTriggerValid,
  cronHelperText
}: TaskProps) => {
  const common = commonStyles();

  const taskTriggerList = tasks?.map(({ id, name }) => ({ id, name }));

  return (
    <>
      <T variant='h5'>Configuration</T>
      <Paper className={clsx(common.topMargin, common.bottomMargin)}>
        <Table>
          <TableBody>
            <TableRowItem
              title='Task Name'
              value={editable
                ? (
                  <TextField
                    fullWidth
                    error={!nameIsValid}
                    helperText={helperText || 'Allowed characters: letters, numbers, - and _'}
                    value={name}
                    onChange={e => setName(e.target.value)}
                  />
                )
                : `${name || ''}`}
            />
            <TableRowItem
              title='Task Type'
              value={(editable && creatingTask)
                ? <Select
                  fullWidth
                  value={taskType || 'process'}
                  onChange={e => {
                    const value = e.target.value as TaskType;
                    setType(value);
                  }}
                  inputProps={{ id: 'task-type-select' }}
                >
                  {Object.values(TaskType)
                    .map((type, idx) => (
                      <MenuItem key={idx} value={type}>{type}</MenuItem>
                    ))
                  }
                </Select>
                : `${taskType || ''}`
              }
            />
            {taskType === TaskType.process
              && <WorkerDefConfiguration
                editable={editable}
                creatingTask={creatingTask}
                aliasesIsValid={aliasesIsValid}
                aliasHelperTexts={aliasHelperTexts}
                workerDef={workerDef}
                cronTrigger={cronTrigger}
                setCronTrigger={setCronTrigger}
                taskTrigger={taskTrigger}
                setTaskTrigger={setTaskTrigger}
                dataConnection={dataConnection}
                setTaskColumnSelectors={setTaskColumnSelectors}
                setTaskOutputColumns={setTaskOutputColumns}
                setWorkerDef={setWorkerDef}
                parameters={parameters}
                setParameters={setParameters}
                parametersIsValid={parametersIsValid}
                parametersHelperTexts={parametersHelperTexts}
                tasks={tasks}
                triggersAreValid={triggersAreValid}
                isCronTriggerValid={isCronTriggerValid}
                cronHelperText={cronHelperText}
              />
            }
            {
              taskType === TaskType.sync
                && <>
                  <TableRowItem
                    title='Cron Trigger'
                    value={editable
                      ? (
                        <div>
                          <TextField
                            style={{
                              width: '90%'
                            }}
                            error={!isCronTriggerValid || !triggersAreValid}
                            helperText={'Requires valid CRON expression'}
                            value={cronTrigger || ''}
                            onChange={e => setCronTrigger(e.target.value.length > 0 ? e.target.value : null)}
                          />
                          <Button
                            style={{
                              color: 'grey',
                              background: 'none',
                              width: '10%'
                            }}
                            onClick={() => {
                              setCronTrigger(null);
                            }}
                          >
                            <Icon>close</Icon>
                          </Button>
                        </div>
                      )
                      : `${cronTrigger || ''}`}
                  />
                  <TableRowItem
                    title='Task Trigger'
                    value={editable
                      ? (
                        <div>
                          <Select
                            style={{
                              width: '90%'
                            }}
                            error={!triggersAreValid}
                            value={taskTrigger || ''}
                            onChange={e => {
                              setTaskTrigger(Number(e.target.value));
                            }}
                            inputProps={{ id: 'task-trigger' }}
                          >
                            {
                              taskTriggerList?.map(({ id, name }) => (
                                <MenuItem key={id} value={id}>
                                  {id + ': ' + name}
                                </MenuItem>
                              )) ?? []
                            }
                          </Select>
                          <Button
                            style={{
                              color: 'grey',
                              background: 'none',
                              width: '10%'
                            }}
                            onClick={() => {
                              setTaskTrigger(null);
                            }}
                          >
                            <Icon>close</Icon>
                          </Button>
                        </div>
                      )
                      : `${taskTrigger || ''}`}
                    tooltip={'Task is launched whenever an Invocation of referred Task is completed'}
                  />
                </>
            }
          </TableBody>
        </Table>
      </Paper>
    </>
  );
};
