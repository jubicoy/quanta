import React, { useContext, useMemo } from 'react';
import {
  useDataSeriesNameCheck,
  useWorkerDefs
} from '../../../hooks';
import { _DataConnectionConfiguratorContext } from '../../context';
import {
  MenuItem,
  Select,
  InputLabel,
  Typography as T,
  Card, CardContent, CardHeader,
  Divider,
  TextField
} from '@material-ui/core';
import {
  DataConnection,
  DataSeries,
  WorkerDef,
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

    dataSeries,
    setDataSeries,

    setSampleResponse,

    setSampleData,

    setSuccess,
    setError,

    selectedWorkerDef,
    setSelectedWorkerDef,

    handleForward
  } = useContext(_DataConnectionConfiguratorContext);

  const workerDefQuery = useMemo(
    () => ({
      workerType: 'Import',
      notDeleted: true
    }),
    []
  );

  const { workerDefs } = useWorkerDefs(workerDefQuery);
  const { nameIsValid, helperText } = useDataSeriesNameCheck(dataSeries.name);

  const workerDefOptions = workerDefs && workerDefs.map((workerDef: WorkerDef, idx) =>
    <MenuItem key={idx} value={workerDef.name}>{workerDef.name}</MenuItem>
  );

  const onHandleNext = () => {
    if (selectedWorkerDef) {
      const updatedDataConnection: DataConnection = {
        ...DEFAULT_IMPORT_WORKER_DATA_CONNECTION,
        ...dataConnection,
        configuration: {
          type: 'IMPORT_WORKER',
          workerDefId: selectedWorkerDef.id
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
            ...dataSeries,
            dataConnection: resDataConnection
          };
          /// Sample DataSeries
          sample(resDataConnection.id, sampleDataSeries)
            .then((response: SampleResponse) => {
            // Set context sampleResponse to response
              setSampleResponse(response);

              // Set context DataSeries/DataConnection to sampled one from response
              setDataSeries(response.dataSeries);

              // Set context sampleData from response
              setSampleData(response.data);

              setSuccess('DataConnection created successfully');
              handleForward();
            })
            .catch((e: Error) => {
              setError('Failed to sample DataSeries', e);
            });
        })
        .catch((e: Error) => {
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
            value={selectedWorkerDef?.name}
            onChange={e => setSelectedWorkerDef(
              workerDefs?.find(worker => worker.name === e.target.value) || null)
            }
          >
            {workerDefOptions}
          </Select>
          {selectedWorkerDef
            && <div style={{ padding: '15px 0' }}>
              <T variant='body1'>Name: <b>{selectedWorkerDef.name}</b></T>
              <T variant='body1'>Description: <b>{selectedWorkerDef.description}</b></T>
            </div> }
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
        onNextClick={onHandleNext}
        disableNext={!selectedWorkerDef || !nameIsValid}
      />
    </>
  );
};
