import React, { useState, useMemo, useEffect } from 'react';
import {
  Select,
  Table,
  TableBody,
  TableHead,
  TableRow,
  TableCell,
  MenuItem
} from '@material-ui/core';

import {
  TableRowItem
} from '../common';
import {
  Task,
  TaskType,
  DataSeries
} from '../../types';
import {
  useDataConnection,
  useDataConnections
} from '../../hooks';

interface TaskProps {
  task: Task;
  setTask: (task: Task) => void;
  dataConnectionId: number;
  setDataConnectionId: (id: number) => void;
  dataSeries: DataSeries | null;
  setDataSeries: (dataSeries: DataSeries | null) => void;
}

export const DataConnectionConfiguration = ({
  task,
  setTask,
  dataConnectionId,
  setDataConnectionId,
  dataSeries,
  setDataSeries
}: TaskProps) => {
  const [connectionOptions, setConnectionOptions] = useState<JSX.Element[]>(
    [<MenuItem key={-1} value={-1}>Unset</MenuItem>]
  );
  const [seriesOptions, setSeriesOptions] = useState<JSX.Element[]>(
    [<MenuItem key={-1} value={-1}>Unset</MenuItem>]
  );

  const { dataConnection } = useDataConnection(dataConnectionId);
  const dataConnectionQuery = useMemo(() => ({ notDeleted: true }), []);
  const { dataConnections } = useDataConnections(dataConnectionQuery);

  useEffect(() => {
    if (!dataConnections) return;
    setConnectionOptions(
      [
        <MenuItem key={-1} value={-1}>Unset</MenuItem>,
        ...dataConnections
          .filter(({ type }) => task.taskType === TaskType.process || type === 'JDBC')
          .map((conn, idx) => (
            <MenuItem key={idx} value={conn.id}>{conn.name}</MenuItem>
          ))
      ]
    );
  }, [dataConnections, task.taskType]);

  useEffect(
    () => {
      if (!dataConnection) return;
      setSeriesOptions(
        [
          <MenuItem key={-1} value={-1}>Unset</MenuItem>,
          ...dataConnection.series.map(series => (
            <MenuItem key={series.id} value={series.id}>{series.name}</MenuItem>
          ))
        ]
      );
    },
    [dataConnection]
  );

  useEffect(() => {
    if (dataConnection && dataConnection.series.length > 0) {
      setDataSeries(dataConnection.series[0]);
    }
    else if (!dataConnection) {
      setDataSeries(null);
    }
  }, [dataConnection, setDataSeries]);

  const connectionColumns = dataSeries?.columns.map(column => ({
    ...column,
    series: dataSeries
  })) ?? [];

  const handleDataConnectionChange = (
    value: number
  ) => {
    setDataConnectionId(value);
    // Clear column selectors on data connection change
    setTask({
      ...task,
      columnSelectors: []
    });
  };

  return (
    <>
      <TableRowItem
        title={'Data Connection'}
        value={
          <Select
            fullWidth
            value={dataConnectionId || ''}
            onChange={e => {
              handleDataConnectionChange(e.target.value as number);
            }}
            inputProps={{ id: 'task-connection-select' }}
          >
            {connectionOptions}
          </Select>
        }
      />
      {dataConnection
        && dataConnection.series.length > 1
        && (
          <TableRowItem
            title={'Data Series'}
            value={
              <Select
                fullWidth
                value={dataSeries ? dataSeries.id : -1}
                onChange={e => {
                  const series = dataConnection && dataConnection.series.find(s => s.id === e.target.value as number);
                  setDataSeries(series || null);
                }}
                inputProps={{ id: 'task-series-select' }}
              >
                {seriesOptions}
              </Select>
            }
          />
        )}
      <TableRowItem
        title={''}
        value={
          <Table>
            <TableHead>
              <TableRow key={0}>
                <TableCell key={1}>Name</TableCell>
                <TableCell key={2}>Class</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {connectionColumns.map((column, i) => (
                <TableRow key={i}>
                  <TableCell>{column.name}</TableCell>
                  <TableCell>{column.type.className}</TableCell>
                  <TableCell />
                </TableRow>
              ))}
            </TableBody>
          </Table>
        }
      />
    </>
  );
};
