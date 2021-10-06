import React from 'react';
import { Worker } from '../../types';
import { useRouter } from '../../hooks';
import clsx from 'clsx';

import {
  Table,
  TableCell,
  TableBody,
  TableHead,
  TableRow,
  Typography as T,
  Paper,
  LinearProgress,
  Icon
} from '@material-ui/core';

import { commonStyles } from '../common';

interface Props {
  workers: null | Worker[];
}

export default ({
  workers
}: Props) => {
  const classes = commonStyles();
  const { history } = useRouter();

  const goToDetails = (item: Worker) => {
    history.push(`/worker/${item.id}`);
  };

  if (workers === null) {
    return (
      <>
        <T>Loading...</T>
        <LinearProgress variant='query' />
      </>
    );
  }

  if (workers.length === 0) {
    return <T variant='subtitle1'>0 detectors found!</T>;
  }

  return (
    <Paper>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell component='th'>Name</TableCell>
            <TableCell component='th'>Type</TableCell>
            <TableCell component='th'>Description</TableCell>
            <TableCell component='th' align='right'>Authorized</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {workers.map((item: Worker) => (
            <TableRow
              key={item.id}
              hover
              onClick={() => goToDetails(item)}
              className={classes.row}
            >
              <TableCell>{`Worker ${item.id}: ${item.definition.name}`}</TableCell>
              <TableCell>{item.definition.type}</TableCell>
              <TableCell>{item.definition.description}</TableCell>
              <TableCell align='right'>
                <Icon
                  className={clsx(classes.icon, {
                    [classes.accept]: item.acceptedOn,
                    [classes.unaccept]: !item.acceptedOn
                  })}
                >{item.acceptedOn ? 'check_circle' : 'block_circle'}</Icon>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </Paper>
  );
};
