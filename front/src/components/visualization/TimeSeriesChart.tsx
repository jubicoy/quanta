import React, { useState, useEffect } from 'react';
import {
  Icon
} from '@material-ui/core';
import {
  ResponsiveContainer,
  LineChart,
  Line,
  CartesianGrid,
  XAxis,
  YAxis,
  Legend,
  LegendProps,
  Tooltip,
  TooltipProps
} from 'recharts';
import moment from 'moment';

import {
  ChartDataPoint,
  generateColor,
  tooltipStyles
} from '.';
import { QueryResult, QUERY_SELECTOR_REGEX } from '../../types';
import { unixToTimeString } from './ChartsPage';

const MAX_LINES = 10;

interface ChartProps {
  timeSeriesQueryResult: QueryResult[];
  startDate: Date;
  endDate?: Date;
  setSuccess: (heading: string, message: string) => void;
  setError: (heading: string, message: string) => void;
};

export const TimeSeriesChart = ({
  timeSeriesQueryResult,
  startDate,
  endDate,
  setSuccess,
  setError
}: ChartProps) => {
  const [chartLines, setChartLines] = useState<React.ReactNode[]>([]);

  const [legendHeight, setLegendHeight] = useState<number>(0);
  const [lineHighlight, setLineHighlight] = useState<number | null>(null);

  // State
  const [chartData, setChartData] = useState<ChartDataPoint[]>([]);
  const [endDateOfData, setEndDateOfData] = useState<number>(moment().unix());

  const tooltipClasses = tooltipStyles();

  const mapQueryResultToChartLines = (timeSeriesQueryResult: QueryResult[]) => {
    // Map QueryResult[] to chart-compatible data
    // From
    // {
    //   "time": "2017-04-22T23:59:36Z",
    //   "values": {
    //     "1": -246.82666,
    //     "2": 3681.4336
    //   }
    // },
    // To
    // {
    //   time: '2017-04-22T23:59:36Z',
    //   column1: -246.82666,
    //   column2: 3681.4336
    // }, ...

    let data: ChartDataPoint[] = [];

    let keyNumber = -1;

    const lines: React.ReactNode[] = timeSeriesQueryResult.flatMap((queryResult) => {
      if (queryResult.measurements.length <= 0) {
        setError('Query error', 'No measurements found.');
        return;
      }
      const groupingSuffix: string = Object
        .entries(queryResult.measurements[0].values)
        .filter(entry => {
          const match = entry[0].match(QUERY_SELECTOR_REGEX);
          if (!match) {
            throw new Error('Illegal data: Measurements has invalid values.');
          }
          const agg = match[1];
          return agg === 'group_by';
        })
        .map(entry => {
          // Check if value entry is grouping or data
          const key = entry[0];
          const value = entry[1];

          const keyMatches = key.match(QUERY_SELECTOR_REGEX);
          if (keyMatches && keyMatches[1] === 'group_by') {
            return ` -- ${keyMatches[5]} = ${value}`;
          }
          return ``;
        })
        .reduce((result, current) => {
          result += current;
          return result;
        }, '');

      const filterOutGrouping = (entry: [string, unknown]) => {
        const match = entry[0].match(QUERY_SELECTOR_REGEX);
        if (!match) {
          throw new Error('Illegal data: Measurements has invalid values.');
        }
        const agg = match[1];
        return agg !== 'group_by';
      };

      return Object
        .entries(queryResult.measurements[0].values)
        .filter(filterOutGrouping)
        .map((entry, i) => {
          keyNumber++;

          const key = entry[0];
          let label = '';

          const keyMatches = key.match(QUERY_SELECTOR_REGEX);
          if (!keyMatches) {
            throw new Error('Illegal data: Measurements has invalid values.');
          }
          label = keyMatches[0];

          queryResult.measurements.forEach(measurement => {
            const point: ChartDataPoint = {
              time: moment(measurement.time).unix()
            };

            const value = Object
              .entries(measurement.values)
              .filter(filterOutGrouping)[i];

            if (value[0] !== label) {
              setError('Illegal data', 'Measurements has inconsistent values.');
            }

            point[`${label}${keyNumber}`] = value[1];
            data.push(point);
          });

          return <Line
            connectNulls
            name={label + groupingSuffix}
            key={`${label}${keyNumber}`}
            type='linear'
            dataKey={`${label}${keyNumber}`}
            dot={false}
            stroke={generateColor(keyNumber, MAX_LINES)}
            animationDuration={300}
          />;
        });
    });

    // Group points of same timestamp
    data = data
      .sort((a, b) => a.time - b.time)
      .reduce(
        (accum: ChartDataPoint[], current) => {
          // Check if object with this timestamp exists
          const { time, ...values } = current;

          const foundIndex = accum.findIndex(point => point.time as number === time as number);

          let point = accum[foundIndex] || { time };

          point = {
            ...point,
            ...values
          };

          if (foundIndex !== -1) {
            accum[foundIndex] = point;
          }
          else {
            accum.push(point);
          }

          return accum;
        }, []);

    if (data[data.length - 1] !== undefined) {
      setEndDateOfData(data[data.length - 1].time);
    }

    if (lines.length > MAX_LINES) {
      setError('Too many lines, colorings will be in-distinguishable.', `Only the first ${MAX_LINES} will be shown.`);
    }

    setChartLines(lines.slice(0, MAX_LINES));

    setChartData(data);
  };

  const legendRef = React.createRef<HTMLUListElement>();
  const chartGridRef = React.createRef<CartesianGrid>();
  const yAxisRef = React.createRef<YAxis>();

  useEffect(() => {
    if (timeSeriesQueryResult.length > MAX_LINES) {
      // Probably a bad query
      setError('Too many different results', 'This is likely a problem with grouping parameters.');
      return;
    }
    mapQueryResultToChartLines(timeSeriesQueryResult);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [timeSeriesQueryResult]);

  const renderLegend = ({ payload }: LegendProps) => {
    if (legendRef.current) {
      setLegendHeight(legendRef.current.offsetHeight);
    }
    if (!payload) return;
    return (
      <ul
        ref={legendRef}
        style={{ listStyleType: 'none' }}
        onMouseLeave={() => setLineHighlight(null)}
      >
        {
          payload.map((entry: { value: React.ReactNode }, index: number) => (
            <li
              key={`item-${index}`}
              style={index === lineHighlight
                ? { textShadow: '0px 0px 1px black' }
                : { textShadow: 'none' }
              }
              // eslint-disable-next-line @typescript-eslint/no-unused-vars
              onMouseEnter={(e) => setLineHighlight(index)}
            >
              <Icon
                style={{
                  color: generateColor(index, MAX_LINES),
                  display: 'inline-flex',
                  verticalAlign: 'middle',
                  marginRight: '2px'
                }}
              >linear_scale
              </Icon>
              {entry.value}
            </li>
          ))
        }
      </ul>
    );
  };

  const renderTooltip = ({ active, payload, label }: TooltipProps) => {
    if (active && payload && label) {
      const classes = tooltipClasses;
      const labelDate = moment.unix(parseInt(label.toString())).format('MMMM Do, YYYY');
      const labelTime = moment.unix(parseInt(label.toString())).format('hh:mm:ss');
      return (
        <div className={classes.chartTooltip}>
          <div className={classes.labelContainer}>
            <p className={classes.labelDateTime}>
              <b>{labelDate}</b>  {labelTime}
            </p>
          </div>
          {
            [...payload]
              .sort((a, b) => (b.value as number) - (a.value as number))
              .map((p) => <div key={`key-${p.name}`}>
                <div
                  className={classes.contentLegend}
                  style={{ backgroundColor: p.color }}
                />
                <span className={classes.contentName}>{p.name}:</span>
                <span className={classes.contentValue}>{p.value}</span>
              </div>)
          }
        </div>
      );
    }
    return null;
  };

  const chartMargin = 5;
  return <ResponsiveContainer width='100%' height={460 + legendHeight}>
    <LineChart
      margin={{ top: chartMargin, right: chartMargin, left: chartMargin, bottom: chartMargin }}
      data={chartData}
    >
      <CartesianGrid
        ref={chartGridRef}
        strokeDasharray='3 3'
      />
      <XAxis
        allowDataOverflow
        dataKey='time'
        tickFormatter={time => unixToTimeString(time, 'DD/MM/YYYY')}
        type='number'
        tickCount={10}
        minTickGap={0}
        interval={'preserveStartEnd'}
        domain={endDate !== undefined
          ? [moment(startDate).unix(), moment(endDate).unix()]
          : [moment(startDate).unix(), endDateOfData]
        }
        padding={{ left: 0, right: 0 }}
      />
      <YAxis
        ref={yAxisRef}
        allowDataOverflow
        allowDecimals={false}
        domain={['auto', 'auto']}
        type='number'
      />
      <Legend
        layout='vertical'
        iconType='plainline'
        content={renderLegend}
      />
      <Tooltip
        content={renderTooltip}
      />
      {chartLines.map((line, i: number) => {
        return React.cloneElement(
          // eslint-disable-next-line @typescript-eslint/ban-ts-ignore
          // @ts-ignore
          line as Line,
          {
            strokeWidth: (lineHighlight === i) ? 2 : 1
          }
        );
      })}
    </LineChart>
  </ResponsiveContainer>;
};
