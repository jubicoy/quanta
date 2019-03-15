import React from 'react';

import {
  Icon,
  Table,
  TableCell,
  TableBody,
  TableHead,
  TableRow
} from '@material-ui/core';

import {
  ColumnSelector
} from '../../types';

interface Props {
  columnSelectors: ColumnSelector[];
}

export default ({ columnSelectors }: Props) => (
  <Table>
    <TableHead>
      <TableRow>
        <TableCell component='th' colSpan={4}> Column Selectors</TableCell>
      </TableRow>
      <TableRow>
        <TableCell component='th'>Column Name</TableCell>
        <TableCell component='th'>Aggregation</TableCell>
        <TableCell component='th'>Alias</TableCell>
        <TableCell component='th'>Format</TableCell>
        <TableCell component='th'>Nullable</TableCell>
        <TableCell component='th'>Class</TableCell>
      </TableRow>
    </TableHead>
    <TableBody>
      {columnSelectors.map((columnSelector, i) => (
        <TableRow key={i}>
          <TableCell>{columnSelector.columnName}</TableCell>
          <TableCell>{columnSelector.modifier}</TableCell>
          <TableCell>{columnSelector.alias}</TableCell>
          <TableCell>{columnSelector.type.format}</TableCell>
          <TableCell>
            <Icon>
              {columnSelector.type.nullable ? 'check_circle' : 'block_circle'}
            </Icon>
          </TableCell>
          <TableCell>{columnSelector.type.className}</TableCell>
        </TableRow>
      ))}
    </TableBody>
  </Table>
);
