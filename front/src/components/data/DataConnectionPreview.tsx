import React, { useContext, useState } from 'react';

import {
  Button,
  LinearProgress,
  Paper,
  Typography as T,
  Table,
  TableBody,
  TableHead,
  TableRow,
  TableCell,
  Tooltip
} from '@material-ui/core';

import {
  DataSeries,
  Column
} from '../../types';
import {
  submitDataSeries
} from '../../client';
import { useRouter } from '../../hooks';

import { _DataConnectionConfiguratorContext } from './DataImportPage';
import { dataStyles } from './DataStyles';
import { CsvPreview } from './csv';
import { JdbcPreview } from './jdbc';
import { JsonIngestPreview } from './json';

interface ExtendedProps {
  column: Column;
}

const TableRowItemExtended = ({
  column
}: ExtendedProps) => {
  return (
    <TableRow>
      <TableCell
        component='th'
        scope='row'
      >
        {column.name}
      </TableCell>
      <TableCell>{column.type.className}</TableCell>
      <TableCell>{column.type.format}</TableCell>
      <TableCell>{column.type.nullable
        ? column.type.nullable.toString()
        : 'false'
      }</TableCell>
    </TableRow>
  );
};

interface DataConnectionPreviewProps {
  reset: () => void;
}

export const DataConnectionPreview = ({
  reset
}: DataConnectionPreviewProps) => {
  const {
    dataConnection,
    dataSeries,

    setSuccess,
    setError,

    handleBack
  } = useContext(_DataConnectionConfiguratorContext);
  const [disableConfirm, setDisableConfirm] = useState<boolean>(false);
  const {
    history
  } = useRouter();

  if (!dataConnection || !dataSeries) {
    return (
      <>
        <T>Loading...</T>
        <LinearProgress variant='query' />
      </>
    );
  }

  const addNewDataConnectionAndImportData = () => {
    if (dataSeries && dataSeries.dataConnection) {
      setDisableConfirm(true);
      submitDataSeries(dataSeries.dataConnection.id, dataSeries, false)
        .then((dataSeries: DataSeries) => {
          reset();
          if (dataSeries.dataConnection) {
            setDisableConfirm(false);
            history.push(`/data-connections/${dataSeries.dataConnection.id}`);
            setSuccess('Import successful');
          }
        })
        .catch((error: Error) => {
          setDisableConfirm(false);
          setError('Import failed', error);
        });
    }
  };

  const addNewDataConnection = () => {
    if (dataSeries && dataSeries.dataConnection) {
      setDisableConfirm(true);
      submitDataSeries(dataSeries.dataConnection.id, dataSeries, true)
        .then((dataSeries: DataSeries) => {
          reset();
          if (dataSeries.dataConnection) {
            setDisableConfirm(false);
            history.push(`/task-new/${dataSeries.dataConnection.id}`);
            setSuccess('Import successful');
          }
        })
        .catch((error: Error) => {
          setDisableConfirm(false);
          setError('Import failed', error);
        });
    }
  };

  const classes = dataStyles();

  let connectionPreview = (
    <></>
  );

  switch (dataConnection.type) {
    case 'CSV':
      connectionPreview = (
        <CsvPreview />
      );
      break;

    case 'JDBC':
      connectionPreview = (
        <JdbcPreview />
      );
      break;

    case 'JSON_INGEST':
      connectionPreview = (
        <JsonIngestPreview />
      );
      break;

    default:
      break;
  }

  return (
    <>
      {connectionPreview}
      <div className={classes.header}>
        <T component='span' variant='h5'>Preprocessing fields configuration</T>
      </div>
      <Paper>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Name</TableCell>
              <TableCell>Class</TableCell>
              <TableCell>Format</TableCell>
              <TableCell>Nullable</TableCell>
            </TableRow>
          </TableHead>
          {dataSeries.columns && (
            <TableBody>
              {dataSeries.columns.map((column) => {
                return (
                  <TableRowItemExtended key={column.index} column={column} />
                );
              })}
            </TableBody>
          )}
        </Table>
      </Paper>
      {
        disableConfirm && <LinearProgress variant='query' />
      }
      <Button
        className={classes.topSpacing}
        onClick={() => handleBack()}
      >
        Back
      </Button>
      <Tooltip title='Confirm Data connection and import data now'>
        <Button
          className={classes.topSpacing}
          color='primary'
          variant='contained'
          disabled={disableConfirm}
          onClick={() => addNewDataConnectionAndImportData()}
        >
          Confirm
        </Button>
      </Tooltip>
      {
        dataConnection.type === 'JDBC' && (
          <Tooltip title='Confirm Data connection without importing data'>
            <Button
              className={classes.topSpacing}
              color='primary'
              variant='contained'
              disabled={disableConfirm}
              onClick={() => addNewDataConnection()}
            >
              Skip
            </Button>
          </Tooltip>
        )
      }
    </>
  );
};
