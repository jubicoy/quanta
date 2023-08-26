import React, { useState, useContext, useEffect } from 'react';
import { Button, Grid, TextField } from '@material-ui/core';

import { CsvColumnSelect } from '.';

import { _DataConnectionConfiguratorContext } from '../../context';

import Autocomplete from '@material-ui/lab/Autocomplete';
import Chip from '@material-ui/core/Chip';
import {
  CsvDataSeriesConfiguration
} from '../../../types';
import {
  useTags
} from '../../../hooks';
import { SampleResponse } from '../../../types/Api';
import StepperButtons from '../StepperButtons';
import { sample, updateDataConnection } from '../../../client';

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
  const { tags } = useTags();

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

  const nextStep = () => {
    if (dataSeries.dataConnection && dataSeries.dataConnection.tags.length > 0) {
      updateDataConnection(dataSeries.dataConnection)
        .catch((e: Error) => {
          setError('Fail to add tags', e);
        });
    }

    handleForward();
  };

  const addTags = (value: string[]) => {
    if (dataSeries && dataSeries.dataConnection) {
      dataSeries.dataConnection.tags = value;
      setDataSeries(dataSeries);
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
        <Autocomplete
          multiple
          size='small'
          style={{ marginTop: '15px' }}
          options={tags || []}
          freeSolo
          onChange={(event, value) => addTags(value)}
          renderTags={(value, getTagProps) =>
            value.map((option, index) =>
              <Chip key={index} variant='outlined' label={option} {...getTagProps({ index })} />
            )
          }
          renderInput={(params) => (
            <TextField
              {...params}
              variant='outlined'
              label='Add Tag'
              placeholder='Tag'
            />
          )}
        />
        <StepperButtons
          onNextClick={nextStep}
          disableNext={!complete}
        />
      </>
    ) : <div>Loading</div>;
};
