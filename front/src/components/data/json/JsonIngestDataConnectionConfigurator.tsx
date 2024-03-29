import React, { useContext, useEffect, useRef } from 'react';

import { _DataConnectionConfiguratorContext } from '../../context';

import {
  Card,
  CardContent,
  CardHeader,
  Typography,
  Divider,
  Icon,
  InputAdornment,
  IconButton,
  TextField
} from '@material-ui/core';
import StepperButtons from '../StepperButtons';

import { submitDataConnection } from '../../../client';
import { useDataSeriesNameCheck } from '../../../hooks';
import {
  DataConnection,
  JsonIngestDataConnectionConfiguration,
  DEFAULT_JSON_INGEST_DATA_CONNECTION,
  DEFAULT_JSON_INGEST_DATA_CONNECTION_CONFIGURATION
} from '../../../types';

// Set path, settings and upload csv
// Call both upload and sample in this step
export const JsonIngestDataConnectionConfigurator = () => {
  const {
    dataConnection,
    setDataConnection,

    dataSeries,
    setDataSeries,

    setSuccess,
    setError,

    handleForward
  } = useContext(_DataConnectionConfiguratorContext);

  const { nameIsValid, helperText } = useDataSeriesNameCheck(dataSeries.name);

  useEffect(() => {
    const updatedDataConnection: DataConnection = {
      ...DEFAULT_JSON_INGEST_DATA_CONNECTION,
      ...dataConnection,
      configuration: DEFAULT_JSON_INGEST_DATA_CONNECTION_CONFIGURATION
    };
    submitDataConnection(updatedDataConnection)
      .then((dataConnection) => setDataConnection(dataConnection))
      .catch((err: Error) => setError('Create Data Connection fail', err));
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [dataConnection.name, dataConnection.description]);

  const tokenRef = useRef<HTMLInputElement>(null);

  const copyTokenToClipboard = () => {
    if (!tokenRef.current) {
      return;
    }
    tokenRef.current.select();
    document.execCommand('copy');
    setSuccess('Token is copied to clipboard');
  };

  return (
    <>
      <Card style={{ marginTop: '15px' }}>
        <CardHeader title='DataConnection' />
        <CardContent>
          <Typography variant='body1'>Name: <b>{dataConnection.name}</b></Typography>
          <Typography variant='body1'>Description: <b>{dataConnection.description}</b></Typography>
          <Divider
            style={{
              marginTop: '16px',
              marginBottom: '26px'
            }}
          />
          <TextField
            fullWidth
            variant='outlined'
            label='Token'
            value={(dataConnection.configuration as JsonIngestDataConnectionConfiguration).token || ''}
            InputLabelProps={{
              shrink: true
            }}
            inputRef={tokenRef}
            InputProps={{
              readOnly: true,
              endAdornment: <InputAdornment position='end'>
                <IconButton
                  onClick={copyTokenToClipboard}
                  aria-label='copy token to clipboard'
                >
                  <Icon>
                    file_copy
                  </Icon>
                </IconButton>
              </InputAdornment>
            }}
          />
          <Divider
            style={{
              marginTop: '16px',
              marginBottom: '26px'
            }}
          />
          <TextField
            style={{ marginBottom: '15px' }}
            fullWidth
            error={!nameIsValid}
            helperText={helperText}
            variant='outlined'
            label='Data Series Name'
            value={dataSeries.name}
            onChange={(e) => setDataSeries({
              ...dataSeries,
              name: e.target.value
            })}
          />
          <TextField
            fullWidth
            variant='outlined'
            label='Description'
            value={dataSeries.description}
            onChange={(e) => setDataSeries({
              ...dataSeries,
              description: e.target.value
            })}
          />
        </CardContent>
      </Card>
      <StepperButtons
        onNextClick={handleForward}
        disableNext={!nameIsValid}
      />
    </>
  );
};
