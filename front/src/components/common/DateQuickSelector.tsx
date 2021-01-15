import React, { useEffect, useState } from 'react';

import {
  makeStyles,
  createStyles,
  TextField,
  MenuItem
} from '@material-ui/core';
import moment from 'moment';

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
      minWidth: 200,
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
  fullWidth?: boolean;
  endDate?: Date;
  startDate: Date;
  setStartDate: (start: Date) => void ;
}

const INPUT_DATE_FORMAT = 'YYYY-MM-DDTHH:mm';

const DATE_QUICK_SELECTOR_RANGES: {
  [key: string]: number;
} = {
  'None': 0,
  'Last 12 hours': 12,
  'Last 24 hours': 24,
  'Last 3 days': 3 * 24,
  'Last 7 days': 7 * 24,
  'Last 1 month': 30 * 24,
  'Last 2 months': 2 * 30 * 24,
  'Last 3 months': 3 * 30 * 24,
  'Last 6 months': 6 * 30 * 24,
  'Last 1 year': 12 * 30 * 24,
  'Last 2 years': 2 * 12 * 30 * 24,
  'Last 3 years': 3 * 12 * 30 * 24
};

export default ({
  fullWidth,
  endDate,
  startDate,
  setStartDate
}: Props) => {
  const classes = useStyles();

  // States
  const [dateQuickSelector, setDateQuickSelector] = useState<number>(DATE_QUICK_SELECTOR_RANGES['None']);

  useEffect(() => {
    const duration = moment.duration(moment(endDate).diff(moment(startDate))).as('hours');
    let selected = 'None';
    for (const range in DATE_QUICK_SELECTOR_RANGES) {
      const rangeDuration = DATE_QUICK_SELECTOR_RANGES[range];
      if (Math.abs(rangeDuration - duration) / rangeDuration < 0.1) {
        selected = range;
        break;
      }
    }

    setDateQuickSelector(DATE_QUICK_SELECTOR_RANGES[selected]);
  }, [startDate, endDate]);

  const onChangeDateQuickSelector = (
    e: React.ChangeEvent<{
      value: string;
    }>
  ) => {
    const toDate = endDate !== undefined ? moment(endDate) : moment();
    const fromDate = moment
      .utc(
        toDate.subtract(
          Number.parseInt(e.target.value),
          'hours'
        ),
        INPUT_DATE_FORMAT,
        true
      )
      .toDate();

    setStartDate(fromDate);
    setDateQuickSelector(Number.parseInt(e.target.value));
  };

  return (
    <TextField
      className={classes.toolbarInput}
      variant='outlined'
      fullWidth={fullWidth}
      select
      label='Date Quick Selector'
      value={dateQuickSelector}
      onChange={(e) => onChangeDateQuickSelector(e)}
    >
      {
        Object.entries(DATE_QUICK_SELECTOR_RANGES).map(pair =>
          <MenuItem key={`menu-item-${pair[1]}`} value={pair[1]}>{pair[0]}</MenuItem>
        )
      }
    </TextField>
  );
};
