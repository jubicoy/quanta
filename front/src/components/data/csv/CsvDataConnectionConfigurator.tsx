import React, { useContext, useState, useEffect } from 'react';

import { _DataConnectionConfiguratorContext } from '../../context';

import { FileSelect } from '..';

import {
  Card,
  CardContent,
  TextField,
  InputLabel,
  CardHeader,
  Divider
} from '@material-ui/core';
import StepperButtons from '../StepperButtons';
import { dataStyles } from '../DataStyles';

import { uploadFile, submitDataConnection } from '../../../client';
import { useDataSeriesNameCheck } from '../../../hooks';
import {
  DataConnection,
  DataSeries,
  DEFAULT_CSV_DATA_CONNECTION,
  FileUploadResponse
} from '../../../types';

// Set path, settings and upload csv
// Call both upload and sample in this step
export const CsvDataConnectionConfigurator = () => {
  const {
    uploadedData,
    setUploadedData,
    setUploadProgress,

    dataConnection,
    dataSeries,
    setDataConnection,
    setDataSeries,

    setSuccess,
    setError,

    handleForward
  } = useContext(_DataConnectionConfiguratorContext);

  const { nameIsValid, helperText } = useDataSeriesNameCheck(dataSeries.name);

  const [selectedDataFile, setSelectedDataFile] = useState<File | null>(null);
  const [path, setPath] = useState<string>('/');
  const [complete, setComplete] = useState<boolean>(false);
  const [isLoading, setIsLoading] = useState<boolean>(false);

  useEffect(() => {
    if (selectedDataFile != null && path.length > 0) {
      setComplete(true);
    }
    else {
      setComplete(false);
    }
  }, [selectedDataFile, path]);

  const onLoadFile = (file: File | null) => {
    setSelectedDataFile(file);

    // if anything changes, consider a reupload situation
    setUploadedData(null);
  };

  const uploadDataFile = () => {
    if (uploadedData) {
      // If file is uploaded
      handleForward();
      return;
    }

    if (selectedDataFile && path) {
      setUploadProgress(0);
      setIsLoading(true);
      // v2
      uploadFile(
        { file: selectedDataFile },
        (progress: { percentage: number | null }) => setUploadProgress(progress.percentage)
      )
        .then((uploaded: FileUploadResponse) => {
          // Upload complete
          // Reset upload progress
          setUploadProgress(null);

          // Create new DataConnection containing correct fileName returned from backend
          // Path is as default '/'
          const updatedDataConnection: DataConnection = {
            ...DEFAULT_CSV_DATA_CONNECTION,
            ...dataConnection,
            configuration: {
              type: 'CSV',
              initialSyncFileName: uploaded.fileName,
              path: path
            }
          };

          setIsLoading(false);
          // Save upload response to uploadedData
          setUploadedData(uploaded);
          setSuccess('File upload successful');

          /// Submit DataConnection
          submitDataConnection(updatedDataConnection)
            .then((resDataConnection) => {
              setDataConnection(resDataConnection);

              // Create new base DataSeries to call /sample
              // Call backend to sample csv
              const sampleDataSeries: DataSeries = {
                ...dataSeries,
                dataConnection: resDataConnection
              };
              setDataSeries(sampleDataSeries);
              handleForward();
            })
            .catch((e: Error) => {
              setIsLoading(false);
              setError('Failed to create DataConnection', e);
            });
        })
        .catch((e: Error) => {
          setIsLoading(false);
          setError('Failed to upload file', e);
        });
    }
  };

  const classes = dataStyles();

  return (
    <>
      <Card style={{ marginTop: '15px' }}>
        <CardHeader title='CSV configuration' />
        <CardContent>
          <TextField
            style={{ marginBottom: '15px' }}
            fullWidth
            error={!nameIsValid}
            helperText={helperText}
            label='Data Series Name'
            value={dataSeries.name}
            onChange={e => setDataSeries({
              ...dataSeries,
              name: e.target.value
            })}
          />
          <TextField
            style={{ marginBottom: '15px' }}
            fullWidth
            label='Data Series Description'
            value={dataSeries.description}
            onChange={e => setDataSeries({
              ...dataSeries,
              description: e.target.value
            })}
          />
          <Divider
            style={{
              marginTop: '16px',
              marginBottom: '26px'
            }}
          />
          <form>
            <TextField
              // TODO: Remove this afterwards
              disabled
              id='csv-data-connection-directory'
              label='Sync path'
              value={path}
              onChange={e => setPath(e.target.value as string)}
            />
            <InputLabel className={classes.topSpacing} shrink>Entry file</InputLabel>
            <FileSelect
              file={selectedDataFile}
              accept='.csv'
              onChange={(file) => onLoadFile(file)}
              onClear={() => onLoadFile(null)}
            />
          </form>
        </CardContent>
      </Card>
      <StepperButtons
        onNextClick={uploadDataFile}
        disableNext={!complete || isLoading || !nameIsValid}
      />
    </>
  );
};
