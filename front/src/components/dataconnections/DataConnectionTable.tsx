import React from 'react';
import { DataConnection } from '../../types';
import { useRouter } from '../../hooks';

import {
  Table,
  TableCell,
  TableBody,
  TableHead,
  TableRow,
  Typography as T,
  Paper,
  makeStyles,
  createStyles,
  LinearProgress
} from '@material-ui/core';

interface Props {
  connections: null | DataConnection[];
}

const useStyles = makeStyles(theme =>
  createStyles({
    paper: {
      margin: theme.spacing(2, 0)
    },
    row: {
      cursor: 'pointer'
    }
  })
);

export default ({
  connections
}: Props) => {
  const classes = useStyles();
  const { history } = useRouter();

  const goToDetails = (item: DataConnection) => {
    history.push(`/data-connections/${item.id}/${encodeURI(item.name)}`);
  };

  if (!connections) {
    return (
      <>
        <T>Loading data connections...</T>
        <LinearProgress variant='query' />
      </>
    );
  }

  if (connections.length === 0) {
    return <T variant='subtitle1'>0 data connections found!</T>;
  }

  return (
    <Paper className={classes.paper}>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell component='th'>Name</TableCell>
            <TableCell component='th'>Description</TableCell>
            <TableCell component='th'>Type</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {connections.map((item, i) => (
            <TableRow
              key={i}
              hover
              onClick={() => goToDetails(item)}
              className={classes.row}
            >
              <TableCell>{item.name}</TableCell>
              <TableCell>{item.description}</TableCell>
              <TableCell>{item.type}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </Paper>
  );
};
