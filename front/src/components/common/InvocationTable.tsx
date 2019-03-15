import React from 'react';

import {
  Button,
  Icon,
  InputLabel,
  MenuItem,
  Select,
  Table,
  TableCell,
  TableBody,
  TableHead,
  TableRow,
  LinearProgress
} from '@material-ui/core';
// Disable time-parsing until backend's Instants-Timestamp problem is sorted out
// import moment from 'moment';

import { commonStyles } from '../common';
import { useRouter } from '../../hooks';
import { Invocation, InvocationStatus } from '../../types';

interface Props {
  invocations: Invocation[] | null;
  status?: InvocationStatus;
  setStatus: (set?: InvocationStatus) => void;
  showTask?: boolean;
  showWorker?: boolean;
  refreshInvocations?: () => void;
}
export default ({
  invocations,
  status,
  setStatus,
  showTask,
  showWorker,
  refreshInvocations
}: Props) => {
  const classes = commonStyles();
  const { history } = useRouter();

  const goToDetails = (item: Invocation) => {
    history.push(`/invocation/${item.id}`);
  };

  const statusOptions = [
    <MenuItem key={0} value='All'>All</MenuItem>,
    <MenuItem key={1} value='Completed'>Completed</MenuItem>,
    <MenuItem key={2} value='Running'>Running</MenuItem>,
    <MenuItem key={3} value='Pending'>Pending</MenuItem>,
    <MenuItem key={4} value='Error'>Error</MenuItem>
  ];

  if (!invocations) {
    return (
      <LinearProgress variant='query' />
    );
  }

  return (
    <div>
      <div className={classes.padding}>
        <InputLabel
          htmlFor='invocation-status-select'
          shrink
        >
          Invocation Status
        </InputLabel>
        <Select
          style={{
            width: '90%'
          }}
          value={status || 'All'}
          onChange={e => {
            const value = e.target.value as string;
            switch (value) {
              case 'Completed':
              case 'Running':
              case 'Pending':
              case 'Error':
                setStatus(value as InvocationStatus);
                break;
              default:
                setStatus(undefined);
            }
          }}
          inputProps={{ id: 'invocation-status-select' }}
        >
          {statusOptions}
        </Select>
        <Button
          style={{ background: 'none' }}
          onClick={refreshInvocations}>
          <Icon>
            refresh
          </Icon>
        </Button>
      </div>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell component='th'>ID</TableCell>
            <TableCell component='th'>Number</TableCell>
            <TableCell component='th'>Status</TableCell>
            {showTask && <TableCell component='th'>Task</TableCell>}
            {showWorker && <TableCell component='th'>Worker</TableCell>}
            <TableCell component='th'>Started</TableCell>
            <TableCell component='th'>Ended</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {invocations.map((invocation, i) => (
            <TableRow
              key={i}
              onClick={() => goToDetails(invocation)}
              className={classes.row}
            >
              <TableCell>{invocation.id}</TableCell>
              <TableCell>{invocation.invocationNumber}</TableCell>
              <TableCell>{invocation.status.toLocaleLowerCase()}</TableCell>
              {showTask && <TableCell>{invocation.task.name}</TableCell>}
              {showWorker && <TableCell>{invocation.worker?.token ?? 'Sync'}</TableCell>}
              {/* Temporary fix to time display until backend format is sorted out */}
              <TableCell>{invocation.startTime}</TableCell>
              <TableCell>{invocation.endTime}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
};
