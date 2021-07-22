import React, { useState, useEffect } from 'react';
import {
  Stepper,
  Step,
  StepLabel
} from '@material-ui/core';

import { DataConnectionPreview } from './DataConnectionPreview';

import { dataStyles } from './DataStyles';
import { DataLoader } from './DataLoader';
import * as client from '../../client';
import { useAlerts } from '../../alert';

import { useRouter } from '../../hooks';

import {
  JdbcDataPreprocessingConfigurator
} from './jdbc';

import {
  FileUploadResponse,
  DataSeries,
  DataConnection,
  DEFAULT_JDBC_DATA_CONNECTION,
  DEFAULT_JDBC_DATA_SERIES
} from '../../types';

import { SampleResponse } from '../../types/Api';

import { _DataConnectionConfiguratorContext } from '../context';

function getSteps () {
  return [
    'Select',
    'Confirm'
  ];
}

interface Props {
  match: {
    params: {
      id: number;
      name: string;
    };
  };
}

export const EditDataSeries = ({ match: { params } }: Props) => {
  const classes = dataStyles();
  const { history } = useRouter();

  // States
  const [uploadedData, setUploadedData] = React
    .useState<FileUploadResponse | null>(null);
  const [activeStep, setActiveStep] = React.useState<number>(0);

  const [dataConnection, setDataConnection] = useState<DataConnection>(DEFAULT_JDBC_DATA_CONNECTION);
  const [dataSeries, setDataSeries] = React
    .useState<DataSeries>(DEFAULT_JDBC_DATA_SERIES);

  const [sampleResponse, setSampleResponse] = React
    .useState<SampleResponse | null>(null);

  const [sampleData, setSampleData] = React
    .useState<string[][]>([]);

  const [uploadProgress, setUploadProgress] = React
    .useState<number | null>(null);

  useEffect(
    () => {
      if (params && params.id !== -1) {
        client.getDataConnection(params.id)
          .then((conn) => {
            const newDataSeries: DataSeries = {
              ...dataSeries,
              dataConnection: conn
            };
            setDataConnection(conn);
            setDataSeries(newDataSeries);
          })
          .catch((e) => console.log(e)
          );
      }
    },
    [params]
  );

  // Alert handlers
  const alertContext = useAlerts('DATA-IMPORT');
  const setSuccess = (heading: string) => {
    alertContext.alertSuccess(heading, '');
  };
  const setError = (heading: string, error: Error) => {
    alertContext.alertError(heading, error.message);
  };

  const reset = () => {
    // remove to fix leak application in useEffect
    return undefined;
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
    if (dataSeries.dataConnection) {
      history.push(`/data-connections/${dataSeries.dataConnection.id}`);
    }
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
                return <JdbcDataPreprocessingConfigurator />;

              case 1:
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
