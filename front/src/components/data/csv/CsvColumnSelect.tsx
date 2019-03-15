import React from 'react';

import {
  Button,
  Divider,
  Grid,
  Icon
} from '@material-ui/core';

import { dataStyles } from '../DataStyles';

import { CsvConfigurationSelect } from '.';
import { SampleTable } from '..';
import {
  CsvDataSeriesConfiguration
} from '../../../types';

interface Props {
  handleCsvConfigurationChange: (configurations: CsvDataSeriesConfiguration) => void;
  handleCsvConfigurationReset: () => void;
}

export const CsvColumnSelect = ({
  handleCsvConfigurationChange,
  handleCsvConfigurationReset
}: Props) => {
  const classes = dataStyles();
  return (
    <Grid container>
      <CsvConfigurationSelect
        handleCsvConfigurationChange={handleCsvConfigurationChange}
      />
      <Divider />
      <SampleTable
        editableType
      />
      <Divider />
      <Grid container spacing={2}>
        <Grid item>
          <Button
            className={classes.topSpacing}
            onClick={handleCsvConfigurationReset}
            variant='outlined'>
            Reset all
            <Icon className='button-icon right'>cancel</Icon>
          </Button>
        </Grid>
      </Grid>
    </Grid>
  );
};
