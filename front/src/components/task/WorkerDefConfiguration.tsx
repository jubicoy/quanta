import React, { useMemo, useEffect } from 'react';
import {
  MenuItem,
  Select,
  Table,
  TableBody,
  TableHead,
  TableRow,
  TableCell,
  Paper,
  Typography as T,
  Icon,
  TextField
} from '@material-ui/core';
import clsx from 'clsx';

import {
  TableRowItem,
  commonStyles
} from '../common';
import {
  useWorkerDefs
} from '../../hooks';
import {
  WorkerDef,
  WorkerType,
  OutputColumn,
  Task,
  mapWorkerDefColumnToOutputColumn,
  TaskType,
  Parameter,
  WorkerParameter
} from '../../types';

interface WorkerDefProps {
  editable: boolean;
  creatingTask?: boolean;
  workerDef: WorkerDef | undefined;
  setWorkerDef: (set: WorkerDef, outputColumns: OutputColumn[]) => void;
  parameters?: Parameter[];
  setParameters: (parameters?: Parameter[]) => void;
  parametersIsValid?: boolean[];
  parametersHelperTexts?: string[];
  task: Task;
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
  workerDef,
  setWorkerDef,
  parameters,
  setParameters,
  parametersIsValid,
  parametersHelperTexts,
  task
}: WorkerDefProps) => {
  const common = commonStyles();

  const workerDefQuery = useMemo(() => ({ notDeleted: true }), []);
  const { workerDefs } = useWorkerDefs(workerDefQuery);

  const workerOptions = workerDefs
    ? [
      <MenuItem key={-1} value={-1}>Unset</MenuItem>,
      ...workerDefs.map((conn, idx) => (
        <MenuItem key={idx} value={conn.id}>{conn.name}</MenuItem>
      ))
    ]
    : [<MenuItem key={-1} value={-1}>Unset</MenuItem>];

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
      {
        task.taskType === TaskType.process
      && <>
        <T variant='h5'>Worker Configuration</T>
        <Paper className={clsx(common.topMargin, common.bottomMargin)}>
          <Table>
            <TableBody>
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
                      }}
                      inputProps={{ id: 'task-connection-select' }}
                    >
                      {workerOptions}
                    </Select>
                  )
                  : `${workerDef?.name ?? ''}`}
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
              <TableRowItem
                title='Description'
                value={workerDef?.description ?? ''}
              />
            </TableBody>
          </Table>
        </Paper>
      </>
      }
    </>
  );
};
