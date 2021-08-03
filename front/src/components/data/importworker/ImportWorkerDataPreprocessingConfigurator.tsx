import React, { useState, useContext, useEffect } from 'react';

import { _DataConnectionConfiguratorContext } from '../DataImportPage';

import StepperButtons from '../StepperButtons';

export const ImportWorkerDataPreprocessingConfigurator = () => {
  const {
    uploadedData,

    dataSeries,

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

  return (uploadedData)
    ? (
      <>
        <StepperButtons
          onNextClick={handleForward}
          disableNext={!complete}
        />
      </>
    ) : <div>Loading</div>;
};
