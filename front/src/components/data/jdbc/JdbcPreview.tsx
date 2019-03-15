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

import {
  JdbcDataSeriesConfiguration,
  JdbcDataConnectionConfiguration,
  DataConnection
} from '../../../types';

import { dataStyles } from '../DataStyles';
import { _DataConnectionConfiguratorContext } from '../DataImportPage';

interface RowProps {
  title: string;
  value?: string;
  capitalizeValue?: boolean;
}

const TableRowItem = ({
  title,
  value,
  capitalizeValue
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
          textTransform: capitalizeValue ? 'capitalize' : 'none'
        }}
      >
        {value}
      </TableCell>
    </TableRow>
  );
};

export const JdbcPreview = () => {
  const classes = dataStyles();
  const {
    dataSeries
  } = useContext(_DataConnectionConfiguratorContext);

  const finalDataConnection = dataSeries.dataConnection;
  if (!finalDataConnection) {
    return null;
  }
  const jdbcSeriesConfiguration = dataSeries.configuration as JdbcDataSeriesConfiguration;

  return (
    <>
      <div className={classes.header}>
        <T component='span' variant='h5'>DataConnection configuration</T>
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
                      {(Object.keys(value) as (keyof JdbcDataConnectionConfiguration)[])
                        .filter(configuration => configuration !== 'password' && configuration !== 'username')
                        .map(configuration => (
                          <TableRowItem title={configuration} value={value[configuration]} />
                        ))}
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
          {jdbcSeriesConfiguration && (
            <TableBody>
              {(Object.keys(jdbcSeriesConfiguration) as (keyof JdbcDataSeriesConfiguration)[]).map((key, i) => {
                // eslint-disable-next-line @typescript-eslint/no-explicit-any
                const value: any = jdbcSeriesConfiguration[key];
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
    </>
  );
};
