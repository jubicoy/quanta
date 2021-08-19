import React, { useState, useContext, useEffect } from 'react';

import {
  Paper,
  Button,
  Card,
  CardContent,
  Grid,
  List,
  ListItem,
  TextField,
  Tooltip,
  Typography
} from '@material-ui/core';

import { _DataConnectionConfiguratorContext } from '../../context';
import Autocomplete from '@material-ui/lab/Autocomplete';
import Chip from '@material-ui/core/Chip';

import {
  sample,
  getDataConnectionMetadata,
  updateDataConnectionTags
} from '../../../client';
import { useDataSeriesNameCheck } from '../../../hooks';
import StepperButtons from '../StepperButtons';
import { dataStyles } from '../DataStyles';
import {
  DataConnectionMetadata,
  SampleResponse,
  JdbcDataSeriesConfiguration
} from '../../../types';
import {
  useTags
} from '../../../hooks';
import { SampleTable } from '..';

export const JdbcDataPreprocessingConfigurator = () => {
  const {
    dataSeries,
    setDataSeries,
    setSampleResponse,
    sampleData,
    setSampleData,
    setSuccess,
    setError,
    handleForward
  } = useContext(_DataConnectionConfiguratorContext);
  const { nameIsValid, helperText } = useDataSeriesNameCheck(dataSeries.name);

  const classes = dataStyles();

  // States
  const [tables, setTables] = useState<string[]>([]);
  const [complete, setComplete] = useState<boolean>(false);
  const [tag, setTag] = useState<string[]>();

  const { tags } = useTags();
  const names = tags && tags.map(({ name }) => name);

  useEffect(() => {
    // Get tables at start
    if (dataSeries.dataConnection) {
      getDataConnectionMetadata(
        dataSeries.dataConnection.id
      ).then((res: DataConnectionMetadata) => {
        if (
          res.jdbcDataConnectionMetadata
          && res.jdbcDataConnectionMetadata.tables.length > 0
        ) {
          setTables(res.jdbcDataConnectionMetadata.tables);
        }
        else {
          setError('No table found.', new Error(''));
        }
      }).catch((e: Error) => {
        setError('Failed to get JDBC metadata', e);
      });
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [dataSeries]);

  useEffect(() => {
    setComplete(
      (dataSeries
      && dataSeries.dataConnection
      && dataSeries.configuration
      && dataSeries.dataConnection
      && dataSeries.dataConnection.configuration
      && sampleData.length > 0) as boolean
    );
  }, [dataSeries, sampleData]);

  const handleSetQuery = (query: string): void => {
    const newJdbcSeriesConfiguration = {
      ...(dataSeries.configuration as JdbcDataSeriesConfiguration),
      query: query
    };
    const newDataSeries = {
      ...dataSeries,
      columns: [],
      configuration: newJdbcSeriesConfiguration
    };

    setDataSeries(newDataSeries);
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
          setSuccess('Sample query executed successfully');
        })
        .catch((e: Error) => {
          setError('Sample query failed to execute', e);
        });
    }
  };

  const nextStep = () => {
    if (dataSeries.dataConnection && tag) {
      updateDataConnectionTags(dataSeries.dataConnection.id, tag).catch((e: Error) => {
        setError('Fail to add tags', e);
      });
    }
    handleForward();
  };

  return (
    <>
      <Card>
        <CardContent>
          <Grid container spacing={2}>
            <Grid item xs={12}>
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
            </Grid>
            <Grid item xs={12}>
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
            </Grid>
            <Grid item xs={6}>
              <Typography
                variant='h6'
                style={{
                  marginBottom: '4px'
                }}
              >
                DataSeries SQL Query
              </Typography>
              <TextField
                fullWidth
                multiline
                variant='outlined'
                color='primary'
                value={(dataSeries.configuration as JdbcDataSeriesConfiguration).query}
                onChange={e => {
                  handleSetQuery(e.target.value as string);
                  setComplete(false);
                }}
                /* InputProps != inputProps */
                InputProps={{
                  className: classes.queryField
                }}
                inputProps={{
                  style: {
                    height: '100%',
                    overflowY: 'scroll'
                  }
                }}
              />
              <Button
                className={classes.topSpacing}
                disabled={(dataSeries.configuration as JdbcDataSeriesConfiguration).query.length === 0}
                color='primary'
                variant='contained'
                onClick={handleSample}
              >
                Fetch Sample
              </Button>
            </Grid>
            <Grid item xs={6}>
              <Typography
                variant='h6'
                style={{
                  marginBottom: '4px'
                }}
              >
                Tables List
              </Typography>
              <Tooltip
                title='Click for "select * from table" query'
                placement='bottom-start'
                disableFocusListener
                disableTouchListener
              >
                <Paper variant='outlined'>
                  <List
                    className={classes.dbTableList}
                  >
                    {tables.slice().sort((a, b) => b.localeCompare(a)).map((table, i) => (
                      <ListItem
                        key={i}
                        className={classes.tableListItem}
                        onClick={() => handleSetQuery('select * from ' + table)}
                      >
                        {table}
                      </ListItem>
                    ))}
                  </List>
                </Paper>
              </Tooltip>
            </Grid>
            <SampleTable
              editableType={false}
            />
          </Grid>
        </CardContent>
      </Card>
      <Autocomplete
        multiple
        size='small'
        style={{ marginTop: '15px' }}
        options={names || []}
        freeSolo
        onChange={(event, value) => setTag(value)}
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
        onNextClick={handleForward}
        disableNext={!complete || !nameIsValid}
      />
    </>);
};
