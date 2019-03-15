import React from 'react';

import {
  Icon,
  Table,
  TableCell,
  TableBody,
  TableHead,
  TableRow
} from '@material-ui/core';

import { Column } from '../../types';

interface Props {
  columns: Column[];
}
export default ({ columns }: Props) => (
  <Table>
    <TableHead>
      <TableRow>
        <TableCell component='th' colSpan={4}>Columns</TableCell>
      </TableRow>
      <TableRow>
        <TableCell component='th'>Name</TableCell>
        <TableCell component='th'>Format</TableCell>
        <TableCell component='th'>Nullable</TableCell>
        <TableCell component='th'>Class</TableCell>
      </TableRow>
    </TableHead>
    <TableBody>
      {columns.map((column, i) => (
        <TableRow key={i}>
          <TableCell>{column.name}</TableCell>
          <TableCell>{column.type.format}</TableCell>
          <TableCell>
            <Icon>
              {column.type.nullable ? 'check_circle' : 'block_circle'}
            </Icon>
          </TableCell>
          <TableCell>{column.type.className}</TableCell>
        </TableRow>
      ))}
    </TableBody>
  </Table>
);
