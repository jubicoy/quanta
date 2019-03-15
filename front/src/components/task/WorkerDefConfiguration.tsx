import React, { useState, useCallback, useEffect, useMemo } from 'react';
import {
  Button,
  Icon,
  Input,
  MenuItem,
  Select,
  Switch,
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
  WorkerParameter
} from '../../types';

interface ConfigProps {
  value: string | number | boolean;
  onChange: (set: string | number | boolean) => void;
}

const ConfigInput = ({
  value,
  onChange
}: ConfigProps) => {
  switch (typeof value) {
    case 'number':
      return (
        <Input
          fullWidth
          type='number'
          value={value}
          onChange={e => onChange(Number(e.target.value))}
        />
      );
    case 'string':
      return (
        <Input
          fullWidth
          value={value}
          onChange={e => onChange(e.target.value as string)}
        />
      );
    case 'boolean':
      return (
        <Switch
          color='primary'
          checked={value}
          onChange={e => onChange(e.target.checked)}
        />
      );
    default:
      return null;
  }
};

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
  configurations: Record<string, string | number | boolean>;
  onConfigChange: (set: Record<string, string | number | boolean>) => void;
  dataConnection: DataConnection | null | undefined;
  setTaskColumnSelectors: (columnSelectors: ColumnSelector[]) => void;
  setTaskOutputColumns: (outputColumns: OutputColumn[]) => void;
  setWorkerDef: (set: WorkerDef, outputColumns: OutputColumn[], additionalParams?: Record<string, WorkerParameter>) => void;
  additionalParams?: Record<string, WorkerParameter>;
  setAdditionalParams: (additionalParams: Record<string, WorkerParameter>) => void;
  tasks: Task[] | undefined;
  triggersAreValid?: boolean;
  isCronTriggerValid?: boolean;
  cronHelperText?: string;
}

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
  configurations,
  onConfigChange,
  dataConnection,
  setTaskColumnSelectors,
  setTaskOutputColumns,
  setWorkerDef,
  additionalParams,
  setAdditionalParams,
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
  const configList = Object.keys(configurations)
    .map(key => ({
      name: key,
      value: configurations[key]
    }))
    .filter(c => typeof c.value !== 'object');

  const additionalParamsList = additionalParams
    ? Object.keys(additionalParams).map(key => ({
      name: key,
      description: additionalParams[key].description,
      nullable: additionalParams[key].nullable,
      condition: additionalParams[key].condition ?? null,
      value: additionalParams[key].value ?? ''
    }))
    : [];

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

  const setConfig = (key: string, value: string | number | boolean) => {
    const newConfig = configurations;
    newConfig[key] = value;
    onConfigChange(newConfig);
  };

  const handleAdditionalParametersValueChange = (
    name: string,
    value: string
  ) => {
    if (additionalParams) {
      const updatedAdditionalParams = additionalParams;
      updatedAdditionalParams[name].value = value;
      setAdditionalParams(updatedAdditionalParams);
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
                    additionalParams: null,
                    columns: []
                  },
                  value?.columns.filter(column => column.columnType === 'output')
                    .map(mapWorkerDefColumnToOutputColumn)
                    .sort((a, b) => a.index - b.index) ?? [],
                  value?.additionalParams ?? undefined
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
        title='Additional Parameters'
        value={
          <Table>
            <TableHead>
              <TableRow key={-3}>
                <TableCell>Name</TableCell>
                <TableCell>Description</TableCell>
                <TableCell>Nullable</TableCell>
                <TableCell>Value</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {editable ? additionalParamsList.map(param => (
                <TableRow key={param.name}>
                  <TableCell>{param.name}</TableCell>
                  <TableCell>{param.description}</TableCell>
                  <TableCell>
                    <Icon>
                      {param.nullable ? 'check_circle' : 'block_circle'}
                    </Icon>
                  </TableCell>
                  <TableCell>
                    <TextField
                      fullWidth
                      value={param.value || undefined}
                      onChange={e => handleAdditionalParametersValueChange(
                        param.name,
                        e.target.value
                      )} />
                  </TableCell>
                </TableRow>
              ))
                : additionalParamsList.map(param => (
                  <TableRow key={param.name}>
                    <TableCell>{param.name}</TableCell>
                    <TableCell>{param.description}</TableCell>
                    <TableCell>
                      <Icon>
                        {param.nullable ? 'check_circle' : 'block_circle'}
                      </Icon>
                    </TableCell>
                    <TableCell>{param.value}</TableCell>
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
      {configList.map(config => (
        <TableRowItem
          key={config.name}
          title={config.name}
          value={editable
            ? (
              <ConfigInput
                value={config.value}
                onChange={(set) => setConfig(config.name, set)}
              />
            )
            : `${typeof config.value === 'boolean' ? config.value.valueOf() : config.value}`}
        />
      ))}
    </>
  );
};
