import React, { useState, useContext, useEffect } from 'react';
import { Button, Grid } from '@material-ui/core';

import { CsvColumnSelect } from '.';

import { _DataConnectionConfiguratorContext } from '../../context';

import {
  CsvDataSeriesConfiguration
} from '../../../types';
import { SampleResponse } from '../../../types/Api';
import StepperButtons from '../StepperButtons';
import { sample } from '../../../client';

export const CsvDataPreprocessingConfigurator = () => {
  const {
    uploadedData,

    dataSeries,
    setDataSeries,

    setSampleResponse,
    sampleResponse,

    setSampleData,

    setSuccess,
    setError,

    handleForward
  } = useContext(_DataConnectionConfiguratorContext);

  const selectedColumns = [...dataSeries.columns];

  const [complete, setComplete] = useState<boolean>(false);

  useEffect(() => {
    if (selectedColumns.length > 0) {
      setComplete(true);
    }
    else {
      setComplete(false);
    }
  }, [selectedColumns]);

  const handleCsvConfigurationChange = (configurations: CsvDataSeriesConfiguration) => {
    setDataSeries({
      ...dataSeries,
      columns: [],
      configuration: configurations
    });
  };

  const handleCsvConfigurationReset = () => {
    // Reset everything
    if (sampleResponse && sampleResponse.dataSeries.columns) {
      setDataSeries(sampleResponse.dataSeries);
    }
  };

  const handleSample = (): void => {
    // Reset sample
    setSampleResponse(null);
    setSampleData([]);
    if (dataSeries.dataConnection) {
      sample(dataSeries.dataConnection.id, dataSeries)
        .then((res: SampleResponse) => {
          setDataSeries(res.dataSeries);
          setSampleResponse(res);
          setSampleData(res.data);
          setSuccess('Fetch sample successfully');
        })
        .catch((e: Error) => {
          setError('Sample failed to execute', e);
        });
    }
  };

  return (uploadedData)
    ? (
      <>
        <CsvColumnSelect
          // Handlers
          handleCsvConfigurationChange={handleCsvConfigurationChange}
          handleCsvConfigurationReset={handleCsvConfigurationReset}
        />
        <Grid
          style={{
            padding: '10px'
          }}
          item
          xs={6}>
          <Button
            color='primary'
            variant='contained'
            onClick={handleSample}
          >
            Fetch Sample
          </Button>
        </Grid>
        <StepperButtons
          onNextClick={handleForward}
          disableNext={!complete}
        />
      </>
    ) : <div>Loading</div>;
};
