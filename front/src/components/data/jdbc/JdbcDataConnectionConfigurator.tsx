import React, { useContext, useEffect, useState } from 'react';

import { _DataConnectionConfiguratorContext } from '../DataImportPage';

import {
  Card,
  CardContent,
  CardHeader,
  Grid,
  InputLabel,
  MenuItem,
  Select,
  TextField
} from '@material-ui/core';
import {
  JdbcDataConnectionConfiguration,
  JdbcDriver,
  DefaultJdbcDataConnectionConfiguration,
  TypeMetadata,
  DataConnectionMetadata,
  DataSeries,
  DataConnection
} from '../../../types';
import {
  submitDataConnection,
  getTypeMetadata,
  getDataConnectionMetadata
} from '../../../client';
import StepperButtons from '../StepperButtons';

// Set JDBC configurations
export const JdbcDataConnectionConfigurator = () => {
  const {
    dataSeries,
    setDataSeries,

    setSuccess,
    setError,

    handleForward
  } = useContext(_DataConnectionConfiguratorContext);

  const [drivers, setDrivers] = useState<JdbcDriver[]>([]);
  const [complete, setComplete] = useState<boolean>(false);
  const [isLoading, setIsLoading] = useState<boolean>(false);

  // Set default config
  // Called once at start
  useEffect(() => {
    // Get drivers from server
    if (drivers.length === 0) {
      getTypeMetadata('JDBC')
        .then((result: TypeMetadata) => {
          if (result.jdbcTypeMetadata
            && result.jdbcTypeMetadata.drivers.length > 0
          ) {
            setDrivers(result.jdbcTypeMetadata.drivers);
          }
          else {
            setError('No JDBC driver found', new Error(''));
          }
        })
        .catch(e => {
          setError('Failed to get JDBC metadata', e);
        });
    }

    // Set default JDBC DataSeries if need
    if (
      dataSeries.dataConnection
      && dataSeries.dataConnection.configuration
    ) {
      // Configurations are available
    }
    else {
      const newDataConnection: DataConnection = {
        id: 0,
        name: '',
        description: '',
        series: [],
        ...dataSeries.dataConnection,
        type: 'JDBC',
        configuration: DefaultJdbcDataConnectionConfiguration
      };
      const newDataSeries: DataSeries = {
        ...dataSeries,
        configuration: {
          type: 'JDBC',
          query: ''
        },
        dataConnection: newDataConnection
      };
      setDataSeries(newDataSeries);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    // Check if configurations are valid to allow next
    const currentDataConnection = dataSeries.dataConnection;
    if (!currentDataConnection) {
      return;
    }
    const currentJdbcConfiguration = currentDataConnection.configuration as JdbcDataConnectionConfiguration;
    setComplete(
      currentJdbcConfiguration !== null
      && currentJdbcConfiguration !== undefined
      && currentJdbcConfiguration.driverJar !== ''
      && currentJdbcConfiguration.driverClass !== ''
      && currentJdbcConfiguration.connectionString !== ''
      && currentJdbcConfiguration.username !== ''
      && currentJdbcConfiguration.password !== ''
    );
  }, [dataSeries]);

  const currentDataConnection = dataSeries.dataConnection;
  if (!currentDataConnection) {
    return null;
  }

  const currentJdbcConfiguration = currentDataConnection.configuration as JdbcDataConnectionConfiguration
    || DefaultJdbcDataConnectionConfiguration;

  const selectedDriver = drivers.find(driver => driver.jar === currentJdbcConfiguration.driverJar);

  const driverJarOptions = drivers.map((driver, idx) =>
    <MenuItem key={idx} value={driver.jar}>{driver.jar}</MenuItem>
  );

  const driverClassOptions = selectedDriver
    ? selectedDriver.classes.map((driverClass: string, idx: number) =>
      <MenuItem key={idx} value={driverClass}>{driverClass}</MenuItem>)
    : [];

  const handleNextClick = () => {
    // Submit JDBC DataConnection
    // and get SQL tables from metadata
    if (dataSeries.dataConnection) {
      setIsLoading(true);
      submitDataConnection(dataSeries.dataConnection)
        .then((resDataConnection) => {
          setDataSeries({
            ...dataSeries,
            dataConnection: resDataConnection
          });

          // Check if tables are available for next step
          getDataConnectionMetadata(resDataConnection.id)
            .then((res: DataConnectionMetadata) => {
              if (
                res.jdbcDataConnectionMetadata
                && res.jdbcDataConnectionMetadata.tables.length > 0
              ) {
                setIsLoading(false);
                setSuccess('Connection successful');
                handleForward();
              }
              else {
                setIsLoading(false);
                setError('No table found.', new Error(''));
              }
            })
            .catch((e: Error) => {
              setIsLoading(false);
              setError('Failed to get metadata', e);
            });
        })
        .catch((e: Error) => {
          setTimeout(() => setIsLoading(false), 1000);
          setError('Failed to create DataConnection', e);
        });
    }
  };

  const handleJdbcConfigurationChange = (
    e: React.ChangeEvent<{
      name: string;
      value: unknown;
    }>
  ) => {
    const key = e.target.name;
    const value = e.target.value;

    let newJdbcConfiguration: JdbcDataConnectionConfiguration;

    // driverJar, driverClass, connectionString, username, password
    if (key === 'driverJar') {
      // Auto-select first (or only) driver class
      const driver = drivers.find(driver => driver.jar === value);
      newJdbcConfiguration = {
        ...currentJdbcConfiguration,
        'driverJar': value as string,
        'driverClass': (driver && driver.classes.length >= 1) ? driver.classes[0] : ''
      };
    }
    else {
      newJdbcConfiguration = {
        ...currentJdbcConfiguration,
        [key as string]: value
      };
    }

    const newDataConnection: DataConnection = {
      id: 0,
      name: '',
      description: '',
      series: [],
      type: 'JDBC',
      ...dataSeries.dataConnection,
      configuration: newJdbcConfiguration
    };
    const newDataSeries: DataSeries = {
      ...dataSeries,
      dataConnection: newDataConnection
    };

    setDataSeries(newDataSeries);
  };

  return (
    <>
      <Card style={{ marginTop: '15px' }}>
        <CardHeader title='JDBC configuration' />
        <CardContent>
          <Grid container spacing={2}>
            <Grid item xs={6}>
              <InputLabel
                htmlFor='jdbc-jar-select'
                shrink
              >
                Driver File
              </InputLabel>
              <Select
                fullWidth
                value={currentJdbcConfiguration.driverJar}
                name='driverJar'
                // What type is event from Select onChange ?
                // eslint-disable-next-line @typescript-eslint/no-explicit-any
                onChange={(e: any) => handleJdbcConfigurationChange(e)}
                inputProps={{ id: 'jdbc-jar-select' }}
              >
                {driverJarOptions}
              </Select>
            </Grid>
            <Grid item xs={6}>
              <InputLabel
                htmlFor='jdbc-class-select'
                shrink
              >
                Driver Class
              </InputLabel>
              <Select
                fullWidth
                name='driverClass'
                value={currentJdbcConfiguration.driverClass}
                disabled={driverClassOptions.length <= 1}
                // eslint-disable-next-line @typescript-eslint/no-explicit-any
                onChange={(e: any) => handleJdbcConfigurationChange(e)}
                inputProps={{ id: 'jdbc-class-select' }}
              >
                {driverClassOptions}
              </Select>
            </Grid>
            {currentJdbcConfiguration.driverJar !== ''
            && currentJdbcConfiguration.driverClass !== ''
            && (
              <>
                <Grid item xs={12}>
                  <TextField
                    label='Connection String'
                    fullWidth
                    value={currentJdbcConfiguration.connectionString}
                    name='connectionString'
                    onChange={e => handleJdbcConfigurationChange(e)}
                  />
                </Grid>
                <Grid item xs={6}>
                  <TextField
                    label='Username'
                    fullWidth
                    value={currentJdbcConfiguration.username}
                    name='username'
                    onChange={e => handleJdbcConfigurationChange(e)}
                  />
                </Grid>
                <Grid item xs={6}>
                  <TextField
                    label='Password'
                    fullWidth
                    type='password'
                    value={currentJdbcConfiguration.password}
                    name='password'
                    onChange={e => handleJdbcConfigurationChange(e)}
                  />
                </Grid>
              </>
            )}
          </Grid>
        </CardContent>
      </Card>
      <StepperButtons
        onNextClick={handleNextClick}
        disableNext={!complete || isLoading}
      />
    </>
  );
};
