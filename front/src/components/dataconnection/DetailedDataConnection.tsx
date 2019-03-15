import React, { useState, useEffect } from 'react';
import clsx from 'clsx';
import {
  useDataConnection,
  useRouter
} from '../../hooks';

import {
  Table,
  TableCell,
  TableBody,
  TableHead,
  TableRow,
  Paper,
  makeStyles,
  createStyles,
  LinearProgress,
  Typography as T,
  Button,
  Link,
  Fab,
  Icon,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle
} from '@material-ui/core';
import { JsonIngestDataConnectionConfiguration } from '../../types';
import { commonStyles } from '../common';

const useStyles = makeStyles(theme =>
  createStyles({
    paper: {
      margin: theme.spacing(2, 0)
    },
    tableTitle: {
      padding: '16px'
    },
    chartContainer: {},
    chartToolbar: {
      marginLeft: '24px'
    }
  })
);

interface Props {
  match: {
    params: {
      id: number;
      name: string;
    };
  };
}
export default ({
  match: {
    params
  }
}: Props) => {
  const common = commonStyles();
  const { dataConnection, deleteDataConnection } = useDataConnection(params.id);
  const classes = useStyles();
  const { history } = useRouter();
  const [openDeleteDialog, setOpenDeleteDialog] = useState<boolean>(false);

  useEffect(() => {
    if (!params.name && dataConnection) {
      history.replace(`${params.id}/${encodeURI(dataConnection.name)}`);
    }
  }, [dataConnection, params.id, params.name, history]);

  if (!dataConnection) {
    return (
      <LinearProgress variant='query' />
    );
  }

  const handleClickOpenDeleteDialog = () => {
    setOpenDeleteDialog(true);
  };

  const handleCloseDeleteDialog = () => {
    setOpenDeleteDialog(false);
  };

  const dataSeries = dataConnection.series;

  if (dataConnection.deletedAt) {
    return (
      <>
        <div className={common.header}>
          <T variant='h4'>Data connection {dataConnection.name} has been deleted</T>
        </div>
      </>
    );
  }

  const renderDataConnectionDetails = () => {
    switch (dataConnection.type) {
      case 'CSV':
        return (
          <Table>
            <TableHead>
              <TableRow>
                <TableCell component='th'>Name</TableCell>
                <TableCell component='th'>Description</TableCell>
                <TableCell component='th'>Type</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              <TableRow>
                <TableCell>{dataConnection.name}</TableCell>
                <TableCell>{dataConnection.description}</TableCell>
                <TableCell>{dataConnection.type}</TableCell>
              </TableRow>
            </TableBody>
          </Table>
        );

      case 'JDBC':
        return (
          <Table>
            <TableHead>
              <TableRow>
                <TableCell component='th'>Name</TableCell>
                <TableCell component='th'>Description</TableCell>
                <TableCell component='th'>Type</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              <TableRow>
                <TableCell>{dataConnection.name}</TableCell>
                <TableCell>{dataConnection.description}</TableCell>
                <TableCell>{dataConnection.type}</TableCell>
              </TableRow>
            </TableBody>
          </Table>
        );

      case 'JSON_INGEST':
        return (
          <Table>
            <TableHead>
              <TableRow>
                <TableCell component='th'>Name</TableCell>
                <TableCell component='th'>Description</TableCell>
                <TableCell component='th'>Type</TableCell>
                <TableCell component='th' colSpan={2}>Token</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              <TableRow>
                <TableCell>{dataConnection.name}</TableCell>
                <TableCell>{dataConnection.description}</TableCell>
                <TableCell>{dataConnection.type}</TableCell>
                <TableCell>
                  {(dataConnection.configuration as JsonIngestDataConnectionConfiguration).token}
                </TableCell>
                <TableCell>
                  <Link href={`/json-ingest/${dataConnection.id}`}>
                    Submit a JSON to this DataConnection
                  </Link>
                </TableCell>
              </TableRow>
            </TableBody>
          </Table>
        );

      default:
        break;
    }
    ;
  };

  return (
    <>
      <div className={common.header}>
        <T variant='h4'>Data connection details</T>
        <div className={common.toggle}>
          <Fab
            className={common.leftMargin}
            variant='extended'
            color='primary'
            onClick={handleClickOpenDeleteDialog}
          >
            <Icon className={clsx(common.icon)}>
              {'delete'}
            </Icon>
            Delete
          </Fab>
        </div>
      </div>
      <Paper className={classes.paper}>
        {renderDataConnectionDetails()}
      </Paper>
      <T variant='h4'>Data series details</T>
      {(dataSeries.length <= 0)
        ? <i>No Data Series available</i>
        : dataSeries.map((series, i) => (
          <div key={i}>
            <Paper className={classes.paper}>
              <div className={classes.tableTitle}>
                <T variant='body1'>Name: <b>{series.name}</b></T>
                <T variant='body1'>Description: <b>{series.description}</b></T>
              </div>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell component='th' colSpan={4}>Columns</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell component='th'>Name</TableCell>
                    <TableCell component='th'>Format</TableCell>
                    <TableCell component='th'>Nullable</TableCell>
                    <TableCell component='th'>Class</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {series.columns.map((column, i) => (
                    <TableRow key={i}>
                      <TableCell>{column.name}</TableCell>
                      <TableCell>{column.type.format}</TableCell>
                      <TableCell>
                        {
                          column.type.nullable
                            ? column.type.nullable.toString()
                            : 'false'
                        }
                      </TableCell>
                      <TableCell>{column.type.className}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </Paper>
          </div>
        )
        )
      }
      <Dialog
        open={openDeleteDialog}
        onClose={handleCloseDeleteDialog}
        aria-labelledby='alert-dialog-title'
        aria-describedby='alert-dialog-description'
      >
        <DialogTitle id='alert-dialog-title'>{'Delete'}</DialogTitle>
        <DialogContent>
          <DialogContentText id='alert-dialog-description'>
            Are you sure want to delete current data connection? This operation will also delete all of the tasks
            created with this data connection. Deleted task's invocations and results won't deleted.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDeleteDialog} color='primary'>
            Cancel
          </Button>
          <Button
            onClick={() => deleteDataConnection()
              .then(() => history.push('/data-connections'))
            }
            color='primary'
            autoFocus>
            Delete
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
};
