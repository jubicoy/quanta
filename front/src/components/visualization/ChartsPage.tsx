import React, { useState, useMemo, useEffect, useRef, useCallback } from 'react';

import {
  makeStyles,
  createStyles,
  LinearProgress,
  FormControl,
  MenuItem,
  TextField,
  Button,
  Icon,
  Paper,
  Switch,
  FormControlLabel,
  Checkbox
} from '@material-ui/core';
import { red, grey } from '@material-ui/core/colors';

import Autocomplete from '@material-ui/lab/Autocomplete';
import moment from 'moment';
import Fuse from 'fuse.js';

import { DateQuickSelector } from '../common';
import { arrayDistinctBy } from '../../utils';
import { TimeSeriesChart } from './TimeSeriesChart';
import {
  TimeSeriesQuery,
  Column,
  QuerySelector,
  DataSeriesSelector,
  TIME_SERIES_MODIFIERS,
  VALID_DATA_TYPES,
  SeriesResultSelector,
  SeriesResultOutputSelector,
  ColumnSelector,
  OutputColumnWithInvocation,
  QUERY_SELECTOR_REGEX
} from '../../types';
import {
  useQueryTimeSeries,
  useColumns,
  useQueryParams
} from '../../hooks';
import { useAlerts } from '../../alert';

const useStyles = makeStyles(() => {
  const padding = 0;
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
      userSelect: 'none',
      paddingRight: 12 + padding + 'px',
      paddingLeft: 12 + padding + 'px',
      paddingTop: '12px',
      marginTop: '24px'
    },
    chartToolbar: {
      textAlign: 'right',
      paddingRight: 4 + padding + 'px',
      paddingLeft: 4 + padding + 'px',
      '& .MuiFormControl-root': {
        marginLeft: '10px'
      }
    },
    toolbarSelect: {
      height: height + 'px',
      '& .MuiOutlinedInput-notchedOutline ': {
        ...borderStyle
      }
    },
    toolbarDateInput: {
      '& .MuiInputBase-root': {
        height: height + 'px'
      },
      '& .MuiOutlinedInput-notchedOutline ': {
        ...borderStyle
      }
    },
    toolbarSwitch: {
      ...borderStyle,
      paddingLeft: '12px',
      marginRight: 0,
      height: height + 'px'
    },
    toolbarSquareButton: {
      ...borderStyle,
      height: height + 'px',
      width: height + 'px',
      minWidth: height + 'px',
      marginLeft: '10px'
    },
    chartToolbarRow: {
      display: 'block',
      marginBottom: '10px'
    },
    columnSelector: {
      paddingLeft: padding + 'px',
      '& .MuiInputBase-root, .MuiButtonBase-root': {
        height: 'auto'
      },
      '& .MuiFormControl-root': {
        marginLeft: '0px'
      }
    },
    chipInput: {
      backgroundColor: grey[300],
      display: 'inline-flex',
      padding: '0 8px 0 12px',
      height: '32px',
      borderRadius: '16px',
      fontSize: '0.8em',
      border: 'none',
      outline: '0',
      verticalAlign: 'middle',
      textAlign: 'center',
      alignItems: 'center',
      justifyContent: 'center',
      margin: '3px 0',
      marginRight: '8px',
      '& .chipInputSpan, .chipInputIcon': {
        display: 'inline-block'
      },
      '& .chipInputSpan': {
        boxSizing: 'content-box'
      },
      '& .chipInputIcon': {
        fontSize: '24px',
        marginLeft: '6px',
        cursor: 'pointer',
        color: grey[600],
        '&:hover': {
          color: grey[800]
        }
      },
      '&.editing': {
        backgroundColor: 'transparent',
        boxShadow: 'inset 0px 0px 0px 1px ' + grey[500],
        '& .chipInputSpan': {
          outline: 'none'
        }
      },
      '&.error': {
        backgroundColor: red[600],
        '& .chipInputIcon': {
          color: grey[50],
          '&:hover': {
            color: grey[300]
          }
        },
        color: '#fff'
      },
      '&:focus': {
        backgroundColor: grey[400]
      },
      '&.error:focus': {
        backgroundColor: red[800]
      }
    },
    paper: {
      boxShadow: '0px 2px 6px rgba(0, 0, 0, 0.5)'
    },
    option: {
      padding: '0'
    }
  });
});

export const unixToTimeString = (unixUtc: number, format: string): string => {
  return moment.unix(unixUtc).utc().format(format);
};

interface SelectorOption {
  querySelector: QuerySelector | null;
  fullString: string;
  key: number;
  isEditing?: boolean;
  isError?: boolean;
}

const ChartsPage = () => {
  const classes = useStyles();

  // Query parameters
  const query = useQueryParams();
  const startDateParam = query.get('startDate');
  const endDateParam = query.get('endDate');
  const selectorsParam = query.get('selectors');

  const intervalSeconds = {
    'None': 0,
    '15 minutes': 15 * 60,
    '1 hour': 60 * 60,
    '6 hours': 60 * 60 * 6,
    '12 hours': 60 * 60 * 12,
    '1 day': 60 * 60 * 24,
    '3 days': 60 * 60 * 24 * 3,
    '1 week': 60 * 60 * 24 * 7,
    '2 week': 60 * 60 * 24 * 7 * 2,
    '1 month': 60 * 60 * 24 * 30,
    '3 month': 60 * 60 * 24 * 30 * 3
  };
  const API_DATE_FORMAT = 'YYYY-MM-DDTHH:mm:ss';
  const INPUT_DATE_FORMAT = 'YYYY-MM-DDTHH:mm';
  // Alert handlers
  const alertContext = useAlerts('TIME-SERIES-CHART');
  const setSuccess = useCallback(
    (heading: string, message: string) => {
      alertContext.alertSuccess(heading, message);
    },
    [alertContext]
  );
  const setError = useCallback(
    (heading: string, message: string) => {
      alertContext.alertError(heading, message);
    },
    [alertContext]
  );

  // States
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [enableEndDate, setEnableEndDate] = useState<boolean>(false);

  const [chartInterval, setChartInterval] = useState<number>(
    Number(query.get('interval')) || intervalSeconds['None']
  );

  // Used for input, no validation
  const startDateDefault = startDateParam && moment.utc(startDateParam, INPUT_DATE_FORMAT, true).isValid()
    ? moment.utc(startDateParam, INPUT_DATE_FORMAT, true).toDate()
    : new Date(2016, 0, 0);
  const endDateDefault = endDateParam && moment.utc(endDateParam, INPUT_DATE_FORMAT, true).isValid()
    ? moment.utc(endDateParam, INPUT_DATE_FORMAT, true).toDate()
    : new Date(2021, 0, 0);
  const [startDateInputText, setStartDateInputText] = useState<string>(
    moment.utc(startDateDefault).format(INPUT_DATE_FORMAT)
  );
  const [endDateInputText, setEndDateInputText] = useState<string>(
    moment.utc(endDateDefault).format(INPUT_DATE_FORMAT)
  );

  // Used for query, with validation
  const [startDate, setStartDate] = useState<Date>(startDateDefault);
  const [endDate, setEndDate] = useState<Date>(endDateDefault);

  // AutoComplete controlled states
  const [selectorOptions, setSelectorOptions] = useState<SelectorOption[]>([]);
  const [selectorSelections, setSelectorSelections] = useState<SelectorOption[]>([]);
  const [selectorInputValue, setSelectorInputValue] = useState<string>('');
  const [selectorHighlight, setSelectorHighlight] = useState<SelectorOption | null>(null);
  const [focusingChipIndex, setFocusingChipIndex] = useState<number>(-1);

  // Key-counter for selection
  const [, setSelectionKeyCount] = useState<number>(selectorSelections.length);

  // Input-validation
  const [selectorError, setSelectorError] = useState<boolean>(false);
  const [intervalError, setIntervalError] = useState<boolean>(false);

  const [isRawDataMode, setIsRawDataMode] = useState<boolean>(false);

  const selectorInputRef = useRef<HTMLInputElement>();
  const inputChipRefs = React.useRef<(HTMLDivElement | null)[]>([]);

  const {
    dataSeriesColumns,
    seriesResultColumnSelectors,
    seriesResultWorkerOutputs
  } = useColumns({});

  const timeSeriesQuery: TimeSeriesQuery = useMemo(
    () => ({
      selectors: selectorSelections.map(selector => selector.fullString),
      interval: chartInterval,
      start: moment.utc(startDate).format(API_DATE_FORMAT) + 'Z',
      end: enableEndDate ? moment.utc(endDate).format(API_DATE_FORMAT) + 'Z' : undefined
    }),
    [selectorSelections, chartInterval, startDate, endDate, enableEndDate]
  );

  const parseQuerySelectorFromString = useCallback(
    (fullString: string) => {
      const matches = fullString.match(QUERY_SELECTOR_REGEX);

      if (!matches) {
        setError('Input Error', 'A Selector is invalid');
        return null;
      }

      const modifier = matches[1];
      const type = matches[2];
      const name = matches[3];
      const invocationNumber = matches[4];
      const columnName = matches[5];
      const filterCondition = matches[6];
      const alias = matches[7];

      // Can't have modifier on raw data
      if (modifier && modifier !== 'where' && isRawDataMode) {
        setError('Input Error', 'Aggregation is not allowed in raw-data mode');
        return null;
      }

      if (modifier === 'where' && filterCondition === '') {
        setError('Input Error', 'Filter is missing condition');
        return null;
      }

      let selector: DataSeriesSelector | SeriesResultSelector | SeriesResultOutputSelector;
      switch (type) {
        case 'series':
          selector = new DataSeriesSelector(
            name,
            columnName
          );
          break;

        case 'result':
          selector = new SeriesResultSelector(
            name,
            columnName,
            invocationNumber.toString()
          );
          break;

        case 'result_output':
          selector = new SeriesResultOutputSelector(
            name,
            columnName,
            invocationNumber.toString()
          );
          break;

        default:
          setError('Input Error', 'Failed to parse series descriptor');
          return null;
      };

      const querySelector = new QuerySelector(
        selector,
        modifier as TIME_SERIES_MODIFIERS,
        alias,
        filterCondition
      );

      return querySelector;
    },
    [isRawDataMode, setError]
  );

  const onAddSelection = (fullString: string) => {
    setSelectionKeyCount((key) => {
      const querySelector = parseQuerySelectorFromString(fullString);
      const isError = querySelector === null;
      const selection = {
        fullString,
        key,
        isEditing: false,
        isError,
        querySelector
      };
      setSelectorSelections([
        ...selectorSelections,
        selection
      ]);
      query.set(
        'selectors',
        [
          ...selectorSelections,
          selection
        ]
          .map(s => s.fullString)
          .join(',')
      );
      return key + 1;
    });
  };

  const onRemoveSelection = useCallback(
    (index: number) => {
      const newSelections = [...selectorSelections];
      newSelections.splice(index, 1);
      setSelectorSelections(newSelections);
      query.set(
        'selectors',
        newSelections.map(s => s.fullString).join(',')
      );
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [selectorSelections]
  );

  const onChangeSelection = useCallback(
    (
      index: number,
      key: keyof SelectorOption,
      value: SelectorOption[keyof SelectorOption]
    ) => {
      const newSelections = [...selectorSelections];
      if (key === 'fullString' && value === '') {
        // Delete selection if string is empty
        onRemoveSelection(index);
        return;
      }
      if (key === 'fullString') {
        const querySelector = parseQuerySelectorFromString(value as string);
        newSelections[index].isError = querySelector === null;
        newSelections[index].querySelector = querySelector;
      }
      if (key === 'isEditing' && value === true) {
        // Reset error on change, so check can run again
        newSelections[index].isError = false;
      }
      (newSelections[index][key] as SelectorOption[keyof SelectorOption]) = value;
      setSelectorSelections(newSelections);
      query.set(
        'selectors',
        newSelections.map(s => s.fullString).join(',')
      );
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [onRemoveSelection, parseQuerySelectorFromString, selectorSelections]
  );

  const resetEditingForAllSelections = useCallback(
    () => {
      const refs = inputChipRefs.current.filter(ref => ref);

      [...selectorSelections]
        .forEach((_selection, index) => {
          const div = refs[index];
          if (div) {
            const innerSpanElement = div.firstElementChild as HTMLSpanElement;
            onChangeSelection(index, 'fullString', innerSpanElement.innerText);
            onChangeSelection(index, 'isEditing', false);
          }
        });
    },
    [onChangeSelection, selectorSelections]
  );

  const isSelectionsValid: boolean = useMemo(
    () => {
      setSelectorError(false);
      setIntervalError(false);
      let isValid = true;

      // Don't make query if some chips are still being edited / error
      if (selectorSelections
        .some(selection => selection.isEditing || selection.isError)
      ) {
        return false;
      }

      if (isRawDataMode) {
        return true;
      }

      arrayDistinctBy(
        selector => selector.querySelector?.selector.name || '',
        selectorSelections
      )
        .filter(selector => selector.querySelector)
        .map(selector => selector.querySelector?.selector.name || '')
        .forEach(seriesName => {
          const selectorsOfName = selectorSelections.filter(selector =>
            selector.querySelector?.selector.name === seriesName
          );
          const hasDataSelect = selectorsOfName.some(selector =>
            selector.querySelector?.modifier !== TIME_SERIES_MODIFIERS.group_by
            && selector.querySelector?.modifier !== TIME_SERIES_MODIFIERS.where
          );
          const hasGroup = selectorsOfName.some(selector =>
            selector.querySelector?.modifier === TIME_SERIES_MODIFIERS.group_by
          );
          const hasFilter = selectorsOfName.some(selector =>
            selector.querySelector?.modifier === TIME_SERIES_MODIFIERS.where
          );

          // Requires grouping/timeInterval if timeInterval is 0
          if (chartInterval === 0) {
            if (hasDataSelect && !hasGroup) {
              setError('Input Error', 'Grouping selector or Interval is needed.');
              setSelectorError(true);
              setIntervalError(true);
              isValid = false;
            };
          };
          // Requires data select if only grouping is found
          if ((hasGroup || hasFilter) && !hasDataSelect) {
            setError('Input Error', 'Data selector is needed.');
            setSelectorError(true);
            isValid = false;
          };
        });

      return isValid;
    },
    // Missing `setError`, adding it here causes unlimited re-renders, no idea why.
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [chartInterval, isRawDataMode, selectorSelections]
  );

  const { timeSeriesQueryResult, refresh: refreshQuery } = useQueryTimeSeries(
    setIsLoading,
    setError,
    timeSeriesQuery,
    isSelectionsValid
  );

  const mapToSelectorOptions = (
    selector: DataSeriesSelector | SeriesResultSelector | SeriesResultOutputSelector,
    columnClassName: string,
    columnAlias?: string
  ): SelectorOption[] => {
    let colOpts: SelectorOption[] = [];

    // Option aggregations
    if (Object.keys(VALID_DATA_TYPES).includes(columnClassName)) {
      // Is valid data column
      // Map all agg
      colOpts = Object
        .values(TIME_SERIES_MODIFIERS)
        .map(agg => {
          const querySelector = new QuerySelector(
            selector,
            agg,
            columnAlias,
            agg === 'where' ? '= \'\'' : undefined
          );
          return {
            querySelector,
            fullString: querySelector.toString(),
            key: -1
          };
        });

      // Option raw data
      // Is valid data column
      const querySelector = new QuerySelector(
        selector,
        undefined,
        columnAlias
      );
      colOpts.push({
        querySelector,
        fullString: querySelector.toString(),
        key: -1
      });
    }
    else {
      // Is not valid data column
      // Map only group_by / where
      colOpts = Object
        .values(TIME_SERIES_MODIFIERS)
        .filter(agg => agg === 'group_by' || agg === 'where')
        .map(agg => {
          const querySelector = new QuerySelector(
            selector,
            agg,
            columnAlias,
            agg === 'where' ? ' = \'\'' : undefined
          );
          return {
            querySelector,
            fullString: querySelector.toString(),
            key: -1
          };
        });
    }

    return colOpts;
  };

  const mapTimeSeriesColumnsToOptions = useCallback(
    (
      dataSeriesCols?: Column[],
      seriesResultColSelectors?: ColumnSelector[],
      seriesResultWorkerOutputs?: OutputColumnWithInvocation[]
    ): SelectorOption[] => {
      // Map Series-columns to appropriate tags for filtering
      if (!dataSeriesCols && !seriesResultColSelectors && !seriesResultWorkerOutputs) return [];
      const options = (dataSeriesCols || [])
        .filter(column =>
          column.name !== 'time'
        )
        .flatMap((column: Column) => {
          if (column && column.series) {
            const selector = new DataSeriesSelector(
              column.series.name,
              column.name
            );
            return mapToSelectorOptions(
              selector,
              column.type.className,
              undefined
            );
          }
          throw new Error('Invalid column');
        })
        .concat(
          (seriesResultColSelectors || [])
            .filter(columnSelector =>
              columnSelector.columnName !== 'time'
            )
            .flatMap(
              (columnSelector: ColumnSelector): SelectorOption[] => {
                if (columnSelector.invocation) {
                  const selector = new SeriesResultSelector(
                    columnSelector.invocation.task.name,
                    columnSelector.columnName,
                    columnSelector.invocation.invocationNumber.toString()
                  );
                  return mapToSelectorOptions(
                    selector,
                    columnSelector.type.className,
                    columnSelector.alias
                  );
                }
                throw new Error('Invalid column');
              }
            )
        )
        .concat(
          (seriesResultWorkerOutputs || [])
            .flatMap((outputColumn: OutputColumnWithInvocation): SelectorOption[] => {
              if (outputColumn.invocation) {
                const selector = new SeriesResultOutputSelector(
                  outputColumn.invocation.task.name,
                  outputColumn.columnName,
                  outputColumn.invocation.invocationNumber.toString()
                );
                return mapToSelectorOptions(
                  selector,
                  outputColumn.type.className,
                  outputColumn.alias
                );
              }
              throw new Error('Worker definition column with no invocation');
            })
        );
      if (selectorsParam) {
        const paramArray = selectorsParam.split(',');
        setSelectorSelections(
          options
            .filter(o => paramArray.includes(o.fullString))
            .map((selector, key) => ({
              ...selector,
              key
            }))
        );
      }
      return options;
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    []
  );

  useEffect(() => {
    setSelectorOptions(
      mapTimeSeriesColumnsToOptions(
        dataSeriesColumns,
        seriesResultColumnSelectors,
        seriesResultWorkerOutputs
      )
    );
  }, [
    dataSeriesColumns,
    seriesResultColumnSelectors,
    seriesResultWorkerOutputs,
    mapTimeSeriesColumnsToOptions
  ]);

  useEffect(() => {
    if (isRawDataMode) {
      setChartInterval(0);
      query.remove('interval');
    }
    setSelectorSelections([]);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isRawDataMode]);

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

  const chart = useMemo(
    () => <TimeSeriesChart
      startDate={startDate}
      endDate={enableEndDate ? endDate : undefined}
      timeSeriesQueryResult={timeSeriesQueryResult}
      setSuccess={setSuccess}
      setError={setError}
    />,
    [
      timeSeriesQueryResult,
      startDate, endDate,
      enableEndDate,
      setSuccess, setError
    ]);

  const startDateRef = React.createRef<HTMLInputElement>();
  const endDateRef = React.createRef<HTMLInputElement>();

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

    return (<TextField
      className={classes.toolbarDateInput}
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
    />);
  };

  const inputAdormentChipsRender = useMemo(
    () => {
      return selectorSelections.map((selection, index) => {
        return <div
          key={selection.key}
          className={
            classes.chipInput
            + (selection.isEditing ? ' editing' : '')
            + (selection.isError ? ' error' : '')
          }
          ref={ref => {
            inputChipRefs.current[index] = ref;
          }}
          tabIndex={0}
          onFocus={() => {
            setFocusingChipIndex(index);
          }}
          onDoubleClick={e => {
            // Prevent Autocomplete's input from "stealing" focus
            e.stopPropagation();
            e.preventDefault();
            const innerSpanElement = e.currentTarget.firstElementChild as HTMLSpanElement|null;
            if (innerSpanElement && !selection.isEditing) {
              resetEditingForAllSelections();
              innerSpanElement.contentEditable = 'true';
              innerSpanElement.focus();
              // Set editable
              onChangeSelection(index, 'isEditing', true);
            }
          }}
          onKeyDown={e => {
            if (e.key === 'Enter') {
              e.stopPropagation();
              e.preventDefault();
              const innerSpanElement = e.currentTarget.firstElementChild as HTMLSpanElement|null;
              if (innerSpanElement && !selection.isEditing) {
                resetEditingForAllSelections();
                innerSpanElement.contentEditable = 'true';
                innerSpanElement.focus();
                // Set editable
                onChangeSelection(index, 'isEditing', true);
              }
            }
          }}
        >
          <span
            className='chipInputSpan'
            contentEditable={selection.isEditing}
            onKeyDown={e => {
              if (selection.isEditing) {
                e.stopPropagation(); // Stop propagation here so editables doesn't lose focus
                if (e.key === 'Enter') {
                  e.preventDefault();
                  if (e.currentTarget) {
                    onChangeSelection(index, 'isEditing', false);
                    onChangeSelection(index, 'fullString', e.currentTarget.innerText);
                    if (e.currentTarget.parentElement) {
                      e.currentTarget.parentElement.focus();
                    }
                  }
                }
              }
            }}
            onMouseDown={e => e.stopPropagation()}
            onMouseUp={e => e.stopPropagation()}
            onClick={e => e.stopPropagation()}
            suppressContentEditableWarning
          >
            {selection.fullString}
          </span>
          <Icon
            className='chipInputIcon'
            onClick={e => {
              e.preventDefault();
              e.stopPropagation();
              onRemoveSelection(index);
            }}
          >cancel</Icon>
        </div>;
      });
    },
    [
      classes.chipInput,
      selectorSelections,
      onChangeSelection,
      onRemoveSelection,
      resetEditingForAllSelections
    ]
  );

  // Render
  return (
    <>
      <Paper className={classes.chartContainer}>
        <div className={classes.chartToolbar}>
          <div className={classes.chartToolbarRow}>
            <FormControlLabel
              className={classes.toolbarSwitch}
              control={
                <Switch
                  checked={isRawDataMode}
                  color='primary'
                  onChange={(e) => setIsRawDataMode(e.target.checked)}
                />
              }
              labelPlacement='start'
              label='Raw data'
            />
            <FormControl variant='outlined' disabled={isRawDataMode}>
              <TextField
                select
                variant='outlined'
                error={intervalError}
                className={classes.toolbarSelect}
                label='Interval'
                id='interval-select'
                value={chartInterval}
                onChange={(e) => {
                  e.target.value === 'None'
                    ? query.remove('interval')
                    : query.set('interval', e.target.value as string);
                  setChartInterval(Number.parseInt(e.target.value));
                }}
                InputProps={{
                  style: {
                    height: '46px'
                  }
                }}
              >
                {
                  Object.entries(intervalSeconds).map(pair =>
                    <MenuItem key={`menu-item-${pair[1]}`} value={pair[1]}>{pair[0]}</MenuItem>
                  )
                }
              </TextField>
            </FormControl>
            {dateTimeInput(
              'Start', 'start-date', startDateInputText, setStartDateInputText, startDateRef
            )}
            {dateTimeInput(
              'End', 'end-date', endDateInputText, setEndDateInputText, endDateRef
            )}
            <DateQuickSelector
              setStartDate={setStartDate}
              setStartDateInputText={setStartDateInputText}
            />
            <Button
              className={classes.toolbarSquareButton}
              variant='outlined'
              onClick={refreshQuery}
            >
              <Icon>refresh</Icon>
            </Button>
          </div>
          <div className={classes.chartToolbarRow}>
            <Autocomplete
              freeSolo
              clearOnBlur={false}
              clearOnEscape={false}
              multiple
              classes={{
                paper: classes.paper,
                option: classes.option
              }}
              className={classes.columnSelector}

              options={
                selectorOptions
              }

              // Set highlighted option to use with auto-complete
              onHighlightChange={(_e, highlightedOption) => {
                setSelectorHighlight(highlightedOption as SelectorOption);
              }}

              // Autocomplete when pressing Tab / Enter
              onKeyDown={e => {
                if (e.key === 'Tab') {
                  // Select highlighted value as selector
                  e.preventDefault();
                  if (selectorHighlight) {
                    onAddSelection(selectorHighlight.fullString);
                  }
                }
                if (e.key === 'Enter') {
                  // Select highlighted value as selector
                  // OR enter custom value
                  e.preventDefault();
                  e.stopPropagation();
                  if (selectorHighlight) {
                    onAddSelection(selectorHighlight.fullString);
                  }
                  else if (selectorInputValue !== '') {
                    onAddSelection(selectorInputValue);
                  }
                }
              }}

              // Current input value
              inputValue={selectorInputValue}
              onInputChange={(e, newInputValue) => {
                setSelectorInputValue(newInputValue);
              }}
              // Selected values
              value={selectorSelections}
              onChange={(_e, newValue: SelectorOption[], reason: string) => {
                if (reason === 'clear') {
                  // All other reasons' behaviors are overriden
                  setSelectorSelections(newValue);
                }
              }}
              // Check if a option is selected
              getOptionSelected={() => false}

              filterOptions={(options, { inputValue }) => {
                // Filter closest-matching options based on inputValue
                // Return all if inputValue is empty
                if (inputValue === '') {
                  return options;
                }
                const searchProps = {
                  shouldSort: true,
                  keys: [
                    'label'
                  ]
                };
                const optionsToString = options.map(option => {
                  return {
                    ...option,
                    label: option.fullString
                  };
                });
                const fuse = new Fuse(optionsToString, searchProps);
                return fuse.search(inputValue)
                  .map(fuseItem => {
                    const newOption = {
                      ...fuseItem.item,
                      'fuseMatches': (fuseItem.matches || []) as Fuse.FuseResultMatch[]
                    };
                    return newOption;
                  });
              }}
              renderOption={(option: SelectorOption) => {
                const labelString = option.fullString;
                return <div
                  style={{
                    width: '100%',
                    padding: '6px 12px'
                  }}
                  onClick={(e) => {
                    e.preventDefault();
                    e.stopPropagation();
                    onAddSelection(labelString);
                  }}
                >
                  {labelString}
                </div>;
              }}

              renderInput={(params) => (
                <TextField
                  {...params}
                  error={selectorError}
                  variant='outlined'
                  label='Selectors'
                  inputRef={selectorInputRef}
                  onClick={e => {
                    e.preventDefault();
                  }}
                  onFocus={() => {
                    resetEditingForAllSelections();
                    setFocusingChipIndex(-1);
                  }}
                  onKeyDown={e => {
                    if (e.key === 'ArrowLeft' || e.key === 'ArrowRight') {
                      e.preventDefault();
                      e.stopPropagation();
                      const isLeft = e.key === 'ArrowLeft';
                      const refs = inputChipRefs.current.filter(ref => ref);

                      if (refs.length >= 1) {
                        if (focusingChipIndex === -1 && !isLeft) {
                          // Don't focus anything if going right from input
                          return;
                        }
                        if (focusingChipIndex === 0 && isLeft) {
                          return;
                        }
                        if (focusingChipIndex >= refs.length - 1 && !isLeft) {
                          if (selectorInputRef.current) {
                            selectorInputRef.current.focus();
                          }
                          return;
                        }

                        let indexToFocus;
                        if (focusingChipIndex === -1 && isLeft) {
                          indexToFocus = refs.length - 1;
                        }
                        else {
                          indexToFocus = isLeft
                            ? focusingChipIndex - 1
                            : focusingChipIndex + 1;
                        }

                        const element = refs[indexToFocus];
                        if (element) {
                          element.focus();
                        }
                      }
                    }
                    if (e.key === 'Backspace') {
                      if (focusingChipIndex === -1) {
                        return;
                      }

                      e.stopPropagation();
                      e.preventDefault();
                      const element = selectorInputRef.current;
                      if (element) {
                        element.focus();
                      }
                      onRemoveSelection(focusingChipIndex);
                      setFocusingChipIndex(-1);
                    }
                  }}
                  InputLabelProps={{
                    shrink: true
                  }}
                />
              )}

              renderTags={() => {
                return inputAdormentChipsRender;
              }}

            />
            <LinearProgress
              style={isLoading
                ? { opacity: 1, transition: 'opacity .2s' }
                : { opacity: 0, transition: 'opacity .2s' }
              }
            />
          </div>
        </div>
        {/* <div id='chartCanvas' ref={chartRef} /> */}
        {chart}
      </Paper>
    </>
  );
};
export default ChartsPage;
