import React, { useState, useContext, useEffect } from 'react';

import { _DataConnectionConfiguratorContext } from '../DataImportPage';
import { SampleTable } from '..';
import StepperButtons from '../StepperButtons';
import {
  Grid,
  Typography as T,
  Card,
  CardContent
} from '@material-ui/core';
export const ImportWorkerDataPreprocessingConfigurator = () => {
  const {
    dataSeries,
    selectedWorker,
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

  return (selectedWorker) ? (
    <>
      <Card>
        <CardContent>
          <Grid item xs={12}>
            <T variant='button'>Worker Name: <b>{selectedWorker.definition.name}</b></T>
          </Grid>
          <Grid item xs={12}>
            <T variant='button'>Description: <b>{selectedWorker.definition.description}</b></T>
          </Grid>
          <SampleTable
            editableType={false}
          />
        </CardContent>
      </Card>
      <StepperButtons
        onNextClick={handleForward}
        disableNext={!complete}
      />
    </>
  ) : <div>Loading</div>;
};
