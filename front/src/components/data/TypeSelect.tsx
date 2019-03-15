import React, { useContext } from 'react';

import {
  FormControl,
  FormControlLabel,
  Grid,
  InputLabel,
  MenuItem,
  Select,
  Switch,
  TextField
} from '@material-ui/core';

import {
  Type,
  Column,
  SupportedColumnClasses,
  DEFAULT_COLUMN
} from '../../types';
import { _DataConnectionConfiguratorContext } from './DataImportPage';

interface TypeProps {
  editingIndex: number;
}

export const TypeSelect = ({
  editingIndex
}: TypeProps) => {
  const {
    dataSeries,
    setDataSeries,
    sampleResponse
  } = useContext(_DataConnectionConfiguratorContext);

  if (!sampleResponse) return null;

  const originalColumns: Column[] = [...sampleResponse.dataSeries.columns];

  const selectedColumns: Column[] = JSON.parse(JSON.stringify(dataSeries.columns));

  const isSelected: boolean = selectedColumns.some(c => c.index === editingIndex);

  let column: Column;

  if (isSelected) {
    // Column is only readable if column is selected
    column = selectedColumns.filter(column => column.index === editingIndex)[0];
  }
  else {
    // Column is default
    column = originalColumns.filter(column => column.index === editingIndex)[0];
  }

  if (!column) {
    // Default value to keep values valid when transitioning out
    column = DEFAULT_COLUMN;
  }

  const handleColumnSelectChange = (checked: boolean) => {
    if (checked) {
      if (!sampleResponse) return;
      const newDataSeries = {
        ...dataSeries,
        columns: [
          ...dataSeries.columns,
          column
        ].sort((a, b) => {
          // Sort columns by index
          return (a.index - b.index);
        })
      };
      setDataSeries(newDataSeries);
    }
    else {
      const newDataSeries = {
        ...dataSeries,
        columns: selectedColumns.filter(
          (column) => (column.index !== editingIndex)
        )
      };
      setDataSeries(newDataSeries);
    }
  };

  const handleColumnTypeChange = (
    e: React.ChangeEvent<{
      name: string;
      value: unknown;
      checked?: boolean;
    }>
  ) => {
    const key = e.target.name;
    const value = e.target.value;

    let newColumns = JSON.parse(JSON.stringify(dataSeries.columns));

    const currentType: Type = selectedColumns.filter(column => column.index === editingIndex)[0].type;

    if (key === 'nullable') {
      const checked: boolean = e.target.checked as boolean;

      // Get in currently selected columns list the column with has
      // editingIndex in original columns list
      // (editingIndex is based on original columns list,
      // dataSeries.columns may have different index)

      newColumns = newColumns.map((column: Column) => {
        if (column.index === editingIndex) {
          column.type = {
            ...currentType,
            nullable: checked
          };
        }
        return column;
      });
    }
    else {
      newColumns = newColumns.map((column: Column) => {
        if (column.index === editingIndex) {
          column.type = {
            ...currentType,
            [key]: value
          };
        }
        return column;
      });
    }

    const newDataSeries = {
      ...dataSeries,
      columns: newColumns
    };

    setDataSeries(newDataSeries);
  };

  const handleColumnNameChange = (
    e: React.ChangeEvent<{
      value: string;
    }>
  ) => {
    const value = e.target.value;

    let newColumns = JSON.parse(JSON.stringify(dataSeries.columns));
    newColumns = newColumns.map((column: Column) => {
      if (column.index === editingIndex) {
        column.name = value;
      }
      return column;
    });

    const newDataSeries = {
      ...dataSeries,
      columns: newColumns
    };

    setDataSeries(newDataSeries);
  };

  return (
    <Grid container spacing={3} alignItems='flex-end'>
      <Grid item xs={1}>
        <FormControlLabel
          control={
            <Switch
              checked={isSelected}
              onChange={(_e, checked) => handleColumnSelectChange(checked)}
            />
          }
          label='Select'
          labelPlacement='top'
        />
      </Grid>
      <Grid item xs={2}>
        <TextField
          disabled={!isSelected}
          label='Name'
          margin='normal'
          fullWidth
          value={column.name}
          name='name'
          required
          onChange={(e) => handleColumnNameChange(e)}
        />
      </Grid>
      <Grid item xs={3}>
        <FormControl margin='normal' fullWidth>
          <InputLabel
            htmlFor='type-class'
            shrink
          >
            Class
          </InputLabel>
          <Select
            disabled={!isSelected}
            autoWidth
            value={column.type.className}
            name='className'
            // What type is event from Select onChange ?
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            onChange={(e: any) => handleColumnTypeChange(e)}
            inputProps={{ id: 'type-class' }}
          >
            {SupportedColumnClasses.map(type => <MenuItem key={type} value={type}>{type}</MenuItem>)}
          </Select>
        </FormControl>
      </Grid>
      <Grid item xs={3}>
        <TextField
          disabled={!isSelected}
          label='Format'
          margin='normal'
          fullWidth
          value={column.type.format || ''}
          name='format'
          onChange={(e) => handleColumnTypeChange(e)}
        />
      </Grid>
      <Grid item xs={2}>
        <FormControlLabel
          control={
            <Switch
              disabled={!isSelected}
              checked={column.type.nullable}
              name='nullable'
              onChange={(e) => handleColumnTypeChange(e)}
              inputProps={{ id: 'type-nullable' }}
            />
          }
          label='Nullable'
          labelPlacement='top'
        />
      </Grid>
    </Grid>
  );
};
