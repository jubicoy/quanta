import React, { useState, useCallback, useEffect } from 'react';
import {
  MenuItem,
  Select,
  Table,
  TableCell,
  TableHead,
  TableRow,
  TableBody,
  Paper,
  Typography as T
} from '@material-ui/core';
import clsx from 'clsx';

import {
  TableRowItem,
  commonStyles
} from '../common';
import { useDataConnection } from '../../hooks';
import {
  WorkerDef,
  ColumnSelector,
  Column,
  isSupportedType,
  TIME_SERIES_MODIFIERS,
  supportedModifiers,
  Task,
  WorkerDefInputColumnWithSelector,
  TaskType,
  DataSeries
} from '../../types';
import { DataConnectionConfiguration } from './DataConnectionConfiguration';

interface WorkerDefProps {
  editable: boolean;
  creatingTask?: boolean;
  workerDef: WorkerDef | undefined;
  seriesKey?: string;
  setTaskColumnSelectors: (columnSelectors: ColumnSelector[]) => void;
  task: Task;
  setTask: (task: Task) => void;
  dataConnectionParam: number;
  setColumnForSyncTask: (columns: ColumnSelector[]) => void;
  workerDefInputColumnsWithSelector: WorkerDefInputColumnWithSelector[];
  setWorkerDefInputColumnsWithSelector: (workerDefInputColumnsWithSelector: WorkerDefInputColumnWithSelector[]) => void;
}

export const InputColumnConfiguration = ({
  editable,
  creatingTask = false,
  workerDef,
  seriesKey,
  setTaskColumnSelectors,
  task,
  setTask,
  dataConnectionParam,
  setColumnForSyncTask,
  workerDefInputColumnsWithSelector,
  setWorkerDefInputColumnsWithSelector
}: WorkerDefProps) => {
  const common = commonStyles();

  const [dataConnectionId, setDataConnectionId] = useState<number>(dataConnectionParam || -1);
  const { dataConnection } = useDataConnection(dataConnectionId);
  const [dataSeries, setDataSeries] = useState<DataSeries | null>(null);

  const taskTypeChange = useCallback(
    () => {
      if (task.taskType === TaskType.sync) {
        // Unset the DataConnection if taskType sync is selected with non-JDBC DataConnection
        if (dataConnection && dataConnection?.type !== 'JDBC') {
          setDataConnectionId(-1);
        }
        setTask(({
          ...task,
          workerDef: undefined,
          columnSelectors: [],
          outputColumns: []
        }));
      }
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [dataConnection, task.taskType]
  );

  const createColumnSelectorForSyncTask = useCallback(
    (): ColumnSelector[] => {
      if (dataSeries) {
        const connectionColumns = dataSeries?.columns.map(column => ({
          ...column,
          series: dataSeries
        })) ?? [];

        return connectionColumns
          .filter(({ index }) => index === 0)
          .map(({ index, name, type }) => ({
            id: -1,
            columnIndex: index,
            columnName: name,
            type: type,
            series: dataSeries
          }));
      }
      return [];
    },
    [dataSeries]
  );

  useEffect(
    () => {
      taskTypeChange();
    },
    [taskTypeChange]
  );

  useEffect(
    () => {
      if (task.taskType === TaskType.sync) {
        setColumnForSyncTask(
          createColumnSelectorForSyncTask()
        );
      }
    },
    [task.taskType, createColumnSelectorForSyncTask, setColumnForSyncTask]
  );

  const getSupportedColumnsForInputColumn = (
    inputColumn: WorkerDefInputColumnWithSelector
  ): Column[] => {
    if (dataSeries) {
      return dataSeries.columns
        .filter(column => isSupportedType(column.type, inputColumn.valueType));
    }
    return [];
  };

  const handleInputColumnChange = (
    value: Column | null | undefined,
    id: number
  ) => {
    const updatedInputColumns = workerDefInputColumnsWithSelector
      .map((inputColumn: WorkerDefInputColumnWithSelector) => {
        if (inputColumn.id === id) {
          return ({
            ...inputColumn,
            selectedColumn: value ?? undefined,
            series: dataSeries
          });
        }
        return inputColumn;
      });

    setWorkerDefInputColumnsWithSelector(updatedInputColumns);
  };

  const handleInputColumnModifierChange = (
    modifier: TIME_SERIES_MODIFIERS,
    id: number
  ) => {
    const updatedInputColumns = workerDefInputColumnsWithSelector
      .map((inputColumn: WorkerDefInputColumnWithSelector) => {
        if (inputColumn.id === id) {
          return ({
            ...inputColumn,
            modifier: modifier
          });
        }
        return inputColumn;
      });

    setWorkerDefInputColumnsWithSelector(updatedInputColumns);
  };

  return (
    <>
      {
        task.taskType === TaskType.process
        && <>
          <Paper className={clsx(common.topMargin, common.bottomMargin)}>
            <T variant='h6' className={common.allMargin}>
              {`Input Columns Configuration:  ${seriesKey || ''}`}
            </T>
            <Table>
              <TableBody>
                <TableRowItem
                  title='Series Key'
                  value={(editable && creatingTask)
                    ? seriesKey
                    : `${workerDef?.name ?? ''}`}
                />
                <DataConnectionConfiguration
                  task={task}
                  setTask={setTask}
                  dataConnectionId={dataConnectionId}
                  setDataConnectionId={setDataConnectionId}
                  dataSeries={dataSeries}
                  setDataSeries={setDataSeries}
                />
                <TableRowItem
                  title='Worker Input Columns'
                  value={
                    <Table>
                      <TableHead>
                        <TableRow key={-1}>
                          <TableCell>Name</TableCell>
                          <TableCell>Class</TableCell>
                          <TableCell>Description</TableCell>
                          { (editable && creatingTask) && <TableCell>Aggregation</TableCell> }
                          { (editable && creatingTask) && <TableCell>Selected column</TableCell> }
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {(editable && creatingTask)
                          ? workerDefInputColumnsWithSelector
                            .filter(col => col.seriesKey === seriesKey)
                            .sort((a, b) => a.index - b.index)
                            .map((inputColumn, inputIndex) => (
                              <TableRow key={inputIndex}>
                                <TableCell>{inputColumn.name}</TableCell>
                                <TableCell>{inputColumn.valueType.className}</TableCell>
                                <TableCell>{inputColumn.description}</TableCell>
                                <TableCell>
                                  {editable && (
                                    inputIndex !== 0 && (
                                      <Select
                                        fullWidth
                                        value={
                                          inputColumn.modifier
                                            ? inputColumn.modifier
                                            : ''
                                        }
                                        onChange={e => {
                                          const value = e.target.value as TIME_SERIES_MODIFIERS;
                                          handleInputColumnModifierChange(value, inputColumn.id);
                                        }}
                                        inputProps={{ id: 'task-connection-input-column-modifier' }}
                                      >
                                        {
                                          dataSeries
                                            ? [
                                              <MenuItem key={-1} value={undefined}>-</MenuItem>,
                                              supportedModifiers(inputColumn.valueType)
                                                .map((modifier, idx) => (
                                                  <MenuItem key={idx} value={modifier}>{modifier}</MenuItem>
                                                ))
                                            ]
                                            : [<MenuItem key={-1} value={undefined}>-</MenuItem>]
                                        }
                                      </Select>
                                    ))
                                  }
                                </TableCell>
                                <TableCell>
                                  <Select
                                    required
                                    fullWidth
                                    value={
                                      inputColumn.selectedColumn
                                        ? inputColumn.selectedColumn.id
                                        : -1
                                    }
                                    onChange={e => {
                                      const value = dataSeries?.columns
                                        .find(column => column.id === e.target.value as number);
                                      handleInputColumnChange(value, inputColumn.id);
                                    }}
                                    inputProps={{ id: 'task-connection-input-column-select' }}
                                  >
                                    {
                                      dataSeries
                                        ? [
                                          <MenuItem key={-1} value={-1}>Unset</MenuItem>,
                                          getSupportedColumnsForInputColumn(inputColumn)
                                            .map((column, idx) => (
                                              <MenuItem key={idx} value={column.id}>{column.name}</MenuItem>
                                            ))
                                        ]
                                        : [<MenuItem key={-1} value={-1}>Unset</MenuItem>]
                                    }
                                  </Select>
                                </TableCell>
                              </TableRow>
                            ))
                          : workerDef?.columns
                              .filter(column => column.seriesKey === seriesKey)
                              .filter(column => column.columnType === 'input')
                              .sort((a, b) => a.index - b.index)
                              .map((inputColumn, inputIndex) => (
                                <TableRow key={inputIndex}>
                                  <TableCell>{inputColumn.name}</TableCell>
                                  <TableCell>{inputColumn.valueType.className}</TableCell>
                                  <TableCell>{inputColumn.description}</TableCell>
                                </TableRow>
                              ))
                        }
                      </TableBody>
                    </Table>
                  }
                />
              </TableBody>
            </Table>
          </Paper>
        </>
      }
    </>
  );
};
