import React, { useContext, useEffect, useState } from 'react';

import { _DataConnectionConfiguratorContext } from '../../context';

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
  DefaultJdbcDataConnectionConfiguration,
  DataConnectionMetadata
} from '../../../types';
import {
  submitDataConnection,
  getDataConnectionMetadata
} from '../../../client';
import { useDrivers, useRouter } from '../../../hooks';
import StepperButtons from '../StepperButtons';

// Set JDBC configurations
export const JdbcDataConnectionConfigurator = () => {
  const {
    dataConnection,
    setDataConnection,

    setSuccess,
    setError
  } = useContext(_DataConnectionConfiguratorContext);
  const { history } = useRouter();

  const {
    drivers,
    driverJarOptions
  } = useDrivers();
  const [complete, setComplete] = useState<boolean>(false);
  const [isLoading, setIsLoading] = useState<boolean>(false);

  useEffect(() => {
    // Check if configurations are valid to allow next
    if (!dataConnection) {
      return;
    }
    const currentJdbcConfiguration = dataConnection.configuration as JdbcDataConnectionConfiguration;
    setComplete(
      currentJdbcConfiguration !== null
      && currentJdbcConfiguration !== undefined
      && currentJdbcConfiguration.driverJar !== ''
      && currentJdbcConfiguration.driverClass !== ''
      && currentJdbcConfiguration.connectionString !== ''
      && currentJdbcConfiguration.username !== ''
      && currentJdbcConfiguration.password !== ''
    );
  }, [dataConnection]);

  const currentJdbcConfiguration = dataConnection.configuration as JdbcDataConnectionConfiguration
    || DefaultJdbcDataConnectionConfiguration;

  const selectedDriver = drivers.find(driver => driver.jar === currentJdbcConfiguration.driverJar);

  const driverClassOptions = selectedDriver
    ? selectedDriver.classes.map((driverClass: string, idx: number) =>
      <MenuItem key={idx} value={driverClass}>{driverClass}</MenuItem>)
    : [];

  const handleNextClick = () => {
    // Submit JDBC DataConnection
    // and get SQL tables from metadata
    if (dataConnection) {
      setIsLoading(true);
      submitDataConnection(dataConnection)
        .then((resDataConnection) => {
          setDataConnection(resDataConnection);

          // Check if tables are available for next step
          getDataConnectionMetadata(resDataConnection.id)
            .then((res: DataConnectionMetadata) => {
              if (
                res.jdbcDataConnectionMetadata
                && res.jdbcDataConnectionMetadata.tables.length > 0
              ) {
                setIsLoading(false);
                setSuccess('Connection successful');
                history.replace('/data-connections');
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

    setDataConnection({
      ...dataConnection,
      configuration: newJdbcConfiguration
    });
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
