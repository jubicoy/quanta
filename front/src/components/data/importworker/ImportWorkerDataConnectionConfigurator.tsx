import React, { useState, useContext, useMemo } from 'react';
import { useWorkers } from '../../../hooks';
import { _DataConnectionConfiguratorContext } from '../DataImportPage';
import {
  MenuItem,
  Select,
  InputLabel,
  Typography as T,
  Card, CardContent, CardHeader
} from '@material-ui/core';
import {
  DataConnection,
  DataSeries,
  DEFAULT_IMPORT_WORKER_DATA_CONNECTION,
  DEFAULT_IMPORT_WORKER_DATA_SERIES
} from '../../../types';
import { sample, submitDataConnection } from '../../../client';
import StepperButtons from '../StepperButtons';
import { SampleResponse } from '../../../types/Api';

export const ImportWorkerDataConnectionConfigurator = () => {
  const {

    dataConnection,
    setDataConnection,
    setDataSeries,

    setSampleResponse,

    setSampleData,

    setSuccess,
    setError,

    handleForward
  } = useContext(_DataConnectionConfiguratorContext);

  const notDeleted = true;
  const workerQuery = useMemo(
    () => ({
      notDeleted
    }),
    [notDeleted]
  );

  const { workers } = useWorkers(workerQuery);

  const [worker, setWorker] = useState<any>(null);
  const [isLoading, setIsLoading] = useState<boolean>(false);

  const workerOptions = workers && workers.filter(w => w.definition.type === 'Import').map((worker: any, idx) =>
    <MenuItem key={idx} value={worker}>{worker.definition.name}</MenuItem>
  );

  const onHandleNext = () => {
    if (worker) {
      const updatedDataConnection: DataConnection = {
        ...DEFAULT_IMPORT_WORKER_DATA_CONNECTION,
        ...dataConnection,
        configuration: {
          type: 'IMPORT_WORKER',
          workerDefId: worker.id
        }
      };
      /// Submit DataConnection
      submitDataConnection(updatedDataConnection)
        .then((resDataConnection) => {
          setDataConnection(resDataConnection);

          // Create new base DataSeries to call /sample
          // Call backend to sample csv
          const sampleDataSeries: DataSeries = {
            ...DEFAULT_IMPORT_WORKER_DATA_SERIES,
            dataConnection: resDataConnection
          };
          console.log(sampleDataSeries);

          /// Sample DataSeries
          sample(resDataConnection.id, sampleDataSeries)
            .then((response: SampleResponse) => {
            // Set context sampleResponse to response
              setSampleResponse(response);
console.log(response);

              // Set context DataSeries/DataConnection to sampled one from response
              setDataSeries(response.dataSeries);

              // Set context sampleData from response
              setSampleData(response.data);

              setSuccess('DataConnection created successfully');
              handleForward();
            })
            .catch((e: Error) => {
              setIsLoading(false);
              setError('Failed to sample DataSeries', e);
            });
        })
        .catch((e: Error) => {
          setIsLoading(false);
          setError('Failed to create DataConnection', e);
        });
    }
  };

  return (
    <>
      <Card style={{ marginTop: '15px' }}>
        <CardHeader title='ImportWorker configuration' />
        <CardContent>
          <InputLabel
            style={{ fontSize: '20px' }}
            shrink
          >
            Select an Import Worker:
          </InputLabel>
          <Select
            fullWidth
            value={worker}
            onChange={e => {
              const value = e.target.value;
              setWorker(value);
            }}
          >
            {workerOptions}
          </Select>
          {worker
      && <div style={{ padding: '15px 0' }}>
        <T variant='body1'>Name: <b>{worker.definition.name}</b></T>
        <T variant='body1'>Description: <b>{worker.definition.description}</b></T>
      </div> }
        </CardContent>
      </Card>

      <StepperButtons
        onNextClick={onHandleNext}
        disableNext={!worker}
      />
    </>
  );
};
