import React, { useState, useCallback, useEffect } from 'react';
import {
  Table,
  TableCell,
  TableHead,
  TableRow,
  TableBody,
  TextField,
  Paper,
  Typography as T
} from '@material-ui/core';
import clsx from 'clsx';

import {
  TableRowItem,
  commonStyles
} from '../common';
import {
  WorkerDef,
  OutputColumn,
  Task,
  WorkerDefOutputColumnWithAlias,
  mapWorkerDefColumnWithAliasToOutputColumn,
  TaskType,
  mapWorkerDefOutputColumnWithAlias
} from '../../types';

interface WorkerDefProps {
  editable: boolean;
  creatingTask?: boolean;
  aliasesIsValid?: boolean[];
  aliasHelperTexts?: string[];
  workerDef: WorkerDef | undefined;
  setTaskOutputColumns: (outputColumns: OutputColumn[]) => void;
  task: Task;
}

export const OutputColumnConfiguration = ({
  editable,
  creatingTask = false,
  aliasesIsValid,
  aliasHelperTexts,
  workerDef,
  setTaskOutputColumns,
  task
}: WorkerDefProps) => {
  const common = commonStyles();

  const [workerDefOutputColumnsWithAlias, setWorkerDefOutputColumnsWithAlias] = useState<WorkerDefOutputColumnWithAlias[]>([]);

  const setOutputColumns = (
    outputColumns: WorkerDefOutputColumnWithAlias[]
  ) => {
    setTaskOutputColumns(
      outputColumns.map(mapWorkerDefColumnWithAliasToOutputColumn)
    );
  };

  const handleOutputAliasChange = (
    value: string,
    id: number
  ) => {
    const updatedOutputColumns = workerDefOutputColumnsWithAlias
      .map((outputColumn: WorkerDefOutputColumnWithAlias) => {
        if (outputColumn.id === id) {
          return ({
            ...outputColumn,
            alias: value
          });
        }
        return outputColumn;
      });

    setWorkerDefOutputColumnsWithAlias(updatedOutputColumns);
    setOutputColumns(
      updatedOutputColumns
    );
  };

  const resetOutputColumns = useCallback(
    () => {
      setWorkerDefOutputColumnsWithAlias(
        workerDef?.columns
          .filter(column => column.columnType === 'output')
          .map(mapWorkerDefOutputColumnWithAlias) ?? []
      );
    },
    [workerDef]
  );

  useEffect(
    () => {
      // Reset columns on data connection change
      if (editable) {
        resetOutputColumns();
      }
    },
    [resetOutputColumns, editable]
  );

  return (
    <>
      {
        task.taskType === TaskType.process
        && <>
          <Paper className={clsx(common.topMargin, common.bottomMargin)}>
            <T variant='h6' className={common.allMargin}>
              Output Columns Configuration
            </T>
            <Table>
              <TableBody>
                <TableRowItem
                  title='Worker Output Columns'
                  value={
                    <Table>
                      <TableHead>
                        <TableRow key={-2}>
                          <TableCell>Name</TableCell>
                          <TableCell>Class</TableCell>
                          <TableCell>Description</TableCell>
                          { (editable && creatingTask) && <TableCell>Alias</TableCell> }
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {(editable && creatingTask) ? workerDefOutputColumnsWithAlias
                          .sort((a, b) => a.index - b.index)
                          .map(
                            (outputColumn, outputIndex) => (
                              <TableRow key={outputIndex}>
                                <TableCell>{outputColumn.name}</TableCell>
                                <TableCell>{outputColumn.valueType.className}</TableCell>
                                <TableCell>{outputColumn.description}</TableCell>
                                <TableCell style={{
                                  minWidth: '440px'
                                // '440px' = width of input field with longest helper text
                                // So field don't resize repeatedly
                                }}>
                                  <TextField
                                    fullWidth
                                    error={
                                      aliasesIsValid ? !aliasesIsValid[outputIndex] : false
                                    }
                                    helperText={
                                      aliasHelperTexts ? aliasHelperTexts[outputIndex] : ''
                                    }
                                    value={outputColumn.alias || ''}
                                    onChange={e => handleOutputAliasChange(e.target.value as string, outputColumn.id)} />
                                </TableCell>
                              </TableRow>
                            )
                          )
                          : workerDef?.columns.filter(column => column.columnType === 'output')
                              .sort((a, b) => a.index - b.index)
                              .map((outputColumn, outputIndex) => (
                                <TableRow key={outputIndex}>
                                  <TableCell>{outputColumn.name}</TableCell>
                                  <TableCell>{outputColumn.valueType.className}</TableCell>
                                  <TableCell>{outputColumn.description}</TableCell>
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
