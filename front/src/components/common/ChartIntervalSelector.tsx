import React from 'react';

import {
  makeStyles,
  createStyles,
  TextField,
  MenuItem
} from '@material-ui/core';
import clsx from 'clsx';

const useStyles = makeStyles(() => {
  const height = 46;
  const borderStyle = {
    border: '1px solid rgba(0, 0, 0, .24)',
    borderRadius: '4px',
    '&:hover': {
      border: '1px solid rgba(0, 0, 0, 1)'
    }
  };
  return createStyles({
    toolbarInput: {
      minWidth: 120,
      '& .MuiInputBase-root': {
        height: height + 'px'
      },
      '& .MuiOutlinedInput-notchedOutline ': {
        ...borderStyle
      }
    }
  });
});

interface Props {
  styles?: string;
  intervalError: boolean;
  chartInterval: number;
  setChartInterval: (interval: string) => void ;
}

const INTERVAL_SECONDS = {
  'None': 0,
  '15 minutes': 15 * 60,
  '1 hour': 60 * 60,
  '6 hours': 60 * 60 * 6,
  '12 hours': 60 * 60 * 12,
  '1 day': 60 * 60 * 24,
  '3 days': 60 * 60 * 24 * 3,
  '1 week': 60 * 60 * 24 * 7,
  '2 weeks': 60 * 60 * 24 * 7 * 2,
  '1 month': 60 * 60 * 24 * 30,
  '3 months': 60 * 60 * 24 * 30 * 3
};

export default ({
  styles,
  intervalError,
  chartInterval,
  setChartInterval
}: Props) => {
  const classes = useStyles();

  return (
    <TextField
      className={clsx(classes.toolbarInput, styles)}
      select
      variant='outlined'
      error={intervalError}
      label='Interval'
      id='interval-select'
      value={chartInterval}
      onChange={(e) => setChartInterval(e.target.value)}
    >
      {
        Object.entries(INTERVAL_SECONDS).map(pair =>
          <MenuItem key={`menu-item-${pair[1]}`} value={pair[1]}>{pair[0]}</MenuItem>
        )
      }
    </TextField>
  );
};
