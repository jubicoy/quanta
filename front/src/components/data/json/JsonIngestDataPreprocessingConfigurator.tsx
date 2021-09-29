import React, { useState, useContext, useEffect } from 'react';

import { _DataConnectionConfiguratorContext } from '../../context';
import StepperButtons from '../StepperButtons';
import {
  Paper,
  Typography,
  TextareaAutosize
} from '@material-ui/core';
import * as Colors from '@material-ui/core/colors';

import {
  DEFAULT_COLUMN,
  JsonIngestDataSeriesConfiguration,
  Column,
  DataSeries
} from '../../../types';
import { JsonIngestDataSeriesConfigurator } from '.';

interface PathColumnMapping {
  jsonPath: string;
  column: Column;
};

export const JsonIngestDataPreprocessingConfigurator = () => {
  const {
    dataConnection,
    dataSeries,
    setDataSeries,

    handleForward
  } = useContext(_DataConnectionConfiguratorContext);

  const buildDataSeriesFromMappings = (
    inputPathColumnsMappings: PathColumnMapping[],
    inputDataSeries: DataSeries
  ): DataSeries => {
    return {
      ...inputDataSeries,
      columns: inputPathColumnsMappings.map(({ column }, i) => {
        return {
          ...column,
          index: i
        };
      }),
      configuration: {
        ...(inputDataSeries.configuration as JsonIngestDataSeriesConfiguration),
        paths: inputPathColumnsMappings.map(({ jsonPath }) => jsonPath)
      }
    };
  };

  const defaultPathColumnMappings = [
    {
      jsonPath: '$.point.time',
      column: {
        ...DEFAULT_COLUMN,
        index: 0,
        name: 'time',
        type: {
          format: `yyyy-MM-dd`,
          className: 'java.time.Instant',
          nullable: false
        }
      }
    },
    {
      jsonPath: '$.point.valueLong',
      column: {
        ...DEFAULT_COLUMN,
        index: 1,
        name: 'value',
        type: {
          format: null,
          className: 'java.lang.Long',
          nullable: false
        }
      }
    }
  ];

  useEffect(
    () => setDataSeries(
      buildDataSeriesFromMappings(
        defaultPathColumnMappings,
        {
          ...dataSeries,
          dataConnection
        }
      )
    ),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    []
  );

  const [sampleJson, setSampleJson] = useState<string>(`{
    "point": {
      "time": "2016-05-17",
      "valueLong": 123321,
      "valueString": "Hello world",
      "valueBoolean": false
    },
    "points": [
      {
        "time": "2016-05-17",
        "valueLong": 1111,
        "valueString": "Hello Earth",
        "valueBoolean": false
      },
      {
        "time": "2016-05-18",
        "valueLong": 2222,
        "valueString": "Hello Mars",
        "valueBoolean": true
      },
      {
        "time": "2016-05-19",
        "valueLong": 3333,
        "valueString": "Hello Kepler-443b",
        "valueBoolean": false


      }
    ]
  }`);

  const [isValidJson, setIsValidJson] = useState<boolean>(true);
  const onChangeJson = (value: string) => {
    let isValid = true;

    try {
      JSON.parse(value);
    }
    catch (e) {
      console.error(e);
      isValid = false;
    }

    setSampleJson(value);
    setIsValidJson(isValid);
  };

  const onForward = () => {
    setDataSeries({
      ...dataSeries,
      dataConnection
    });
    handleForward();
  };

  return <>
    <Paper style={{ padding: '10px' }}>
      <Typography
        variant='h6'
        style={{
          marginBottom: '4px'
        }}
      >
        Sample JSON Document
      </Typography>
      <TextareaAutosize
        id='json-textarea-label'
        value={sampleJson}
        onChange={(e) => onChangeJson(e.target.value)}
        style={{
          borderColor: isValidJson ? 'inherit' : Colors.red[600],
          outlineColor: isValidJson ? 'inherit' : Colors.red[600],
          resize: 'vertical',
          width: '100%'
        }}
      />
      <Typography
        variant='h5'
        style={{
          margin: '12px 0'
        }}
      >
        DataSeries
      </Typography>
      <JsonIngestDataSeriesConfigurator
        dataConnection={dataConnection}
        dataSeries={dataSeries}
        setDataSeries={setDataSeries}
        sampleJson={sampleJson}
      />
    </Paper>
    <StepperButtons
      onNextClick={onForward}
      disableNext={false}
    />
  </>;
};
