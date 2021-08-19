import React, { useMemo, useState, useEffect } from 'react';
import {
  Fab,
  Grid,
  Icon,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  TextField,
  Typography as T
} from '@material-ui/core';

import Autocomplete from '@material-ui/lab/Autocomplete';

import clsx from 'clsx';

import {
  useTasks,
  useDataConnections,
  useRouter,
  useWorkerDefs,
  useTags
} from '../../hooks';

import { searchTasks } from '../../client';
import { Tag } from '../../types';
import { commonStyles } from '../common';
import TaskTable from './TaskTable';

export default () => {
  const common = commonStyles();
  const dataConnectionQuery = useMemo(() => ({ notDeleted: true }), []);
  const { dataConnections } = useDataConnections(dataConnectionQuery);
  const { history } = useRouter();
  const { tags } = useTags();
  const workerDefQuery = useMemo(() => ({ notDeleted: true }), []);
  const { workerDefs } = useWorkerDefs(workerDefQuery);
  const [connectionId, setConnectionId] = useState<number|undefined>(undefined);
  const [workerDefId, setWorkerDefId] = useState<number|undefined>(undefined);
  const [filter, setFilter] = useState<string>('Active');

  const [taskTags, setTaskTags] = useState<Tag[]>();
  const [tagFilter, setTagFilter] = useState<number[]>();

  useEffect(
    () => {
      if (taskTags) {
        const searchTagIds = taskTags.map(({ id }) => id);
        searchTasks(searchTagIds).then(setTagFilter);
      }
    },
    [taskTags]
  );

  const taskQuery = useMemo(
    () => ({
      connection: connectionId,
      workerDef: workerDefId,
      notDeleted: filter === 'Active' ? true : filter === 'Deleted' ? false : undefined
    }),
    [connectionId, workerDefId, filter]
  );
  const {
    tasks,
    refresh
  } = useTasks(taskQuery);

  const connectionOptions = [
    <MenuItem key={-1} value={-1}>All</MenuItem>,
    ...(dataConnections || []).map((connection) => (
      <MenuItem key={connection.id} value={connection.id}>{connection.name}</MenuItem>
    ))
  ];

  const workerOptions = [
    <MenuItem key={-1} value={-1}>All</MenuItem>,
    ...(workerDefs || []).map((def) => (
      <MenuItem key={def.id} value={def.id}>{def.name}</MenuItem>
    ))
  ];

  return (
    <>
      <Select
        className={clsx(common.bottomMargin, common.floatRight)}
        autoWidth
        value={filter}
        onChange={e => {
          const value = e.target.value as string;
          setFilter(value);
        }}
      >
        <MenuItem value='Active'>Active Tasks</MenuItem>
        <MenuItem value='Deleted'>Deleted Tasks</MenuItem>
        <MenuItem value='All'>All Tasks</MenuItem>
      </Select>
      <T variant='h4'>Tasks</T>
      {tags && <Autocomplete
        style={{ padding: '10px' }}
        multiple
        size='small'
        options={tags}
        getOptionLabel={(option) => option.name}
        filterSelectedOptions
        onChange={(event, value) => setTaskTags(value)}
        renderInput={(params) => (
          <TextField
            {...params}
            variant='outlined'
            label='Tags'
            placeholder='Tag'
          />
        )}
      />
      }
      <Paper className={clsx(common.padding, common.topMargin, common.bottomMargin)}>
        <Grid container spacing={2}>
          <Grid item xs={6}>
            <InputLabel
              htmlFor='task-connection-select'
              shrink
            >
              Data Connection
            </InputLabel>
            <Select
              fullWidth
              value={connectionId || -1}
              onChange={e => {
                const value = e.target.value as number;
                setConnectionId(value !== -1 ? value : undefined);
              }}
              inputProps={{ id: 'task-connection-select' }}
            >
              {connectionOptions}
            </Select>
          </Grid>
          <Grid item xs={6}>
            <InputLabel
              htmlFor='task-connection-select'
              shrink
            >
              Worker Definition
            </InputLabel>
            <Select
              fullWidth
              value={workerDefId || -1}
              onChange={e => {
                const value = e.target.value as number;
                setWorkerDefId(value !== -1 ? value : undefined);
              }}
              inputProps={{ id: 'task-connection-select' }}
            >
              {workerOptions}
            </Select>
          </Grid>
        </Grid>
        <Fab className={common.topMargin} variant='extended' color='primary' onClick={refresh}>
          <Icon className={common.icon}>
            refresh
          </Icon>
          Refresh
        </Fab>
      </Paper>
      { (taskTags && tagFilter && taskTags.length > 0 && tagFilter.length === 0)
        ? <T color='error'>No task found!</T>
        : <TaskTable
          filter={tagFilter || []}
          tasks={tasks}
        />
      }

      <Fab
        className={clsx(common.topMargin, common.floatRight)}
        variant='extended'
        color='primary'
        onClick={() => history.push('/task-new')}
      >
        <Icon className={common.icon}>
          add
        </Icon>
        Create new
      </Fab>
    </>
  );
};
