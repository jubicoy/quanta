import React from 'react';

import {
  Table,
  TableCell,
  TableBody,
  TableHead,
  TableRow
} from '@material-ui/core';

import {
  WorkerDefColumn
} from '../../types';

interface Props {
  workerColumns: WorkerDefColumn[];
}

export default ({ workerColumns }: Props) => (
  <Table>
    <TableHead>
      <TableRow key={-1}>
        <TableCell>Name</TableCell>
        <TableCell>Class</TableCell>
        <TableCell>Description</TableCell>
      </TableRow>
    </TableHead>
    <TableBody>
      {workerColumns
        .map((column, index) => (
          <TableRow key={index}>
            <TableCell>{column.name}</TableCell>
            <TableCell>{column.valueType.className}</TableCell>
            <TableCell>{column.description}</TableCell>
          </TableRow>
        ))
      }
    </TableBody>
  </Table>
);
