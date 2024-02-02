import React, { useContext } from 'react';

import {
  Grid,
  Table,
  TableBody,
  TableRow,
  TableCell,
  Typography,
  Button,
  Icon,
  TableHead,
  Collapse
} from '@material-ui/core';

import { TypeSelect } from './TypeSelect';
import { dataStyles } from './DataStyles';

import {
  Column
} from '../../types';

import clsx from 'clsx';
import { _DataConnectionConfiguratorContext } from '../context';

interface SampleTableProps {
  editableType: boolean; // Can types be edited
}

export const SampleTable = ({
  editableType
}: SampleTableProps) => {
  const classes = dataStyles();

  const {
    sampleData,
    sampleResponse
  } = useContext(_DataConnectionConfiguratorContext);

  const [editingIndex, setEditingIndex] = React.useState<number>(-1);

  let columnsRow: Column[] = [];
  let sampleDataRows: string[][] = [];

  if (sampleResponse && sampleData) {
    columnsRow = sampleResponse.dataSeries.columns;
    sampleDataRows = sampleData;
  }

  return (
    <Grid
      className={classes.sampleTableGrid}
      item
      xs={12}>
      <Typography
        className={clsx(classes.typography, classes.typeSelectStickies)}
        variant='body1'>
        Sample rows
      </Typography>
      {!sampleData
        ? (
          'No sample data'
        )
        : <>
          <Collapse
            className={classes.typeSelectStickies}
            in={editableType && editingIndex !== -1}
          >
            <TypeSelect
              editingIndex={editingIndex}
            />
          </Collapse>
          <Table>
            <TableHead>
              <SampleTableColumnHeaderRow
                key={0}
              >
                {
                  columnsRow.map((column) => {
                    return (
                      <SampleTableColumnHeaderCell
                        key={column.index}
                        editableType={editableType}
                        isEditing={column.index === editingIndex}
                        columnIndex={column.index}
                        setEditingIndex={column.index !== -1 ? (index) => setEditingIndex(index) : undefined}>
                        {column.name}
                      </SampleTableColumnHeaderCell>
                    );
                  })
                }
              </SampleTableColumnHeaderRow>
            </TableHead>
            <TableBody>
              {sampleDataRows.map(
                (dataRow, i) => {
                  return (
                    <TableRow key={i}>
                      {dataRow.map((data, j) =>
                        <TableCell key={j}>
                          {data}
                        </TableCell>
                      )}
                    </TableRow>
                  );
                }
              )}
            </TableBody>
          </Table>
        </>
      }
    </Grid>
  );
};

interface SampleTableColumnHeaderProps {
  children: React.ReactNode;
}

const SampleTableColumnHeaderRow = ({
  children
}: SampleTableColumnHeaderProps) => {
  return <React.Fragment>
    <TableRow>
      {children}
    </TableRow>
  </React.Fragment>;
};

interface SampleTableColumnHeaderCellProps {
  setEditingIndex?: (index: number) => void;
  children: React.ReactNode;
  columnIndex: number;
  isEditing: boolean;
  editableType: boolean;
}

const SampleTableColumnHeaderCell = ({
  editableType,
  setEditingIndex,
  children,
  columnIndex,
  isEditing
}: SampleTableColumnHeaderCellProps) => {
  const classes = dataStyles();

  return (
    <TableCell
      className={clsx(
        classes.headerCell,
        classes.headerRow,
        isEditing && classes.editMode)}>
      {editableType && setEditingIndex
        ? (
          <Button
            onClick={() => setEditingIndex(isEditing ? -1 : columnIndex)}
            disableRipple
          >
            {children}
            <Icon>settings</Icon>
          </Button>
        ) : children
      }
    </TableCell>
  );
};
