import React, { useState, useCallback, useEffect, useMemo } from 'react';
import {
  Button,
  Icon,
  MenuItem,
  Select,
  Table,
  TableCell,
  TableHead,
  TableRow,
  TableBody,
  TextField
} from '@material-ui/core';

import { TableRowItem } from '../common';
import {
  useWorkerDefs
} from '../../hooks';
import {
  WorkerDef,
  WorkerType,
  DataConnection,
  ColumnSelector,
  Column,
  isSupportedType,
  TIME_SERIES_MODIFIERS,
  supportedModifiers,
  OutputColumn,
  Task,
  WorkerDefInputColumnWithSelector,
  WorkerDefOutputColumnWithAlias,
  mapWorkerDefInputColumnWithSelector,
  mapWorkerDefInputColumnWithoutSelector,
  mapWorkerDefOutputColumnWithAlias,
  mapWorkerDefColumnToOutputColumn,
  mapWorkerDefColumnWithAliasToOutputColumn,
  Parameter,
  WorkerParameter
} from '../../types';

interface WorkerDefProps {
  editable: boolean;
  creatingTask?: boolean;
  aliasesIsValid?: boolean[];
  aliasHelperTexts?: string[];
  workerDef: WorkerDef | undefined;
  cronTrigger: string | null;
  setCronTrigger: (set: string | null) => void;
  taskTrigger: number | null;
  setTaskTrigger: (set: number | null) => void;
  dataConnection: DataConnection | null | undefined;
  setTaskColumnSelectors: (columnSelectors: ColumnSelector[]) => void;
  setTaskOutputColumns: (outputColumns: OutputColumn[]) => void;
  setWorkerDef: (set: WorkerDef, outputColumns: OutputColumn[]) => void;
  parameters?: Parameter[];
  setParameters: (parameters?: Parameter[]) => void;
  parametersIsValid?: boolean[];
  parametersHelperTexts?: string[];
  tasks: Task[] | undefined;
  triggersAreValid?: boolean;
  isCronTriggerValid?: boolean;
  cronHelperText?: string;
}

const INPUT_TYPE = {
  'java.lang.String': 'string',
  'java.lang.Double': 'number',
  'java.time.Instant': 'datetime-local',
  'java.lang.Long': 'number'
};

export const WorkerDefConfiguration = ({
  editable,
  creatingTask = false,
  aliasesIsValid,
  aliasHelperTexts,
  workerDef,
  cronTrigger,
  setCronTrigger,
  taskTrigger,
  setTaskTrigger,
  dataConnection,
  setTaskColumnSelectors,
  setTaskOutputColumns,
  setWorkerDef,
  parameters,
  setParameters,
  parametersIsValid,
  parametersHelperTexts,
  tasks,
  triggersAreValid,
  isCronTriggerValid,
  cronHelperText
}: WorkerDefProps) => {
  const workerDefQuery = useMemo(() => ({ notDeleted: true }), []);
  const { workerDefs } = useWorkerDefs(workerDefQuery);
  const [workerDefInputColumnsWithSelector, setWorkerDefInputColumnsWithSelector] = useState<WorkerDefInputColumnWithSelector[]>([]);
  const [workerDefOutputColumnsWithAlias, setWorkerDefOutputColumnsWithAlias] = useState<WorkerDefOutputColumnWithAlias[]>([]);

  const workerOptions = workerDefs
    ? [
      <MenuItem key={-1} value={-1}>Unset</MenuItem>,
      ...workerDefs.map((conn, idx) => (
        <MenuItem key={idx} value={conn.id}>{conn.name}</MenuItem>
      ))
    ]
    : [<MenuItem key={-1} value={-1}>Unset</MenuItem>];

  const taskTriggerList = tasks?.map(({ id, name }) => ({ id, name }));

  let parametersList: WorkerParameter[] = workerDef?.parameters || [];

  parametersList = parametersList.map(parameter => {
    const taskParameter = parameters?.find(p => p.name === parameter.name);
    if (taskParameter) {
      return {
        ...parameter,
        id: taskParameter.id,
        defaultValue: taskParameter.value
      };
    }
    return parameter;
  });

  function getSupportedColumnsForInputColumn (
    dataConnection: DataConnection | null | undefined,
    inputColumn: WorkerDefInputColumnWithSelector
  ): Column[] {
    if (dataConnection && dataConnection.series.length > 0) {
      return dataConnection.series[0].columns
        .filter(column => isSupportedType(column.type, inputColumn.type));
    }
    return [];
  }

  const mapWorkerDefColumnToColumnSelector = (
    workerDefColumns: WorkerDefInputColumnWithSelector[]
  ): ColumnSelector[] => {
    if (dataConnection && dataConnection.series.length > 0) {
      return workerDefColumns
        .filter(({ selectedColumn }) => selectedColumn)
        .map(workerDefColumn => {
          if (workerDefColumn.selectedColumn) {
            return {
              id: -1,
              columnIndex: workerDefColumn.selectedColumn.index,
              columnName: workerDefColumn.selectedColumn.name,
              type: workerDefColumn.selectedColumn.type,
              alias: workerDefColumn.alias,
              modifier: workerDefColumn.modifier,
              workerDefColumn: mapWorkerDefInputColumnWithoutSelector(workerDefColumn),
              series: dataConnection.series[0]
            };
          }
          else {
            throw new Error('Trying to map input column to column selector without selected column');
          }
        });
    }
    return [];
  };

  function setOutputColumns (
    outputColumns: WorkerDefOutputColumnWithAlias[]
  ) {
    setTaskOutputColumns(
      outputColumns.map(mapWorkerDefColumnWithAliasToOutputColumn)
    );
  }

  function setColumnSelectors (
    inputColumns: WorkerDefInputColumnWithSelector[]
  ) {
    setTaskColumnSelectors(
      mapWorkerDefColumnToColumnSelector(
        inputColumns
      )
    );
  }

  function setColumnsOnWorkerDefChange (
    selectedWorkerDef: WorkerDef | null | undefined
  ) {
    setWorkerDefInputColumnsWithSelector(
      selectedWorkerDef?.columns
        .filter(column => column.columnType === 'input')
        .map(mapWorkerDefInputColumnWithSelector) ?? []
    );

    setWorkerDefOutputColumnsWithAlias(
      selectedWorkerDef?.columns
        .filter(column => column.columnType === 'output')
        .map(mapWorkerDefOutputColumnWithAlias) ?? []
    );
  }

  const handleInputColumnChange = (
    value: Column | null | undefined,
    inputWorkerDefColumn: WorkerDefInputColumnWithSelector
  ) => {
    const updatedInputColumns = workerDefInputColumnsWithSelector
      .map((inputColumn: WorkerDefInputColumnWithSelector) => {
        if (inputColumn.id === inputWorkerDefColumn.id) {
          inputColumn.selectedColumn = value ?? undefined;
        }
        return inputColumn;
      });

    setWorkerDefInputColumnsWithSelector(updatedInputColumns);
    setColumnSelectors(
      updatedInputColumns
    );
  };

  const handleInputColumnModifierChange = (
    modifier: TIME_SERIES_MODIFIERS,
    id: number
  ) => {
    const updatedInputColumns = workerDefInputColumnsWithSelector
      .map((inputColumn: WorkerDefInputColumnWithSelector) => {
        if (inputColumn.id === id) {
          inputColumn.modifier = modifier;
        }
        return inputColumn;
      });

    setWorkerDefInputColumnsWithSelector(updatedInputColumns);
    setColumnSelectors(
      updatedInputColumns
    );
  };

  const handleOutputAliasChange = (
    value: string,
    id: number
  ) => {
    const updatedOutputColumns = workerDefOutputColumnsWithAlias
      .map((outputColumn: WorkerDefOutputColumnWithAlias) => {
        if (outputColumn.id === id) {
          outputColumn.alias = value;
        }
        return outputColumn;
      });

    setWorkerDefOutputColumnsWithAlias(updatedOutputColumns);
    setOutputColumns(
      updatedOutputColumns
    );
  };

  const resetInputColumns = useCallback(
    () => {
      setWorkerDefInputColumnsWithSelector(
        workerDef?.columns
          .filter(column => column.columnType === 'input')
          .map(mapWorkerDefInputColumnWithSelector) ?? []
      );
    },
    [workerDef]
  );

  useEffect(
    () => {
      // Reset input columns on data connection change
      if (editable) {
        resetInputColumns();
      }
    },
    [resetInputColumns, dataConnection, editable]
  );

  useEffect(
    () => {
      if (parameters && parameters.length !== 0) return;
      if (parametersList.length === 0) return;

      const taskParameters = parametersList.map(parameter => ({
        id: parameter.id,
        name: parameter.name,
        value: parameter.defaultValue
      }));
      setParameters(taskParameters);
    }, [parametersList, parameters, setParameters]
  );

  const handleParametersValueChange = (
    name: string,
    value: string
  ) => {
    if (parameters && parameters.length !== 0) {
      const updatedParameters = parameters.map(parameter => {
        if (parameter.name === name) {
          return {
            ...parameter,
            value: value || null
          };
        }
        return parameter;
      });
      setParameters(updatedParameters);
    }
    else {
      if (workerDef) {
        if (workerDef.parameters) {
          const updatedParameters = workerDef.parameters
            .map(parameter => {
              if (parameter.name === name) {
                return {
                  id: -1,
                  name: parameter.name,
                  value: value || null
                };
              }
              return {
                id: -1,
                name: parameter.name,
                value: parameter.defaultValue || null
              };
            });
          setParameters(updatedParameters);
        }
      }
    }
  };

  return (
    <>
      <TableRowItem
        title='Worker Definition'
        value={(editable && creatingTask)
          ? (
            <Select
              fullWidth
              value={workerDef?.id || -1}
              onChange={e => {
                const value = workerDefs && workerDefs.find(def => def.id === e.target.value as number);
                setWorkerDef(
                  value || {
                    id: -1,
                    type: 'Detect' as WorkerType,
                    name: '',
                    description: '',
                    parameters: undefined,
                    columns: []
                  },
                  value?.columns.filter(column => column.columnType === 'output')
                    .map(mapWorkerDefColumnToOutputColumn)
                    .sort((a, b) => a.index - b.index) ?? []
                );
                setColumnsOnWorkerDefChange(value);
              }}
              inputProps={{ id: 'task-connection-select' }}
            >
              {workerOptions}
            </Select>
          )
          : `${workerDef?.name ?? ''}`}
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
              { (editable && creatingTask) ? workerDefInputColumnsWithSelector
                .sort((a, b) => a.index - b.index)
                .map((inputColumn, inputIndex) => (
                  <TableRow key={inputIndex}>
                    <TableCell>{inputColumn.name}</TableCell>
                    <TableCell>{inputColumn.type.className}</TableCell>
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
                              dataConnection
                                ? [
                                  <MenuItem key={-1} value={undefined}>-</MenuItem>,
                                  supportedModifiers(inputColumn.type)
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
                          const value = dataConnection && dataConnection.series[0].columns
                            .find(column => column.id === e.target.value as number);
                          handleInputColumnChange(value, inputColumn);
                        }}
                        inputProps={{ id: 'task-connection-input-column-select' }}
                      >
                        {
                          dataConnection
                            ? [
                              <MenuItem key={-1} value={-1}>Unset</MenuItem>,
                              getSupportedColumnsForInputColumn(dataConnection, inputColumn)
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
                : workerDef?.columns.filter(column => column.columnType === 'input')
                  .sort((a, b) => a.index - b.index)
                  .map((inputColumn, inputIndex) => (
                    <TableRow key={inputIndex}>
                      <TableCell>{inputColumn.name}</TableCell>
                      <TableCell>{inputColumn.type.className}</TableCell>
                      <TableCell>{inputColumn.description}</TableCell>
                    </TableRow>
                  ))
              }
            </TableBody>
          </Table>
        }
      />
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
                      <TableCell>{outputColumn.type.className}</TableCell>
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
                      <TableCell>{outputColumn.type.className}</TableCell>
                      <TableCell>{outputColumn.description}</TableCell>
                    </TableRow>
                  ))
              }
            </TableBody>
          </Table>
        }
      />
      <TableRowItem
        title='Parameters'
        value={
          <Table>
            <TableHead>
              <TableRow key={-3}>
                <TableCell>Name</TableCell>
                <TableCell>Description</TableCell>
                <TableCell>Type</TableCell>
                <TableCell>Nullable</TableCell>
                <TableCell>Value</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {editable ? parametersList.map((parameter, index) => (
                <TableRow key={parameter.name}>
                  <TableCell>{parameter.name}</TableCell>
                  <TableCell>{parameter.description}</TableCell>
                  <TableCell>{parameter.type}</TableCell>
                  <TableCell>
                    <Icon>
                      {parameter.nullable ? 'check_circle' : 'block_circle'}
                    </Icon>
                  </TableCell>
                  <TableCell>
                    <TextField
                      fullWidth
                      error={
                        parametersIsValid
                          ? !parametersIsValid[index] || false
                          : false
                      }
                      helperText={
                        parametersHelperTexts ? parametersHelperTexts[index] : ''
                      }
                      type={Object.values(INPUT_TYPE)[Object.keys(INPUT_TYPE).indexOf(parameter.type)] || 'string'}
                      value={parameter.defaultValue || ''}
                      onChange={e => handleParametersValueChange(
                        parameter.name,
                        e.target.value
                      )} />
                  </TableCell>
                </TableRow>
              ))
                : parametersList.map(param => (
                  <TableRow key={param.name}>
                    <TableCell>{param.name}</TableCell>
                    <TableCell>{param.description}</TableCell>
                    <TableCell>{param.type}</TableCell>
                    <TableCell>
                      <Icon>
                        {param.nullable ? 'check_circle' : 'block_circle'}
                      </Icon>
                    </TableCell>
                    <TableCell>{param.defaultValue}</TableCell>
                  </TableRow>
                ))
              }
            </TableBody>
          </Table>
        }
      />
      <TableRowItem
        title='Worker type'
        value={workerDef?.id !== -1 ? workerDef?.type : ''}
      />
      <TableRowItem title='Description' value={workerDef?.description ?? ''} />
      <TableRowItem
        title='Cron Trigger'
        value={editable
          ? (
            <div>
              <TextField
                style={{
                  width: '90%'
                }}
                error={!isCronTriggerValid || !triggersAreValid}
                helperText={cronHelperText}
                value={cronTrigger || ''}
                onChange={e => setCronTrigger(e.target.value.length > 0 ? e.target.value : null)}
              />
              <Button
                style={{
                  color: 'grey',
                  background: 'none',
                  width: '10%'
                }}
                onClick={() => {
                  setCronTrigger(null);
                }}
              >
                <Icon>close</Icon>
              </Button>
            </div>
          )
          : `${cronTrigger || ''}`}
      />
      <TableRowItem
        title='Task Trigger'
        value={editable
          ? (
            <div>
              <Select
                style={{
                  width: '90%'
                }}
                error={!triggersAreValid}
                value={taskTrigger || ''}
                onChange={e => {
                  setTaskTrigger(Number(e.target.value));
                }}
                inputProps={{ id: 'task-trigger' }}
              >
                {
                taskTriggerList?.map(({ id, name }) => (
                  <MenuItem key={id} value={id}>
                    {id + ': ' + name}
                  </MenuItem>
                )) ?? []
                }
              </Select>
              <Button
                style={{
                  color: 'grey',
                  background: 'none',
                  width: '10%'
                }}
                onClick={() => {
                  setTaskTrigger(null);
                }}
              >
                <Icon>close</Icon>
              </Button>
            </div>
          )
          : `${taskTrigger || ''}`}
        tooltip={'Task is launched whenever an Invocation of referred Task is completed'}
      />
    </>
  );
};
