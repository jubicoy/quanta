import React, { useContext } from 'react';
import {
  Grid,
  TextField,
  Typography
} from '@material-ui/core';

import { _DataConnectionConfiguratorContext } from '../DataImportPage';

import {
  CsvDataSeriesConfiguration,
  CsvDataConnectionConfiguration
} from '../../../types';

interface Props {
  handleCsvConfigurationChange: (configurations: CsvDataSeriesConfiguration) => void;
}

export const CsvConfigurationSelect = ({
  handleCsvConfigurationChange
}: Props) => {
  const {
    dataSeries,
    dataConnection
  } = useContext(_DataConnectionConfiguratorContext);

  const fileName = (dataConnection.configuration as CsvDataConnectionConfiguration).initialSyncFileName;
  const configurations: CsvDataSeriesConfiguration = (dataSeries.configuration as CsvDataSeriesConfiguration);

  return (
    <Grid container spacing={2}>
      <Grid item xs={12}>
        <Typography variant='button'>Server file name: {fileName}</Typography>
      </Grid>
      <Grid item xs={12}>
        <TextField
          label='Headers'
          helperText='Leave empty to use file header row, separate with vertical bar "|"'
          fullWidth
          value={configurations.headers ? configurations.headers.join('|') : ''}
          onChange={(event) => handleCsvConfigurationChange({
            ...configurations,
            headers: event.currentTarget.value.length === 0
              ? null
              : event.currentTarget.value
                .split('|')
          })}
        />
      </Grid>
      <Grid item xs={3}>
        <TextField
          label='Character set'
          margin='normal'
          fullWidth
          value={configurations.charset}
          onChange={(event) => handleCsvConfigurationChange({
            ...configurations,
            charset: event.currentTarget.value
          })}
        />
      </Grid>
      <Grid item xs={3}>
        <TextField
          label='Delimiter'
          margin='normal'
          fullWidth
          value={configurations.delimiter}
          onChange={(event) => handleCsvConfigurationChange({
            ...configurations,
            delimiter: event.currentTarget.value
          })}
        />
      </Grid>
      <Grid item xs={3}>
        <TextField
          label='Line Separator'
          margin='normal'
          fullWidth
          value={configurations.separator}
          onChange={(event) => handleCsvConfigurationChange({
            ...configurations,
            separator: event.currentTarget.value
          })}
        />
      </Grid>
      <Grid item xs={3}>
        <TextField
          label='Quote character'
          margin='normal'
          fullWidth
          value={configurations.quote}
          onChange={(event) => handleCsvConfigurationChange({
            ...configurations,
            quote: event.currentTarget.value
          })}
        />
      </Grid>
    </Grid>
  );
};
