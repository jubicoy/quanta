import React, { useState, useEffect } from 'react';

import {
  Paper,
  Grid,
  Typography,
  TextField,
  Divider,
  InputLabel,
  Icon,
  Button,
  FormControl,
  Select,
  MenuItem,
  FormControlLabel,
  Switch,
  TableContainer,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  Collapse,
  IconButton
} from '@material-ui/core';
import * as Colors from '@material-ui/core/colors';

import {
  DEFAULT_COLUMN,
  SupportedColumnClasses,
  SampleResponse,
  DataSeries,
  JsonIngestDataSeriesConfiguration,
  Column,
  DataConnection
} from '../../../types';
import { sample } from '../../../client';
import { useAlerts } from '../../../alert';

interface PathColumnMapping {
  jsonPath: string;
  column: Column;
};

interface Props {
  dataConnection: DataConnection;
  dataSeries: DataSeries;
  setDataSeries: (value: DataSeries) => void;
  sampleJson: string;
  readOnly?: true;
}

export const JsonIngestDataSeriesConfigurator = (props: Props) => {
  // Alert handlers
  const alertContext = useAlerts('DATA-IMPORT');
  const setError = (heading: string, error: Error) => {
    alertContext.alertError(heading, error.message);
  };

  const {
    dataConnection,
    dataSeries,
    setDataSeries,
    sampleJson
  } = props;
  const readOnly: boolean = props.readOnly || false;

  const DEFAULT_PATH_COLUMN_MAPPING = {
    jsonPath: '',
    column: {
      ...DEFAULT_COLUMN
    }
  };

  const [pathColumnMappings, setPathColumnMappings] = useState<PathColumnMapping[]>([]);

  useEffect(
    () => {
      // Map initial DataSeries to internal mappings
      if (
        pathColumnMappings.length <= 0
        && dataSeries
      ) {
        const columns = dataSeries.columns;
        const paths = (dataSeries.configuration as JsonIngestDataSeriesConfiguration).paths;

        if (!columns || !paths) {
          return;
        }

        if (columns.length !== paths.length) {
          throw new Error('Invalid DataSeries');
        }

        const newMappings = columns.map((column, i) => {
          const jsonPath = paths[i];
          return {
            jsonPath,
            column
          };
        });

        setPathColumnMappings(newMappings);
      }
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [dataSeries]
  );

  const buildDataSeriesFromMappings = (inputPathColumnsMappings: PathColumnMapping[], inputDataSeries: DataSeries): DataSeries => {
    return {
      ...inputDataSeries,
      columns: inputPathColumnsMappings.map(({ column }, i) => {
        return {
          ...column,
          index: i
        };
      }),
      configuration: {
        ...(inputDataSeries.configuration as JsonIngestDataSeriesConfiguration),
        paths: inputPathColumnsMappings.map(({ jsonPath }) => jsonPath)
      }
    };
  };

  useEffect(
    () => {
      if (pathColumnMappings !== []) {
        setDataSeries(buildDataSeriesFromMappings(pathColumnMappings, dataSeries));
      }
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [pathColumnMappings]
  );

  const [isClosed, setIsClosed] = useState<boolean>(false);

  const [sampleResponse, setSampleResponse] = useState<SampleResponse | null>(null);

  const addColumn = () => {
    const newMappings = [...pathColumnMappings, DEFAULT_PATH_COLUMN_MAPPING];

    setPathColumnMappings(newMappings);
  };

  const onChangeJsonPaths = (event: { target: { value: string } }, index: number) => {
    const value = event.target.value;
    const newMappings = [...pathColumnMappings];
    newMappings[index] = {
      ...newMappings[index],
      jsonPath: value
    };
    setPathColumnMappings(newMappings);
  };

  const onChangeDataSeries = (keyName: string, keyValue: unknown) => {
    const newDataSeries = {
      ...dataSeries,
      [keyName]: keyValue
    };

    setDataSeries(newDataSeries);
  };

  const onChangeColumns = (event: { target: { name?: string; value: unknown } }, index: number) => {
    const key = event.target.name || '';
    const value = event.target.value;

    const newMappings = [...pathColumnMappings];
    let newColumnInMapping = { ...pathColumnMappings[index].column };

    if (key === 'name') {
      newColumnInMapping = {
        ...newColumnInMapping,
        'name': (value as string)
      };
    }
    else {
      // Changing type
      newColumnInMapping = {
        ...newColumnInMapping,
        type: {
          ...newColumnInMapping.type,
          [key]: value
        }
      };
    }

    newMappings[index] = {
      ...newMappings[index],
      column: newColumnInMapping
    };

    setPathColumnMappings(newMappings);
  };

  const onDeleteMapping = (index: number) => {
    const newMappings = [...pathColumnMappings];
    newMappings.splice(index, 1);

    setPathColumnMappings(newMappings);
  };

  const onFetchSample = () => {
    if (
      !dataConnection
      || dataConnection.id < 0
    ) {
      return;
    }

    sample(
      dataConnection.id,
      {
        ...dataSeries,
        configuration: {
          ...dataSeries.configuration,
          sampleJsonDocument: sampleJson
        } as JsonIngestDataSeriesConfiguration
      }
    )
      .then(sampleResponse => {
        setSampleResponse((sampleResponse && sampleResponse.data.length > 0) ? sampleResponse : null);
      })
      .catch((e: Error) => {
        setSampleResponse(null);
        setError('Fetch sample failed!', e);
      });
  };

  const renderColumnsConfigurators = () => {
    return pathColumnMappings.map(({ jsonPath, column }, i) =>
      <div key={i}>
        <Divider
          key={'div' + i}
          style={{
            margin: '14px 0'
          }}
        />
        <Grid key={'grid' + i} container spacing={0} justify='space-evenly'>
          <Grid item xs={3} style={{
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'center',
            paddingLeft: '10px'
          }}>
            <TextField
              size='medium'
              variant='outlined'
              label='JSON Path'
              value={jsonPath}
              InputProps={{
                readOnly: readOnly
              }}
              onChange={(e) => onChangeJsonPaths(e, i)}
            />
          </Grid>
          <Grid item xs={1} style={{
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'center'
          }}>
            <Icon
              style={{
                margin: 'auto'
              }}
            >arrow_right_alt</Icon>
          </Grid>
          <Grid item xs={7}>
            <Paper variant='outlined' style={{
              padding: '16px'
            }}>
              <Typography
                variant='body1'
                style={{
                  fontWeight: 'bold',
                  marginBottom: '12px'
                }}
              >
                Column
              </Typography>
              <Grid
                container
                spacing={2}
              >
                <Grid item xs={3}>
                  <TextField
                    InputProps={{
                      readOnly: readOnly
                    }}
                    fullWidth
                    label='Name'
                    name='name'
                    value={column.name}
                    onChange={(e) => onChangeColumns(e, i)}
                    InputLabelProps={{
                      shrink: true
                    }}
                  />
                </Grid>
                <Grid item xs={4}>
                  <FormControl fullWidth>
                    <InputLabel shrink id='class-select-label'>Class</InputLabel>
                    <Select
                      inputProps={{
                        readOnly: readOnly
                      }}
                      name='className'
                      labelId='class-select-label'
                      value={column.type.className}
                      onChange={(e) => onChangeColumns(e, i)}
                    >
                      {
                        SupportedColumnClasses.map(cls =>
                          <MenuItem key={cls} value={cls}>{cls}</MenuItem>
                        )
                      }
                    </Select>
                  </FormControl>
                </Grid>
                <Grid item xs={3}>
                  <TextField
                    InputProps={{
                      readOnly: readOnly
                    }}
                    fullWidth
                    label='Format'
                    name='format'
                    value={column.type.format || ''}
                    onChange={(e) => onChangeColumns(e, i)}
                    InputLabelProps={{
                      shrink: true
                    }}
                  />
                </Grid>
                <Grid item xs={2}>
                  <FormControl fullWidth>
                    <InputLabel shrink id='nullable-switch-label'>Nullable</InputLabel>
                    <FormControlLabel
                      style={{
                        marginTop: '16px'
                      }}
                      control={
                        <Switch
                          readOnly
                          contentEditable={false}
                          checked={column.type.nullable || false}
                          name='nullable'
                          onChange={
                            readOnly
                              ? undefined
                              : (e) => onChangeColumns(
                                {
                                  target: {
                                    name: 'nullable',
                                    value: e.target.checked
                                  }
                                },
                                i
                              )
                          }
                        />
                      }
                      label={`${column.type.nullable}`}
                      labelPlacement='end'
                    />
                  </FormControl>
                </Grid>
              </Grid>
            </Paper>
          </Grid>
          <Grid item xs={1} style={{
            flexBasis: '4%',
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'center'
          }}>
            <Button
              disabled={readOnly}
              style={{
                color: Colors.grey[500],
                margin: 'auto'
              }}
              onClick={() => onDeleteMapping(i)}
              size='large'
            >
              <Icon>delete</Icon>
            </Button>
          </Grid>
        </Grid>
      </div>
    );
  };

  return <Paper
    style={{
      padding: '14px',
      margin: '0'
    }}
    elevation={4}
  >
    <Grid
      container
      justify='space-between'
    >
      {
        readOnly
        && <Grid item xs={12}>
          <Paper
            style={{
              padding: '10px',
              marginBottom: '10px',
              fontWeight: 'bold',
              backgroundColor: '#ddd'
            }}
            variant='outlined'
          >
            READ-ONLY MODE
          </Paper>
        </Grid>
      }
      <Grid item xs={6}>
        <Typography variant='body1'>Name: <b>{dataSeries.name}</b></Typography>
        <Typography variant='body1'>Description: <b>{dataSeries.description}</b></Typography>
        <Typography variant='body1'>Columns count: <b>{pathColumnMappings.length}</b></Typography>
      </Grid>
      <Grid container justify='flex-end' alignItems='center' item xs={6}>
        <IconButton
          onClick={() => setIsClosed(!isClosed)}
        >
          {
            isClosed
              ? <Icon fontSize='large'>arrow_drop_down</Icon>
              : <Icon fontSize='large'>arrow_drop_up</Icon>
          }
        </IconButton>
      </Grid>
    </Grid>
    <Collapse in={!isClosed}>
      <FormControlLabel
        style={{
          marginLeft: '0'
        }}
        control={
          <Switch
            inputProps={{
              readOnly: readOnly
            }}
            color='primary'
            checked={(dataSeries.configuration as JsonIngestDataSeriesConfiguration).isCollections || false}
            onChange={
              readOnly
                ? undefined
                : (e) => onChangeDataSeries(
                  'configuration',
                  {
                    ...(dataSeries.configuration as JsonIngestDataSeriesConfiguration),
                    isCollections: e.target.checked
                  }
                )
            }
          />
        }
        label='Is collections:'
        labelPlacement='start'
      />
      {renderColumnsConfigurators()}
      <Divider
        style={{
          margin: '14px 0'
        }}
      />
      <Button
        disabled={readOnly}
        size='large'
        startIcon={<Icon>add</Icon>}
        onClick={addColumn}
      >
        Add New Column
      </Button>
      <Divider
        style={{
          margin: '14px 0'
        }}
      />
      <Grid
        container
        justify='space-between'
      >
        <Grid item xs={2}>
          <Typography
            variant='h6'
            style={{
              marginBottom: '0px'
            }}
          >
            Sample Rows
          </Typography>
        </Grid>
        <Grid item xs={2} style={{
          textAlign: 'right'
        }}>
          <Button
            variant='outlined'
            color='primary'
            onClick={onFetchSample}
          >
            Fetch sample
          </Button>
        </Grid>
      </Grid>
      {
        (sampleResponse && sampleResponse.dataSeries && sampleResponse.data.length > 0)
          ? <TableContainer
            component={({ children }) => <Paper style={{ marginTop: '14px' }} variant='outlined'>{children}</Paper>}
          >
            <Table>
              <TableHead>
                <TableRow>
                  {
                    sampleResponse.dataSeries.columns.map((column, columnIndex) => {
                      return (
                        <TableCell key={columnIndex}>{column.name}</TableCell>
                      );
                    })
                  }
                </TableRow>
              </TableHead>
              <TableBody>
                {
                  // Map sample rows to table rows
                  sampleResponse.data.map((row, rowIndex) => {
                    return (
                      <TableRow key={rowIndex}>
                        {
                          row.map((cell, cellIndex) => {
                            return (
                              <TableCell key={cellIndex}>{cell}</TableCell>
                            );
                          })
                        }
                      </TableRow>
                    );
                  })
                }
              </TableBody>
            </Table>
          </TableContainer>
          : <TableContainer
            component={({ children }) => <Paper style={{ marginTop: '14px' }} variant='outlined'>{children}</Paper>}
          >
            <Table>
              <TableBody>
                <TableRow>
                  <TableCell><i>No sample rows</i></TableCell>
                </TableRow>
              </TableBody>
            </Table>
          </TableContainer>
      }
    </Collapse>
  </Paper>;
};
