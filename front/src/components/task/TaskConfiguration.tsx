import React, { useState, useCallback } from 'react';
import {
  Typography as T,
  MenuItem,
  Paper,
  Select,
  Table,
  TableBody,
  TextField,
  Button,
  Icon,
  createStyles,
  makeStyles
} from '@material-ui/core';
import clsx from 'clsx';
import moment from 'moment';

import {
  commonStyles,
  TableRowItem,
  DateQuickSelector
} from '../common';
import {
  Task,
  TaskType
} from '../../types';

const useStyles = makeStyles(() => {
  const height = 46;
  const borderStyle = {
    border: '1px solid rgba(0, 0, 0, .24)',
    borderRadius: '4px',
    '&:hover': {
      border: '1px solid rgba(0, 0, 0, 1)'
    }
  };
  return createStyles({
    toolbarDateInput: {
      '& .MuiInputBase-root': {
        height: height + 'px'
      },
      '& .MuiOutlinedInput-notchedOutline ': {
        ...borderStyle
      },
      'marginInline': '10px'
    }
  });
});

const INPUT_DATE_FORMAT = 'YYYY-MM-DDTHH:mm';

interface TaskProps {
  editable: boolean;
  creatingTask?: boolean;
  nameIsValid: boolean;
  helperText?: string;
  name: string;
  setName: (name: string) => void;
  taskType: TaskType;
  setType: (set: TaskType) => void;
  cronTrigger: string | null;
  setCronTrigger: (set: string | null) => void;
  taskTrigger: number | null;
  setTaskTrigger: (set: number | null) => void;
  triggersAreValid?: boolean;
  isCronTriggerValid?: boolean;
  cronHelperText?: string;
  tasks: Task[] | undefined;
  syncIntervalOffset?: number;
  setSyncIntervalOffset: (syncIntervalOffset: number | undefined) => void;
}

export const TaskConfiguration = ({
  editable,
  creatingTask,
  nameIsValid,
  helperText,
  name,
  setName,
  taskType,
  setType,
  cronTrigger,
  setCronTrigger,
  taskTrigger,
  setTaskTrigger,
  triggersAreValid,
  isCronTriggerValid,
  cronHelperText,
  tasks,
  syncIntervalOffset,
  setSyncIntervalOffset
}: TaskProps) => {
  const common = commonStyles();
  const classes = useStyles();

  const taskTriggerList = tasks?.map(({ id, name }) => ({ id, name }));

  const [startDate] = useState<Date>(new Date());
  const [endDate, setEndDate] = useState<Date>(
    syncIntervalOffset
      ? moment.utc(new Date()).subtract(syncIntervalOffset, 'seconds').toDate()
      : new Date()
  );
  const [enableSyncInterval, setEnableSyncInterval] = useState<string>(
    syncIntervalOffset ? 'enable' : 'disable'
  );

  const handleSyncIntervalOffset = useCallback(
    (endDate: Date) => {
      const interval = moment.utc(startDate).diff(moment.utc(endDate)) / 1000;
      setEndDate(endDate);
      setSyncIntervalOffset(interval);
    },
    [setSyncIntervalOffset, startDate]
  );

  const handleEnableSyncInterval = (value: string) => {
    setEnableSyncInterval(value);
    if (value === 'disable') {
      setSyncIntervalOffset(undefined);
    }
  };

  return (
    <>
      <T variant='h5'>Task Configuration</T>
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
                ? (
                  <Select
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
                ) : `${taskType || ''}`
              }
            />
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
                      helperText={cronHelperText}
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
            {
              taskType === TaskType.sync
                && <TableRowItem
                  title='Sync Interval'
                  value={editable
                    ? (
                      <div>
                        <TextField
                          select
                          label='Select'
                          variant='outlined'
                          className={clsx(classes.toolbarDateInput)}
                          value={enableSyncInterval}
                          onChange={(e) => handleEnableSyncInterval(e.target.value)}
                        >
                          <MenuItem value='enable'>Enable</MenuItem>
                          <MenuItem value='disable'>Disable</MenuItem>
                        </TextField>
                        {
                          enableSyncInterval === 'enable'
                            ? <>
                              <DateQuickSelector
                                startDate={endDate}
                                setStartDate={handleSyncIntervalOffset}
                                endDate={startDate}
                              />
                              <TextField
                                className={clsx(classes.toolbarDateInput)}
                                variant='outlined'
                                label='From'
                                type='datetime-local'
                                value={moment.utc(startDate).format(INPUT_DATE_FORMAT)}
                                disabled
                              />
                              <TextField
                                className={clsx(classes.toolbarDateInput)}
                                variant='outlined'
                                label='To'
                                type='datetime-local'
                                value={moment.utc(endDate).format(INPUT_DATE_FORMAT)}
                                inputProps={{
                                  max: `${moment.utc(startDate).format(INPUT_DATE_FORMAT)}`
                                }}
                                onChange={(e) => handleSyncIntervalOffset(new Date(e.target.value))}
                              />
                            </>
                            : null
                        }
                      </div>
                    )
                    : `${syncIntervalOffset || ''}`
                  }
                />
            }
          </TableBody>
        </Table>
      </Paper>
    </>
  );
};
