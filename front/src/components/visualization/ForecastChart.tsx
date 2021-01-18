import React, { useState, useEffect } from 'react';

import {
  Icon
} from '@material-ui/core';
import {
  ResponsiveContainer,
  ComposedChart,
  Area,
  Line,
  CartesianGrid,
  XAxis,
  YAxis,
  Legend,
  LegendProps,
  Tooltip,
  TooltipProps,
  ReferenceLine
} from 'recharts';
import moment from 'moment';

import {
  ChartDataPoint,
  generateColor,
  tooltipStyles
} from '.';
import { commonStyles } from '../common';
import {
  QueryResult,
  QUERY_SELECTOR_REGEX
} from '../../types';

const MAX_LINES = 30;

interface ChartProps {
  timeSeriesQueryResult: QueryResult[];
  startDate: Date;
  endDate?: Date;
  setSuccess: (heading: string, message: string) => void;
  setError: (heading: string, message: string) => void;
  trend: boolean;
};

export const ForecastChart = ({
  timeSeriesQueryResult,
  startDate,
  endDate,
  setError,
  trend
}: ChartProps) => {
  // Styles
  const common = commonStyles();
  const tooltip = tooltipStyles();

  // States
  const [isNegative, setIsNegative] = useState<boolean>(false);
  const [chartData, setChartData] = useState<ChartDataPoint[]>([]);
  const [chartLines, setChartLines] = useState<React.ReactElement[]>([]);
  const [legendHeight, setLegendHeight] = useState<number>(0);
  const [lineHighlight, setLineHighlight] = useState<string | null>(null);
  const [endDateOfData, setEndDateOfData] = useState<number>(moment().unix());

  useEffect(() => {
    // Map QueryResult[] to chart-compatible data
    if (timeSeriesQueryResult.length > MAX_LINES) {
      // Probably a bad query
      setError('Too many different results', 'This is likely a problem with grouping parameters.');
      return;
    }
    let data: ChartDataPoint[] = [];
    let keyNumber = -1;
    let isDataNegative = false;

    const lines: React.ReactElement[] = [];
    timeSeriesQueryResult.flatMap((queryResult) => {
      if (queryResult.measurements.length <= 0) {
        setError('Query error', 'No measurements found.');
        return;
      }

      const filterOutGrouping = (entry: [string, unknown]) => {
        const match = entry[0].match(QUERY_SELECTOR_REGEX);
        if (!match) {
          throw new Error('Illegal data: Measurements has invalid values.');
        }
        const agg = match[1];
        return agg !== 'group_by';
      };

      const forecastArray: string[] = [];

      Object
        .entries(queryResult.measurements[0].values)
        .filter(filterOutGrouping)
        .map((entry, i) => {
          const keyMatches = entry[0].match(QUERY_SELECTOR_REGEX);
          if (!keyMatches) {
            throw new Error('Illegal data: Measurements has invalid values.');
          }
          let label = keyMatches[0];

          if (keyMatches[5].includes('yhat') && forecastArray.includes(`${keyMatches[3]}.${keyMatches[4]}`)) {
            return;
          };

          keyNumber++;
          if (keyMatches[2] === 'result_output' && keyMatches[5].includes('yhat')) {
            queryResult.measurements.forEach(measurement => {
              const lower = Object.entries(measurement.values).find(
                v => v[0].includes(`result_output:${keyMatches[3]}.${keyMatches[4]}.yhat_lower`)
              );
              const upper = Object.entries(measurement.values).find(
                v => v[0].includes(`result_output:${keyMatches[3]}.${keyMatches[4]}.yhat_upper`)
              );

              if (lower && lower[1] < 0) {
                isDataNegative = true;
              }

              const point: ChartDataPoint = {
                time: moment(measurement.time).unix()
              };
              if (lower && upper) {
                const value = Object.entries(measurement.values).find(
                  v => v[0].includes(`result_output:${keyMatches[3]}.${keyMatches[4]}.yhat`)
                  && v[0] !== lower[0]
                  && v[0] !== upper[0]
                );
                if (value) {
                  if (label !== value[0]) {
                    label = value[0];
                  }
                  point[`${label}${keyNumber}`] = value[1];
                  point[`${label}${keyNumber}-area`] = [
                    lower[1],
                    upper[1]
                  ];
                  forecastArray.push(
                    `${keyMatches[3]}.${keyMatches[4]}`
                  );
                  data.push(point);
                }
              }
            });

            lines.push(
              <Area
                name={label + '-area'}
                key={`${label}${keyNumber}-area`}
                type='linear'
                dataKey={`${label}${keyNumber}-area`}
                dot={false}
                fill={generateColor(keyNumber, MAX_LINES)}
                stroke={generateColor(keyNumber, MAX_LINES)}
                strokeOpacity={0}
                animationDuration={300}
              />
            );
          }
          else {
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
          }

          lines.push(
            <Line
              name={label}
              key={`${label}${keyNumber}`}
              type='linear'
              dataKey={`${label}${keyNumber}`}
              dot={false}
              stroke={generateColor(keyNumber, MAX_LINES)}
              animationDuration={300}
            />
          );
        })
        .filter(line => line != null);
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

    setIsNegative(isDataNegative);
    setChartLines(lines);
    setChartData(data);
  }, [timeSeriesQueryResult, setError, trend]);

  const legendRef = React.createRef<HTMLUListElement>();
  const chartGridRef = React.createRef<CartesianGrid>();
  const yAxisRef = React.createRef<YAxis>();

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
          payload
            .filter((entry: { value: string }) => !entry.value.includes('area'))
            .map((entry: { value: React.ReactNode }, index: number) => (
              <li
                key={`item-${index}`}
                style={entry.value === lineHighlight
                  ? { textShadow: '0px 0px 1px black' }
                  : { textShadow: 'none' }
                }
                onMouseEnter={() => setLineHighlight(entry.value as string)}
              >
                <Icon
                  style={{
                    color: generateColor(index, MAX_LINES),
                    display: 'inline-flex',
                    verticalAlign: 'middle',
                    marginRight: '2px'
                  }}
                >
                  linear_scale
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
      const classes = tooltip;
      return (
        <div className={classes.chartTooltip}>
          <div className={classes.labelContainer}>
            <p className={classes.labelDateTime}>
              <b>{moment.unix(Number(label)).utc().format('DD/MM/YYYY')}</b>
            </p>
          </div>
          {
            [...payload]
              .map((p) => {
                if (typeof p.value === 'number') {
                  return (
                    <div key={`key-${p.name}`}>
                      <div
                        className={classes.contentLegend}
                        style={{ backgroundColor: p.color }}
                      />
                      <span className={classes.contentName}>{p.name}:</span>
                      <span className={classes.contentValue}>{p.value}</span>
                    </div>
                  );
                }
                else {
                  return (
                    <div key={`key-${p.name}-area`}>
                      <>
                        <div
                          className={classes.contentLegend}
                          style={{ backgroundColor: 'black' }}
                        />
                        <span className={classes.contentName}>{p.name} lower:</span>
                        <span className={classes.contentValue}>{p.value[0]}</span>
                      </>
                      <br />
                      <>
                        <div
                          className={classes.contentLegend}
                          style={{ backgroundColor: 'black' }}
                        />
                        <span className={classes.contentName}>{p.name} upper:</span>
                        <span className={classes.contentValue}>{p.value[1]}</span>
                      </>
                    </div>
                  );
                };
              })
          }
        </div>
      );
    }
    return null;
  };

  return (
    <ResponsiveContainer width='100%' height={460 + legendHeight}>
      <ComposedChart
        className={common.topMargin}
        data={chartData}
      >
        <XAxis
          allowDataOverflow
          dataKey='time'
          tickFormatter={time => moment.unix(time).utc().format('DD/MM/YYYY')}
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
          domain={isNegative ? ['auto', 'auto'] : [0, 'auto']}
          type='number'
        />
        <CartesianGrid
          ref={chartGridRef}
          strokeDasharray='3 3'
        />
        <Legend
          layout='vertical'
          iconType='plainline'
          content={renderLegend}
        />
        <Tooltip
          content={renderTooltip}
        />
        <ReferenceLine
          x={moment().unix()}
          stroke='red' />

        {chartLines.map((line: React.ReactElement) => {
          return React.cloneElement(
            line,
            {
              strokeWidth: (lineHighlight === line.props.name) ? 2 : 1,
              fillOpacity: (lineHighlight + 'area' === line.props.name) ? 0.4 : 0.2,
              strokeDasharray: (line.props.name.includes('trend')) ? '5 5' : undefined
            }
          );
        })}
      </ComposedChart>
    </ResponsiveContainer>
  );
};
