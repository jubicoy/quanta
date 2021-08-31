import React, { useState } from 'react';
import {
  Stepper,
  Step,
  StepLabel
} from '@material-ui/core';
import { DataConnectionSelector } from './DataConnectionSelector';
import { DataConnectionPreview } from './DataConnectionPreview';
import { dataStyles } from './DataStyles';
import { DataLoader } from './DataLoader';

import { useAlerts } from '../../alert';

import {
  CsvDataPreprocessingConfigurator,
  CsvDataConnectionConfigurator
} from './csv';

import {
  JdbcDataConnectionConfigurator,
  JdbcDataPreprocessingConfigurator
} from './jdbc';

import {
  JsonIngestDataConnectionConfigurator,
  JsonIngestDataPreprocessingConfigurator
} from './json';

import {
  ImportWorkerDataConnectionConfigurator,
  ImportWorkerDataPreprocessingConfigurator
} from './importworker';

import {
  FileUploadResponse,
  DataConnection,
  DataSeries,
  DEFAULT_JDBC_DATA_CONNECTION,
  DEFAULT_JDBC_DATA_SERIES,
  Worker
} from '../../types';

import { DataConnectionType } from '../../types/DataConnections';
import { SampleResponse } from '../../types/Api';

import { _DataConnectionConfiguratorContext } from '../context';

function getSteps () {
  return [
    'Type',
    'Connection',
    'Select',
    'Confirm'
  ];
}

export const DataImportPage = () => {
  const classes = dataStyles();

  // States
  const [activeStep, setActiveStep] = React.useState<number>(0);

  const [uploadedData, setUploadedData] = React
    .useState<FileUploadResponse | null>(null);

  const [selectedWorker, setSelectedWorker] = React
    .useState<Worker | null>(null);

  const [dataConnection, setDataConnection] = useState<DataConnection>(DEFAULT_JDBC_DATA_CONNECTION);
  const [dataSeries, setDataSeries] = React
    .useState<DataSeries>(DEFAULT_JDBC_DATA_SERIES);

  const [sampleResponse, setSampleResponse] = React
    .useState<SampleResponse | null>(null);

  const [sampleData, setSampleData] = React
    .useState<string[][]>([]);

  const [uploadProgress, setUploadProgress] = React
    .useState<number | null>(null);

  // Alert handlers
  const alertContext = useAlerts('DATA-IMPORT');
  const setSuccess = (heading: string) => {
    alertContext.alertSuccess(heading, '');
  };
  const setError = (heading: string, error: Error) => {
    alertContext.alertError(heading, error.message);
  };

  const reset = () => {
    setActiveStep(0);
    setDataConnection(DEFAULT_JDBC_DATA_CONNECTION);
    setDataSeries(DEFAULT_JDBC_DATA_SERIES);
    setUploadedData(null);
    setSelectedWorker(null);
    setSampleResponse(null);
    setSampleData([]);
  };

  const onSubmitDataSeriesSuccess = () => {
    return undefined;
  };

  // Stepper handlers
  const steps = getSteps();
  const handleForward = () => {
    setActiveStep(activeStep + 1);
  };
  const handleBack = () => {
    const newActiveStep = activeStep - 1;
    // TODO: Don't revert DataConnection when backing to step 0?
    switch (newActiveStep) {
      case 0:
        reset();
        break;
      default:
        break;
    }
    setActiveStep(newActiveStep);
  };

  // Main handlers

  const handleDataConnectionTypeSelection = (typeSelection: DataConnectionType) => {
    // Set type and continue Stepper
    switch (typeSelection) {
      case 'CSV':
        setDataConnection({
          ...dataConnection,
          type: 'CSV'
        });
        break;

      case 'JDBC':
        setDataConnection({
          ...dataConnection,
          type: 'JDBC'
        });
        break;

      case 'JSON_INGEST':
        setDataConnection({
          ...dataConnection,
          type: 'JSON_INGEST'
        });
        break;

      case 'IMPORT_WORKER':
        setDataConnection({
          ...dataConnection,
          type: 'IMPORT_WORKER'
        });
        break;

      default:
        break;
    }

    handleForward();
  };

  return (
    <React.Fragment>
      <Stepper className={classes.stepper} nonLinear activeStep={activeStep}>
        {steps.map((label, index) => (
          <Step key={'data-connection-step-' + index}>
            <StepLabel>{label}</StepLabel>
          </Step>
        ))}
      </Stepper>
      <div>
        <_DataConnectionConfiguratorContext.Provider value={{
          // Map context to state-setters
          uploadedData: uploadedData,
          setUploadedData: setUploadedData,
          setUploadProgress: setUploadProgress,

          selectedWorker: selectedWorker,
          setSelectedWorker: setSelectedWorker,

          dataConnection: dataConnection,
          setDataConnection: setDataConnection,
          dataSeries: dataSeries,
          setDataSeries: setDataSeries,
          onSubmitDataSeriesSuccess: onSubmitDataSeriesSuccess,

          sampleResponse: sampleResponse,
          setSampleResponse: setSampleResponse,

          sampleData: sampleData,
          setSampleData: setSampleData,

          setSuccess: setSuccess,
          setError: setError,

          handleForward: handleForward,
          handleBack: handleBack
        }}>
          {(() => {
            switch (activeStep) {
              case 0:
                return <DataConnectionSelector
                  onTypeSelect={(typeSelection) =>
                    handleDataConnectionTypeSelection(typeSelection)
                  }
                />;
              case 1:
                switch (dataConnection.type) {
                  case 'CSV':
                    return <CsvDataConnectionConfigurator />;
                  case 'JDBC':
                    return <JdbcDataConnectionConfigurator />;
                  case 'JSON_INGEST':
                    return <JsonIngestDataConnectionConfigurator />;
                  case 'IMPORT_WORKER':
                    return <ImportWorkerDataConnectionConfigurator />;

                  default:
                    return null;
                }
              case 2:
                switch (dataConnection.type) {
                  case 'CSV':
                    return <CsvDataPreprocessingConfigurator />;
                  case 'JDBC':
                    return <JdbcDataPreprocessingConfigurator />;
                  case 'JSON_INGEST':
                    return <JsonIngestDataPreprocessingConfigurator />;
                  case 'IMPORT_WORKER':
                    return <ImportWorkerDataPreprocessingConfigurator />;
                  default:
                    return null;
                }
              case 3:
                return (
                  <DataConnectionPreview
                    reset={reset}
                  />
                );
              default:
                return null;
            }
          })()}
        </_DataConnectionConfiguratorContext.Provider>
      </div>
      <DataLoader
        isLoading={uploadProgress !== null}
        message='Uploading'
        progressive
        progress={uploadProgress !== null ? uploadProgress : 0}
      />
    </React.Fragment>
  );
};
