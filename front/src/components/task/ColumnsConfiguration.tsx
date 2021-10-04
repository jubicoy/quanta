import React, { useState, useCallback, useEffect, useMemo } from 'react';
import {
  Table,
  TableBody,
  Paper,
  Typography as T
} from '@material-ui/core';
import clsx from 'clsx';

import {
  commonStyles
} from '../common';
import { useDataConnection } from '../../hooks';
import {
  WorkerDef,
  ColumnSelector,
  OutputColumn,
  Task,
  TaskType,
  DataSeries,
  WorkerDefInputColumnWithSelector,
  mapWorkerDefInputColumnWithSelector,
  mapWorkerDefInputColumnWithoutSelector
} from '../../types';
import { DataConnectionConfiguration } from './DataConnectionConfiguration';
import { InputColumnConfiguration } from './InputColumnConfiguration';
import { OutputColumnConfiguration } from './OutputColumnConfiguration';

interface WorkerDefProps {
  editable: boolean;
  creatingTask?: boolean;
  aliasesIsValid?: boolean[];
  aliasHelperTexts?: string[];
  workerDef: WorkerDef | undefined;
  setTaskColumnSelectors: (columnSelectors: ColumnSelector[]) => void;
  setTaskOutputColumns: (outputColumns: OutputColumn[]) => void;
  setWorkerDef: (set: WorkerDef, outputColumns: OutputColumn[]) => void;
  task: Task;
  setTask: (task: Task) => void;
  dataConnectionParam: number;
  setColumnForSyncTask: (columns: ColumnSelector[]) => void;
}

export const ColumnsConfiguration = ({
  editable,
  creatingTask = false,
  aliasesIsValid,
  aliasHelperTexts,
  workerDef,
  setTaskColumnSelectors,
  setTaskOutputColumns,
  task,
  setTask,
  dataConnectionParam,
  setColumnForSyncTask
}: WorkerDefProps) => {
  const common = commonStyles();

  const [dataConnectionId, setDataConnectionId] = useState<number>(dataConnectionParam || -1);
  const { dataConnection } = useDataConnection(dataConnectionId);
  const [dataSeries, setDataSeries] = useState<DataSeries | null>(null);
  const [workerDefInputColumnsWithSelector, setWorkerDefInputColumnsWithSelector] = useState<WorkerDefInputColumnWithSelector[]>([]);

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

  const seriesKeyOptions = useMemo(
    () => {
      return [...new Set(
        workerDef?.columns
          .filter(col => col.seriesKey)
          .map(col => col.seriesKey)
      )];
    },
    [workerDef]
  );

  useEffect(
    () => {
      if (seriesKeyOptions.length !== 0) return;
      seriesKeyOptions.push('');
    },
    [seriesKeyOptions]
  );

  const resetInputColumns = useCallback(
    () => {
      setWorkerDefInputColumnsWithSelector(
        workerDef?.columns
          .filter(column => column.columnType === 'input')
          .map(mapWorkerDefInputColumnWithSelector) ?? []
      );
    },
    [workerDef, setWorkerDefInputColumnsWithSelector]
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

  useEffect(
    () => {
      // Reset columns on data connection change
      if (editable) {
        resetInputColumns();
      }
    },
    [resetInputColumns, editable]
  );

  const mapWorkerDefColumnToColumnSelector = (
    workerDefColumns: WorkerDefInputColumnWithSelector[]
  ): ColumnSelector[] => {
    return workerDefColumns
      .filter(({ selectedColumn }) => selectedColumn)
      .reduce((acc, cur) => {
        if (cur.selectedColumn && cur.series !== null) {
          acc.push({
            id: -1,
            columnIndex: cur.selectedColumn.index,
            columnName: cur.selectedColumn.name,
            type: cur.selectedColumn.type,
            modifier: cur.modifier,
            workerDefColumn: mapWorkerDefInputColumnWithoutSelector(cur),
            series: cur.series
          });
        }
        return acc;
      }, [] as ColumnSelector[]);
  };

  useEffect(
    () => {
      setTaskColumnSelectors(
        mapWorkerDefColumnToColumnSelector(workerDefInputColumnsWithSelector)
      );
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [workerDefInputColumnsWithSelector]
  );

  return (
    <>
      {
        task.taskType === TaskType.sync
        && <Paper className={clsx(common.topMargin, common.bottomMargin)}>
          <Table>
            <TableBody>
              <DataConnectionConfiguration
                task={task}
                setTask={setTask}
                dataConnectionId={dataConnectionId}
                setDataConnectionId={setDataConnectionId}
                dataSeries={dataSeries}
                setDataSeries={setDataSeries}
              />
            </TableBody>
          </Table>
        </Paper>
      }
      {
        task.taskType === TaskType.process
        && <>
          <T variant='h5'>Columns Configuration</T>
          <>
            {
              seriesKeyOptions.map(
                (seriesKey) => (
                  <InputColumnConfiguration
                    key={seriesKey}
                    editable={editable}
                    creatingTask={creatingTask}
                    workerDef={workerDef}
                    seriesKey={seriesKey}
                    setTaskColumnSelectors={setTaskColumnSelectors}
                    task={task}
                    setTask={setTask}
                    dataConnectionParam={dataConnectionParam}
                    setColumnForSyncTask={setColumnForSyncTask}
                    workerDefInputColumnsWithSelector={workerDefInputColumnsWithSelector}
                    setWorkerDefInputColumnsWithSelector={setWorkerDefInputColumnsWithSelector}
                  />
                )
              )
            }
          </>
          <OutputColumnConfiguration
            editable={editable}
            creatingTask={creatingTask}
            aliasesIsValid={aliasesIsValid}
            aliasHelperTexts={aliasHelperTexts}
            workerDef={workerDef}
            setTaskOutputColumns={setTaskOutputColumns}
            task={task}
          />
        </>
      }
    </>
  );
};
