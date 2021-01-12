import React,
{
  useCallback,
  useEffect,
  useMemo,
  useState
} from 'react';

import {
  makeStyles,
  createStyles,
  Button,
  Fab,
  FormControlLabel,
  Grid,
  Icon,
  LinearProgress,
  MenuItem,
  Paper,
  Switch,
  TextField,
  Typography,
  Checkbox
} from '@material-ui/core';
import clsx from 'clsx';
import moment from 'moment';

import { useAlerts } from '../../alert';
import { commonStyles } from '../common';
import {
  useColumns,
  useInvocations,
  useQueryParams,
  useQueryTimeSeries
} from '../../hooks';
import {
  ColumnSelector,
  InvocationStatus,
  OutputColumnWithInvocation,
  QuerySelector,
  TIME_SERIES_MODIFIERS,
  TimeSeriesQuery,
  SeriesResultOutputSelector,
  SeriesResultSelector
} from '../../types';

import { ForecastChart } from './ForecastChart';

const useStyles = makeStyles(theme => {
  const height = 46;
  const borderStyle = {
    border: '1px solid rgba(0, 0, 0, .24)',
    borderRadius: '4px',
    '&:hover': {
      border: '1px solid rgba(0, 0, 0, 1)'
    }
  };
  return createStyles({
    chartContainer: {
      userSelect: 'none'
    },
    chartToolbar: {
      textAlign: 'right',
      paddingRight: theme.spacing(1),
      paddingLeft: theme.spacing(1)
    },
    toolbarInput: {
      minWidth: 226,
      '& .MuiInputBase-root': {
        height: height + 'px'
      },
      '& .MuiOutlinedInput-notchedOutline ': {
        ...borderStyle
      }
    },
    toolbarSquareButton: {
      ...borderStyle,
      height: height + 'px',
      width: height + 'px',
      minWidth: height + 'px'
    },
    toolbarSwitch: {
      ...borderStyle,
      height: height + 'px',
      marginRight: 0
    },
    chartToolbarRow: {
      display: 'block'
    },
    columnSelector: {
      paddingLeft: theme.spacing(2),
      '& .MuiInputBase-root, .MuiButtonBase-root': {
        height: 'auto'
      },
      '& .MuiFormControl-root': {
        marginLeft: 0
      }
    },
    paper: {
      boxShadow: '0px 2px 6px rgba(0, 0, 0, 0.5)'
    }
  });
});

interface InvocationOption {
  invocation: number | 'latest';
  date: number | null;
  selectors: QuerySelector[];
}

interface Props {
  match: { params: {id: string } };
}

export default ({
  match: { params: { id } }
}: Props) => {
  const classes = useStyles();
  const common = commonStyles();
  const query = useQueryParams();
  const startDateParam = query.get('startDate');
  const endDateParam = query.get('endDate');
  const invocationParam = query.get('invocation');
  const dateQuickSelectorParam = query.get('dateQuickSelector');

  // Defaults
  const API_DATE_FORMAT = 'YYYY-MM-DDTHH:mm:ss';
  const INPUT_DATE_FORMAT = 'YYYY-MM-DDTHH:mm';
  const DEFAULT_MODIFIER = TIME_SERIES_MODIFIERS.sum;
  const DEFAULT_INTERVAL = 60 * 60 * 24 * 7;

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

  const dateNow = new Date();
  const startDateDefault = startDateParam && moment.utc(startDateParam, INPUT_DATE_FORMAT, true).isValid()
    ? moment.utc(startDateParam, INPUT_DATE_FORMAT, true).toDate()
    : new Date(dateNow.getFullYear(), dateNow.getMonth() - 1, dateNow.getDate());

  if (!startDateParam || !moment.utc(startDateParam, INPUT_DATE_FORMAT, true).isValid()) {
    query.set(
      'startDate',
      moment.utc(startDateDefault).format(INPUT_DATE_FORMAT)
    );
  }

  const endDateDefault = endDateParam && moment.utc(endDateParam, INPUT_DATE_FORMAT, true).isValid()
    ? moment.utc(endDateParam, INPUT_DATE_FORMAT, true).toDate()
    : new Date(dateNow.getFullYear(), dateNow.getMonth() + 1, dateNow.getDate());

  // Alert handlers
  const alertContext = useAlerts('FORECAST-CHART');
  const setSuccess = useCallback((heading: string, message: string) => {
    alertContext.alertSuccess(heading, message);
  }, [alertContext]);
  const setError = useCallback((heading: string, message: string) => {
    alertContext.alertError(heading, message);
  }, [alertContext]);

  // States
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [enableEndDate, setEnableEndDate] = useState<boolean>(false);

  // Used for input, no validation
  const [startDateInputText, setStartDateInputText] = useState<string>(
    moment.utc(startDateDefault).format(INPUT_DATE_FORMAT)
  );
  const [endDateInputText, setEndDateInputText] = useState<string>(
    moment.utc(endDateDefault).format(INPUT_DATE_FORMAT)
  );

  const [dateQuickSelector, setDateQuickSelector] = useState<number>(
    dateQuickSelectorParam && (!isNaN(Number(dateQuickSelectorParam)))
      ? Number.parseInt(dateQuickSelectorParam)
      : dateQuickSelectorRanges['None']);

  // Used for query, with validation
  const [startDate, setStartDate] = useState<Date>(startDateDefault);
  const [endDate, setEndDate] = useState<Date>(endDateDefault);

  const [invocationOptions, setInvocationOptions] = useState<InvocationOption[]>([]);
  const [selectedInvocation, setSelectedInvocation] = useState<number | 'latest'>(
    invocationParam && (!isNaN(Number(invocationParam)))
      ? Number(invocationParam)
      : 'latest'
  );
  const [selectorValue, setSelectorValue] = useState<QuerySelector[]>([]);
  const [filterInput, setFilterInput] = useState<string>(
    query.get('filter') || ''
  );
  const [filter, setFilter] = useState<string>(filterInput);
  const [grouped, setGrouped] = useState<boolean>(
    query.get('grouped') === 'true' || false
  );
  const [sourceData, setSourceData] = useState<boolean>(
    query.get('source') === 'true' || false
  );

  const status: InvocationStatus = 'Completed';
  const invocationsQuery = useMemo(
    () => ({
      task: parseInt(id),
      status
    }),
    [id]
  );
  const { invocations } = useInvocations(invocationsQuery);
  const {
    seriesResultColumnSelectors,
    seriesResultWorkerOutputs
  } = useColumns({
    taskId: parseInt(id)
  });

  const mapToSelectors = useCallback(
    (
      seriesResultWorkerOutputs: OutputColumnWithInvocation[],
      seriesResultColumnSelectors: ColumnSelector[] | undefined
    ): QuerySelector[] => {
      const inputs = sourceData && seriesResultColumnSelectors
        ? seriesResultColumnSelectors
          .filter(input => input.invocation !== undefined && input.columnName !== 'time')
          .map(input => {
            const selector = new SeriesResultSelector(
              input.invocation ? input.invocation.task.name : '',
              input.columnName,
              input.invocation ? input.invocation.invocationNumber.toString() : ''
            );
            return new QuerySelector(
              selector,
              input.workerDefColumn?.name === 'group'
                ? TIME_SERIES_MODIFIERS.group_by
                : DEFAULT_MODIFIER,
              input.alias
            );
          })
        : [];
      const outputs = seriesResultWorkerOutputs.map(output => {
        const selector = new SeriesResultOutputSelector(
          output.invocation.task.name,
          output.columnName,
          output.invocation.invocationNumber.toString()
        );
        return new QuerySelector(
          selector,
          output.columnName === 'group'
            ? TIME_SERIES_MODIFIERS.group_by
            : DEFAULT_MODIFIER,
          output.alias
        );
      });

      return [
        ...inputs,
        ...outputs
      ];
    }, [
      sourceData,
      DEFAULT_MODIFIER
    ]);

  useEffect(() => {
    if (
      invocations
      && (!sourceData || seriesResultColumnSelectors)
      && seriesResultWorkerOutputs
    ) {
      const selectors = mapToSelectors(seriesResultWorkerOutputs, seriesResultColumnSelectors || undefined);
      // Map Invocations to InvocationOptions
      setInvocationOptions(
        [
          {
            invocation: 'latest',
            date: null,
            selectors: selectors.filter(s =>
              (s.selector as SeriesResultOutputSelector).invocationSelector === 'latest'
            )
          },
          ...invocations.map(i => ({
            invocation: i.invocationNumber,
            date: i.startTime,
            selectors: selectors.filter(s =>
              (s.selector as SeriesResultOutputSelector).invocationSelector === i.invocationNumber.toString()
            )
          }))
        ]
      );
    }
    else {
      setInvocationOptions([]);
    }
  }, [
    invocations,
    sourceData,
    seriesResultColumnSelectors,
    seriesResultWorkerOutputs,
    mapToSelectors
  ]);

  useEffect(() => {
    const selectedOption = invocationOptions.find(i => i.invocation === selectedInvocation);
    if (selectedOption) {
      let newValue = selectedOption.selectors.filter(s =>
        grouped || s.modifier === DEFAULT_MODIFIER
      );
      const groupSelectors = selectedOption.selectors.filter(
        s => s.modifier && s.modifier === TIME_SERIES_MODIFIERS.group_by
      );
      if (filter.length > 0) {
        newValue = [
          ...newValue,
          ...groupSelectors.map(group => new QuerySelector(
            group.selector,
            TIME_SERIES_MODIFIERS.where,
            undefined,
            `= '${filter}'`
          ))
        ];
      }
      setSelectorValue(newValue);
    }
  }, [
    invocationOptions,
    grouped,
    filter,
    selectedInvocation,
    DEFAULT_MODIFIER
  ]);

  const timeSeriesQuery: TimeSeriesQuery = useMemo(
    () => ({
      selectors: selectorValue.map(selector => selector.toString()),
      interval: DEFAULT_INTERVAL,
      start: moment.utc(startDate).format(API_DATE_FORMAT) + 'Z',
      end: enableEndDate ? moment.utc(endDate).format(API_DATE_FORMAT) + 'Z' : undefined
    }),
    [selectorValue, startDate, endDate, DEFAULT_INTERVAL, enableEndDate]
  );

  const { timeSeriesQueryResult, refresh: refreshQuery } = useQueryTimeSeries(setIsLoading, setError, timeSeriesQuery, true);

  const chart = useMemo(() => (
    <ForecastChart
      startDate={startDate}
      endDate={enableEndDate ? endDate : undefined}
      timeSeriesQueryResult={timeSeriesQueryResult}
      setSuccess={setSuccess}
      setError={setError}
    />
  ),
  [
    timeSeriesQueryResult,
    startDate,
    endDate,
    enableEndDate,
    setError,
    setSuccess
  ]);

  const validateAndSetDate = (isStartDate: boolean): void => {
    const currentInputText = isStartDate ? startDateInputText : endDateInputText;
    const currentInputDate = moment.utc(currentInputText, INPUT_DATE_FORMAT, true).toDate();
    const isValid = moment.utc(currentInputText, INPUT_DATE_FORMAT, true).isValid();
    const setInput = isStartDate ? setStartDateInputText : setEndDateInputText;
    const setValue = isStartDate ? setStartDate : setEndDate;
    const getDateAsString = (date: Date) => moment.utc(date).format(INPUT_DATE_FORMAT);

    if (!isValid
      || currentInputDate < new Date(1970, 0, 0)
      || currentInputDate > new Date(2100, 0, 0)
    ) {
      // Reset to old date if new date isn't valid or within reasonable range
      setInput(
        isStartDate
          ? getDateAsString(startDate)
          : getDateAsString(endDate)
      );
      return;
    }

    if (isStartDate) {
      // Swap start/end date when needed
      if (currentInputDate > endDate) {
        query.set('startDate', getDateAsString(endDate));
        query.set('endDate', currentInputText);
        setStartDateInputText(getDateAsString(endDate));
        setStartDate(endDate);
        setEndDateInputText(currentInputText);
        setEndDate(currentInputDate);
        return;
      }
    }
    else {
      if (currentInputDate < startDate) {
        query.set('endDate', getDateAsString(startDate));
        query.set('startDate', currentInputText);
        setEndDateInputText(getDateAsString(startDate));
        setEndDate(startDate);
        setStartDateInputText(currentInputText);
        setStartDate(currentInputDate);
        return;
      }
    }

    query.set(isStartDate ? 'startDate' : 'endDate', currentInputText);
    setInput(currentInputText);
    setValue(currentInputDate);
  };

  const startDateRef = React.createRef<HTMLInputElement>();
  const endDateRef = React.createRef<HTMLInputElement>();
  const filterRef = React.createRef<HTMLInputElement>();

  const dateTimeInput = (
    label: string,
    name: string,
    inputState: string,
    setInputState: React.Dispatch<React.SetStateAction<string>>,
    ref: React.RefObject<HTMLInputElement>
  ) => {
    const testInput = document.createElement('input');
    try {
      // Non-supporting browser will fail and type remains as text
      testInput.type = 'datetime-local';
    }
    catch (e) {
      console.log(e.description);
    }

    return (
      <>
        <TextField
          className={clsx(classes.toolbarInput)}
          fullWidth
          variant='outlined'
          label={label}
          type='datetime-local'
          name={name}
          value={label === 'End' && !enableEndDate ? '' : inputState}
          disabled={label === 'End' && !enableEndDate}
          onKeyDown={e => {
            if (e.key === 'Enter') {
              e.preventDefault();
              if (ref.current) {
                ref.current.blur();
              }
            }
          }}
          inputRef={ref}
          onBlur={() => validateAndSetDate(label === 'Start')}
          onChange={(e) => {
            setInputState(e.target.value);
          }}
          inputProps={{
            title: testInput.type === 'text' ? 'Input date as format: ' + INPUT_DATE_FORMAT : ''
          }}
          InputLabelProps={{
            shrink: true
          }}
          InputProps={label === 'End'
            ? {
              startAdornment: (
                <Checkbox
                  disabled={false}
                  checked={enableEndDate}
                  color='primary'
                  onChange={(e) => {
                    e.target.checked ? query.set('endDate', endDateInputText) : query.remove('endDate');
                    setEnableEndDate(e.target.checked);
                  }}
                />
              )
            } : {}}
        />
      </>
    );
  };

  // Render
  return (
    <>
      <div className={clsx(common.verticalPadding, common.header)}>
        <div>
          <Typography variant='h4'>Forecast visualization</Typography>
        </div>
        <div className={common.toggle}>
          <Fab
            variant='extended'
            color='primary'
            className={common.leftMargin}
            href={`/task/${id}`}
          >
            <Icon className={common.icon}>
              clear
            </Icon>
            Cancel
          </Fab>
        </div>
      </div>
      <Paper className={clsx(classes.chartContainer, common.padding)}>
        <div className={classes.chartToolbar}>
          <div className={clsx(classes.chartToolbarRow, common.bottomMargin)}>
            <Grid container direction='row' spacing={2}>
              <Grid container item xs={4}>
                <Button
                  className={clsx(classes.toolbarSquareButton, common.leftMargin)}
                  variant='outlined'
                  onClick={refreshQuery}
                >
                  <Icon>refresh</Icon>
                </Button>
                <FormControlLabel
                  className={clsx(classes.toolbarSwitch, common.leftPadding)}
                  control={
                    <Switch
                      checked={grouped}
                      color='primary'
                      onChange={(e) => {
                        e.target.checked ? query.set('grouped', 'true') : query.remove('grouped');
                        setGrouped(e.target.checked);
                      }}
                    />
                  }
                  labelPlacement='start'
                  label='Group'
                />
                <FormControlLabel
                  className={clsx(classes.toolbarSwitch, common.leftPadding)}
                  control={
                    <Switch
                      checked={sourceData}
                      color='primary'
                      onChange={(e) => {
                        e.target.checked ? query.set('source', 'true') : query.remove('source');
                        setSourceData(e.target.checked);
                      }}
                    />
                  }
                  labelPlacement='start'
                  label='Source'
                />
              </Grid>
              <Grid container item xs={8} spacing={2}>
                <Grid item xs={4}>
                  {dateTimeInput('Start', 'start-date', startDateInputText, setStartDateInputText, startDateRef)}
                </Grid>
                <Grid item xs={4}>
                  {dateTimeInput('End', 'end-date', endDateInputText, setEndDateInputText, endDateRef)}
                </Grid>
                <Grid item xs={4}>
                  <TextField
                    className={clsx(classes.toolbarInput)}
                    fullWidth
                    variant='outlined'
                    select
                    label='Date Quick Selector'
                    value={dateQuickSelector}
                    onChange={e => {
                      const fromDate = moment.utc(moment().subtract(Number.parseInt(e.target.value), 'hours')).format(INPUT_DATE_FORMAT);
                      setStartDateInputText(fromDate);
                      query.set('startDate', fromDate);
                      setStartDate(moment.utc(fromDate, INPUT_DATE_FORMAT, true).toDate());
                      Number.parseInt(e.target.value) === 0 ? query.remove('dateQuickSelector') : query.set('dateQuickSelector', e.target.value);
                      setDateQuickSelector(Number.parseInt(e.target.value));
                    }}
                  >
                    {
                      Object.entries(dateQuickSelectorRanges).map(pair =>
                        <MenuItem key={`menu-item-${pair[1]}`} value={pair[1]}>{pair[0]}</MenuItem>
                      )
                    }
                  </TextField>
                </Grid>
              </Grid>
            </Grid>
          </div>
          <div className={clsx(classes.chartToolbarRow, common.bottomMargin)}>
            <Grid container direction='row' spacing={2}>
              <Grid item xs>
                <TextField
                  className={classes.toolbarInput}
                  select
                  fullWidth
                  label='Invocation'
                  variant='outlined'
                  value={selectedInvocation}
                  onChange={e => {
                    const value = invocationOptions.find(i => i.invocation === e.target.value);
                    if (value) {
                      isNaN(Number(value.invocation))
                        ? query.remove('invocation')
                        : query.set('invocation', value.invocation as string);
                      setSelectedInvocation(value.invocation);
                    }
                  }}
                >
                  {invocationOptions.map(i => (
                    <MenuItem key={i.invocation} value={i.invocation}>
                      {i.date
                        ? `${i.invocation}: ${moment.utc(i.date).format(INPUT_DATE_FORMAT)}`
                        : `${i.invocation}`}
                    </MenuItem>
                  ))}
                </TextField>
              </Grid>
              <Grid item xs>
                <TextField
                  className={classes.toolbarInput}
                  fullWidth
                  label='Filter'
                  variant='outlined'
                  value={filterInput}
                  onKeyDown={e => {
                    if (e.key === 'Enter') {
                      e.preventDefault();
                      if (filterRef.current) {
                        filterRef.current.blur();
                      }
                    }
                  }}
                  inputRef={filterRef}
                  onChange={e => setFilterInput(e.target.value as string)}
                  onBlur={() => {
                    setFilter(filterInput);
                    filterInput.length > 0
                      ? query.set('filter', filterInput)
                      : query.remove('filter');
                  }}
                />
              </Grid>
            </Grid>
            <LinearProgress
              style={isLoading
                ? { opacity: 1, transition: 'opacity .2s' }
                : { opacity: 0, transition: 'opacity .2s' }
              }
            />
          </div>
        </div>
        {chart}
      </Paper>
    </>
  );
};
