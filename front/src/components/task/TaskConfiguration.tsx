import React, { useState } from 'react';
import {
  Typography as T,
  Button,
  Grid,
  Icon,
  Input,
  InputLabel,
  Fab,
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
  WorkerParameter
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
  configurations: Record<string, string | number | boolean>;
  onConfigChange: (set: Record<string, string | number | boolean>) => void;
  dataConnection: DataConnection | null | undefined;
  setTaskColumnSelectors: (columnSelectors: ColumnSelector[]) => void;
  setTaskOutputColumns: (outputColumns: OutputColumn[]) => void;
  setWorkerDef: (workerDef: WorkerDef | undefined, outputColumns: OutputColumn[], additionalParams?: Record<string, WorkerParameter>) => void;
  taskType: TaskType;
  setType: (set: TaskType) => void;
  additionalParams?: Record<string, WorkerParameter>;
  setAdditionalParams: (additionalParams: Record<string, WorkerParameter>) => void;
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
  configurations,
  onConfigChange,
  dataConnection,
  setTaskColumnSelectors,
  setTaskOutputColumns,
  setWorkerDef,
  taskType,
  setType,
  additionalParams,
  setAdditionalParams,
  tasks,
  triggersAreValid,
  isCronTriggerValid,
  cronHelperText
}: TaskProps) => {
  const [configKey, setConfigKey] = useState<string>('');
  const [configType, setConfigType] = useState<'string' | 'number' | 'boolean' | 'function'>('string');

  const common = commonStyles();

  const taskTriggerList = tasks?.map(({ id, name }) => ({ id, name }));

  const addConfig = () => {
    if (
      configKey.length > 0
      && !Object.keys(configurations).some(oldKey => oldKey === configKey)
    ) {
      const newConfig = configurations;
      switch (configType) {
        case 'string':
          newConfig[configKey] = '-';
          break;
        case 'number':
          newConfig[configKey] = 0;
          break;
        case 'boolean':
          newConfig[configKey] = false;
          break;
      }
      onConfigChange(newConfig);
      setConfigKey('');
      setConfigType('string');
    }
  };

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
                configurations={configurations}
                onConfigChange={onConfigChange}
                dataConnection={dataConnection}
                setTaskColumnSelectors={setTaskColumnSelectors}
                setTaskOutputColumns={setTaskOutputColumns}
                setWorkerDef={setWorkerDef}
                additionalParams={additionalParams}
                setAdditionalParams={setAdditionalParams}
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
        {(editable && taskType === TaskType.process) && (
          <div className={common.padding}>
            <Grid container spacing={2}>
              <Grid item xs={6}>
                <InputLabel
                  htmlFor='config-key-input'
                  shrink
                >
                  Config key
                </InputLabel>
                <Input
                  fullWidth
                  id='config-key-input'
                  value={configKey}
                  onChange={e => setConfigKey(e.target.value as string)}
                />
              </Grid>
              <Grid item xs={6}>
                <InputLabel
                  htmlFor='config-type-select'
                  shrink
                >
                  Config type
                </InputLabel>
                <Select
                  fullWidth
                  value={configType}
                  onChange={e => {
                    const value = e.target.value as string;
                    switch (value) {
                      case 'string':
                        setConfigType('string');
                        break;
                      case 'number':
                        setConfigType('number');
                        break;
                      case 'boolean':
                        setConfigType('boolean');
                        break;
                    }
                  }}
                  inputProps={{ id: 'config-type-select' }}
                >
                  [
                  <MenuItem value='string'>String</MenuItem>,
                  <MenuItem value='number'>Number</MenuItem>,
                  <MenuItem value='boolean'>Boolean</MenuItem>
                  ]
                </Select>
              </Grid>
            </Grid>
            <Fab
              className={common.topMargin}
              disabled={
                configKey.length === 0
                || Object.keys(configurations).some(oldKey => oldKey === configKey)
              }
              variant='extended'
              color='primary'
              onClick={addConfig}
            >
              <Icon className={common.icon}>
                add
              </Icon>
              Add config
            </Fab>
          </div>
        )}
      </Paper>
    </>
  );
};
