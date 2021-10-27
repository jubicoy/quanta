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
  chartInterval: string;
  setChartInterval: (interval: string) => void ;
}

const INTERVAL_SECONDS = {
  'None': '0',
  '15 minutes': '15m',
  '1 hour': '1h',
  '6 hours': '6h',
  '12 hours': '12h',
  '1 day': '1d',
  '3 days': '3d',
  '1 week': '1w',
  '2 weeks': '2w',
  '1 month': '1M',
  '1 year': '1y'
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
