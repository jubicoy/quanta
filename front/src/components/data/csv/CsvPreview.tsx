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
  DataConnection,
  CsvDataSeriesConfiguration,
  CsvDataConnectionConfiguration
} from '../../../types';
import { dataStyles } from '../DataStyles';
import { _DataConnectionConfiguratorContext } from '../DataImportPage';

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

export const CsvPreview = () => {
  const classes = dataStyles();
  const {
    dataConnection,
    dataSeries
  } = useContext(_DataConnectionConfiguratorContext);

  const csvConfigurations = dataSeries.configuration as CsvDataSeriesConfiguration;
  const finalDataConnection = dataConnection;
  if (!finalDataConnection) {
    return null;
  }

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
                      {(Object.keys(value) as (keyof CsvDataConnectionConfiguration)[])
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
          {csvConfigurations && (
            <TableBody>
              {(Object.keys(csvConfigurations) as (keyof CsvDataSeriesConfiguration)[]).map((key, i) => {
                // eslint-disable-next-line @typescript-eslint/no-explicit-any
                const value: any = csvConfigurations[key];
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
