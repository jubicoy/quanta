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

export default ({ columnSelectors }: Props) => {
  const seriesKeys = [...new Set(columnSelectors.map(col => col.workerDefColumn?.seriesKey))]
    .filter(key => key !== undefined);

  return (
    <>
      {
        seriesKeys.map((seriesKey, index) => (
          <Table key={`${seriesKey}-${index}`}>
            <TableHead>
              <TableRow>
                <TableCell component='th' colSpan={6}> Column Selectors - {seriesKey}</TableCell>
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
              {columnSelectors
                .filter(col => col.workerDefColumn?.seriesKey === seriesKey)
                .map((columnSelector, i) => (
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
        ))
      }
    </>
  );
};
