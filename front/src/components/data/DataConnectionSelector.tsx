import React, { useContext, useMemo } from 'react';

import { Typography,
  Grid,
  Card,
  CardContent,
  TextField,
  CardHeader,
  Icon,
  Button
} from '@material-ui/core';

import { dataStyles } from './DataStyles';
import { _DataConnectionConfiguratorContext } from '../context';
import { DataConnectionType } from '../../types';
import { useDataConnections, useNameCheck } from '../../hooks';

interface DataConnectionSelectorProps {
  onTypeSelect: (selection: DataConnectionType) => void;
}

export const DataConnectionSelector = ({
  onTypeSelect
}: DataConnectionSelectorProps) => {
  const classes = dataStyles();
  const {
    dataConnection,
    setDataConnection,

    dataSeries,
    setDataSeries
  } = useContext(_DataConnectionConfiguratorContext);

  const { dataConnections } = useDataConnections();
  const nameArray = useMemo(
    () => {
      return dataConnections?.map(dc => dc.name) || [];
    },
    [dataConnections]
  );
  const { nameIsValid, helperText } = useNameCheck(
    dataConnection.name,
    nameArray
  );

  const handleDataConnectionChange = (
    e: React.ChangeEvent<{
      name: string;
      value: string;
    }>
  ) => {
    const key = e.target.name;
    const value = e.target.value;

    const newDataConnection = {
      ...dataConnection,
      [key]: value
    };

    setDataConnection(newDataConnection);

    setDataSeries({
      ...dataSeries,
      dataConnection: newDataConnection
    });
  };

  const renderTypeSelections = () => {
    const types = {
      'CSV': 'description',
      'JDBC': 'dns',
      'JSON_INGEST': 'code',
      'IMPORT_WORKER': 'storage'
    };
    return Object.entries(types).map(entry => {
      const type = entry[0] as DataConnectionType;
      const icon = entry[1];
      return <Grid item xs={3} key={type.toString()}>
        <Button
          className={classes.dataConnectionButton}
          variant='outlined'
          disabled={
            dataConnection.name.length === 0
            || dataConnection.description.length === 0
            || !nameIsValid
          }
          onClick={() => onTypeSelect(type)}
          name={type}
        >
          <Icon className={classes.dataConnectionButtonIcon}>
            {icon}
          </Icon>
          <span>{type}</span>
        </Button>
      </Grid>;
    });
  };

  return (
    <React.Fragment>
      <Card>
        <CardHeader title='Data connection configuration' />
        <CardContent>
          <form>
            <Grid container spacing={3}>
              <Grid item xs={12}>
                <TextField
                  error={!nameIsValid}
                  helperText={helperText}
                  id='data-connection-configurator-name-field'
                  label='Data connection name'
                  name='name'
                  value={dataConnection.name}
                  fullWidth
                  onChange={handleDataConnectionChange} />
              </Grid>
              <Grid item xs={12}>
                <TextField
                  id='data-connection-configurator-description-field'
                  label='Data connection description'
                  name='description'
                  value={dataConnection.description}
                  fullWidth
                  onChange={handleDataConnectionChange} />
              </Grid>
            </Grid>
          </form>
          <Typography className={classes.topSpacing} variant='h5'>
            Select type
          </Typography>

          <Grid
            className={classes.topSpacing}
            container
            spacing={2}
          >
            {renderTypeSelections()}
          </Grid>
        </CardContent>
      </Card>
    </React.Fragment>
  );
};
