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
  validateAndSetDate: (dateAsString: string, isStartDate: boolean) => void ;
}

const INPUT_DATE_FORMAT = 'YYYY-MM-DDTHH:mm';

const dateQuickSelectorRanges = {
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
  validateAndSetDate
}: Props) => {
  const classes = useStyles();

  // States
  const [dateQuickSelector, setDateQuickSelector] = useState<number>(dateQuickSelectorRanges['None']);

  useEffect(() => {
    const duration = moment.duration(moment(endDate).diff(moment(startDate))).as('hours');
    const getDateQuickSelector = Object.entries(dateQuickSelectorRanges).reduce((prev, curr) => {
      return (Math.abs(curr[1] - duration) < Math.abs(prev[1] - duration) ? curr : prev);
    });

    setDateQuickSelector(
      Math.abs(getDateQuickSelector[1] - duration) > duration * 0.2
        ? dateQuickSelectorRanges['None']
        : getDateQuickSelector[1]);
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
        )
      )
      .format(INPUT_DATE_FORMAT);

    validateAndSetDate(fromDate, true);
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
        Object.entries(dateQuickSelectorRanges).map(pair =>
          <MenuItem key={`menu-item-${pair[1]}`} value={pair[1]}>{pair[0]}</MenuItem>
        )
      }
    </TextField>
  );
};
