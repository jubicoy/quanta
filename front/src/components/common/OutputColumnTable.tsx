import React from 'react';

import {
  Table,
  TableCell,
  TableBody,
  TableHead,
  TableRow
} from '@material-ui/core';

import {
  OutputColumn
} from '../../types';

interface Props {
  outputColumns: OutputColumn[];
}

export default ({ outputColumns }: Props) => (
  <Table>
    <TableHead>
      <TableRow>
        <TableCell component='th' colSpan={4}> Output Columns</TableCell>
      </TableRow>
      <TableRow key={-1}>
        <TableCell>Alias</TableCell>
        <TableCell>Column name</TableCell>
      </TableRow>
    </TableHead>
    <TableBody>
      {outputColumns
        .map((outputColumn, index) => (
          <TableRow key={index}>
            <TableCell>{outputColumn.alias}</TableCell>
            <TableCell>{outputColumn.columnName}</TableCell>
          </TableRow>
        ))
      }
    </TableBody>
  </Table>
);
