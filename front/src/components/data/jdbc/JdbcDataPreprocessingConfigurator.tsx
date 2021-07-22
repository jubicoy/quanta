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

import {
  sample,
  getDataConnectionMetadata
} from '../../../client';

import StepperButtons from '../StepperButtons';
import { dataStyles } from '../DataStyles';
import {
  DataConnectionMetadata,
  SampleResponse,
  JdbcDataSeriesConfiguration
} from '../../../types';
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

  const classes = dataStyles();

  // States
  const [tables, setTables] = useState<string[]>([]);
  const [complete, setComplete] = useState<boolean>(false);

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

  return (
    <>
      <Card>
        <CardContent>
          <Grid container spacing={2}>
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
      <StepperButtons
        onNextClick={handleForward}
        disableNext={!complete}
      />
    </>);
};
