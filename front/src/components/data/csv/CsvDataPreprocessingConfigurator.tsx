import React, { useState, useContext, useEffect } from 'react';

import { CsvColumnSelect } from '.';

import { _DataConnectionConfiguratorContext } from '../DataImportPage';

import {
  CsvDataSeriesConfiguration
} from '../../../types';
import StepperButtons from '../StepperButtons';

export const CsvDataPreprocessingConfigurator = () => {
  const {
    uploadedData,

    dataSeries,
    setDataSeries,

    sampleResponse,

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
      configuration: configurations
    });
  };

  const handleCsvConfigurationReset = () => {
    // Reset everything
    if (sampleResponse && sampleResponse.dataSeries.columns) {
      setDataSeries(sampleResponse.dataSeries);
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
        <StepperButtons
          onNextClick={handleForward}
          disableNext={!complete}
        />
      </>
    ) : <div>Loading</div>;
};
