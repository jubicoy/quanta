import React from 'react';
import { Task, columnSelectorToString } from '../../types';
import { useRouter } from '../../hooks';
import { commonStyles } from '../common';

import {
  LinearProgress,
  Paper,
  Table,
  TableCell,
  TableBody,
  TableHead,
  TableRow,
  Typography as T
} from '@material-ui/core';

interface Props {
  tasks: Task[] | undefined;
}

export default ({
  tasks
}: Props) => {
  const classes = commonStyles();
  const { history } = useRouter();

  const goToDetails = (item: Task) => {
    history.push(`/task/${item.id}`);
  };

  if (!tasks) {
    return (
      <>
        <T>Loading...</T>
        <LinearProgress variant='query' />
      </>
    );
  }

  if (tasks && tasks.length === 0) {
    return <T variant='subtitle1'>0 tasks found!</T>;
  }

  return (
    <Paper>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell component='th'>ID</TableCell>
            <TableCell component='th'>Name</TableCell>
            <TableCell component='th'>Type</TableCell>
            <TableCell component='th'>Data Connection</TableCell>
            <TableCell component='th'>Column Selectors</TableCell>
            <TableCell component='th'>Worker Name</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {tasks?.map((item: Task) => (
            <TableRow
              key={item.id}
              hover
              onClick={() => goToDetails(item)}
              className={classes.row}
            >
              <TableCell>{item.id}</TableCell>
              <TableCell>{item.name}</TableCell>
              <TableCell>{item.workerDef?.type ?? 'Sync'}</TableCell>
              <TableCell>
                {(item.columnSelectors.length > 0
                && item.columnSelectors[0].series
                && item.columnSelectors[0].series.dataConnection)
                  ? item.columnSelectors[0].series.dataConnection.name
                  : ''}
              </TableCell>
              <TableCell>
                {item.columnSelectors
                  .map(columnSelector => columnSelectorToString(columnSelector))
                  .join(', ')}
              </TableCell>
              <TableCell>{item.workerDef?.name ?? ''}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </Paper>
  );
};
