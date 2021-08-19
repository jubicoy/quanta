import React, { useContext } from 'react';

import {
  Paper,
  Table,
  TableRow,
  TableHead,
  TableBody,
  TableCell,
  Typography as T
} from '@material-ui/core';
import Chip from '@material-ui/core/Chip';

import {
  ImportWorkerDataSeriesConfiguration,
  ImportWorkerDataConnectionConfiguration,
  DataConnection
} from '../../../types';

import { dataStyles } from '../DataStyles';

import { _DataConnectionConfiguratorContext } from '../../context';

interface RowProps {
  title: string;
  value?: string;
}

const TableRowItem = ({
  title,
  value
}: RowProps) => {
  const classes = dataStyles();

  if (value === undefined) {
    return (
      <TableRow>
        <TableCell
          component='th'
          scope='row'
          style={{
            textTransform: 'capitalize'
          }}
        >
          {title}
        </TableCell>
        <TableCell />
      </TableRow>
    );
  }

  return (
    <TableRow>
      <TableCell
        className={classes.previewTableCell}
        component='th'
        scope='row'
        style={{
          textTransform: 'capitalize'
        }}
      >
        {title}
      </TableCell>
      <TableCell
        className={classes.previewTableLast}
        style={{
          textTransform: 'capitalize'
        }}
      >
        {value}
      </TableCell>
    </TableRow>
  );
};

export const ImportWorkerPreview = () => {
  const classes = dataStyles();
  const {
    dataConnection,
    dataSeries
  } = useContext(_DataConnectionConfiguratorContext);

  const importworkerConfigurations = dataSeries.configuration as ImportWorkerDataSeriesConfiguration;
  const finalDataConnection = dataConnection;
  if (!finalDataConnection) {
    return null;
  }

  return (
    <>
      <div className={classes.header}>
        <T component='span' variant='h5'>DataConnection configuration</T>
        {dataSeries?.dataConnection?.tags.map((option: string, index: number) =>
          <Chip style={{ margin: '5px' }} key={index} variant='outlined' label={option} />
        )}
      </div>
      <Paper>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Key</TableCell>
              <TableCell>Value</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {(Object.keys(finalDataConnection) as (keyof DataConnection)[])
              .slice()
              .filter(key => key !== 'series')
              .map((key, i) => {
                // eslint-disable-next-line @typescript-eslint/no-explicit-any
                const value: any = finalDataConnection[key];
                if (typeof value === 'object') {
                  return (
                    <>
                      <TableRowItem title={key} />
                      {(Object.keys(value) as (keyof ImportWorkerDataConnectionConfiguration)[])
                        .map((d, j) => (
                          <TableRowItem key={`${i}-${j}`} title={d} value={value[d]} />
                        ))
                      }
                    </>
                  );
                }
                else {
                  return (
                    <TableRowItem key={i} title={key} value={value} />
                  );
                }
              })}
          </TableBody>
        </Table>
      </Paper>
      <div className={classes.header}>
        <T component='span' variant='h5'>Preprocessing configurations</T>
      </div>
      <Paper>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Key</TableCell>
              <TableCell>Value</TableCell>
            </TableRow>
          </TableHead>
          {importworkerConfigurations && (
            <TableBody>
              {(Object.keys(importworkerConfigurations) as (keyof ImportWorkerDataSeriesConfiguration)[]).filter(k => k !== 'parameters').map((key, i) => {
                // eslint-disable-next-line @typescript-eslint/no-explicit-any
                const value: any = importworkerConfigurations[key];
                return (
                  <TableRowItem
                    key={i}
                    title={key}
                    value={value ? value.toString() : ''}
                  />
                );
              })}
            </TableBody>
          )}
        </Table>
      </Paper>
      <div className={classes.header}>
        <T component='span' variant='h5'>Parameters</T>
      </div>
      <Paper>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Name</TableCell>
              <TableCell>Value</TableCell>
            </TableRow>
          </TableHead>
          {importworkerConfigurations.parameters && (
            <TableBody>
              {(importworkerConfigurations as ImportWorkerDataSeriesConfiguration).parameters.map((p: any, i) => {
                // eslint-disable-next-line @typescript-eslint/no-explicit-any
                return (
                  <TableRowItem
                    key={i}
                    title={p.name}
                    value={p.value ? p.value : ''}
                  />
                );
              })}
            </TableBody>
          )}
        </Table>
      </Paper>
    </>
  );
};
