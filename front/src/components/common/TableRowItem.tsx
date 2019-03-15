import React from 'react';
import {
  TableRow,
  TableCell,
  Tooltip
} from '@material-ui/core';

import { commonStyles } from '.';

interface RowProps {
  title: string;
  value?: string | React.ReactNode;
  tooltip?: string;
}

export const TableRowItem = ({
  title,
  value,
  tooltip
}: RowProps) => {
  const common = commonStyles();

  if (value === undefined) {
    return (
      <TableRow>
        <Tooltip title={tooltip || ''}>
          <TableCell
            component='th'
            scope='row'
          >
            {title}
          </TableCell>
        </Tooltip>
        <TableCell />
      </TableRow>
    );
  }

  return (
    <TableRow>
      <Tooltip title={tooltip || ''} interactive arrow>
        <TableCell
          className={common.previewTableCell}
          component='th'
          scope='row'
        >
          {title}
        </TableCell>
      </Tooltip>
      <TableCell className={common.previewTableLast}>{value}</TableCell>
    </TableRow>
  );
};
