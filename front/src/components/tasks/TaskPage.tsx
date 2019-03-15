import React, { useMemo, useState } from 'react';
import {
  Fab,
  Grid,
  Icon,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  Typography as T
} from '@material-ui/core';
import clsx from 'clsx';

import {
  useTasks,
  useDataConnections,
  useRouter,
  useWorkerDefs
} from '../../hooks';
import { commonStyles } from '../common';
import TaskTable from './TaskTable';

export default () => {
  const common = commonStyles();
  const dataConnectionQuery = useMemo(() => ({ notDeleted: true }), []);
  const { dataConnections } = useDataConnections(dataConnectionQuery);
  const { history } = useRouter();
  const workerDefQuery = useMemo(() => ({ notDeleted: true }), []);
  const { workerDefs } = useWorkerDefs(workerDefQuery);
  const [connectionId, setConnectionId] = useState<number|undefined>(undefined);
  const [workerDefId, setWorkerDefId] = useState<number|undefined>(undefined);
  const taskQuery = useMemo(
    () => ({
      connection: connectionId,
      workerDef: workerDefId,
      notDeleted: true
    }),
    [connectionId, workerDefId]
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
      <T variant='h4'>Tasks</T>
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
      <TaskTable
        tasks={tasks}
      />
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
