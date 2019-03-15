import React, { useState } from 'react';

import {
  useDataConnection
} from '../../../hooks';

import {
  Paper,
  LinearProgress,
  Typography,
  TextareaAutosize,
  TextField,
  Button
} from '@material-ui/core';
import * as Colors from '@material-ui/core/colors';
import { JsonIngestDataSeriesConfigurator } from '.';
import { JsonIngestDataConnectionConfiguration } from '../../../types';
import { ingestJson } from '../../../client';
import { useAlerts } from '../../../alert';

interface Props {
  match: {
    params: {
      id: number;
      name: string;
    };
  };
}

export const JsonIngestPage = ({
  match: {
    params
  }
}: Props) => {
  // Alert handlers
  const alertContext = useAlerts('JSON-INGEST');
  const setSuccess = (heading: string, message: string) => {
    alertContext.alertSuccess(heading, message);
  };
  const setError = (heading: string, message: string) => {
    alertContext.alertError(heading, message);
  };

  const { dataConnection } = useDataConnection(params.id);

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

  if (!dataConnection) {
    return (
      <LinearProgress variant='query' />
    );
  }

  const dataSeries = dataConnection.series;
  const token = (dataConnection.configuration as JsonIngestDataConnectionConfiguration).token || '';

  const onSubmitJson = () => {
    ingestJson(
      token,
      sampleJson
    )
      .then(response => {
        if (response.ok) {
          setSuccess('Successfully ingested JSON Document', 'Rows have been inserted to database');
        }
        else {
          setError('Failed to ingest JSON Document', 'Please check network log for more details');
        }
      })
      .catch((error: Error) => {
        setError('Failed to ingest JSON Document', error.message);
      });
  };

  return (
    <>
      <Paper
        style={{
          padding: '10px',
          marginBottom: '12px'
        }}
      >
        <Typography
          variant='h6'
          style={{
            marginBottom: '12px'
          }}
        >
          JSON Ingestion
        </Typography>
        <TextField
          fullWidth
          variant='outlined'
          label='Token'
          value={token}
          InputLabelProps={{
            shrink: true
          }}
          InputProps={{
            readOnly: true
          }}
          style={{
            marginBottom: '12px'
          }}
        />
        <Typography
          variant='body2'
          style={{
            marginBottom: '14px'
          }}
        >
          Input a valid JSON document below and preview rows parsed by DataSeries
        </Typography>
        <Button
          color='primary'
          variant='outlined'
          onClick={onSubmitJson}
        >
          Submit JSON
        </Button>
      </Paper>
      <Paper style={{ padding: '10px' }}>
        <Typography
          variant='h6'
          style={{
            marginBottom: '4px'
          }}
        >
          JSON Document
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
        {
          dataSeries.length <= 0
            ? <i>No Data Series available</i>
            : <JsonIngestDataSeriesConfigurator
              readOnly
              dataConnection={dataConnection}
              dataSeries={dataSeries[0]}
              setDataSeries={() => undefined}
              sampleJson={sampleJson}
            />
        }
      </Paper>
    </>
  );
};
