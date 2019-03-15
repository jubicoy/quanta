import React, { useMemo, useState, useCallback } from 'react';
import {
  LinearProgress,
  Typography,
  Paper,
  TextField,
  Grid,
  Button,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  Icon,
  Fab
} from '@material-ui/core';

import { commonStyles } from '../common';
import { useExternalClientsOfTask, useNameCheck, useRouter } from '../../hooks';
import { ExternalClient } from '../../types';
import { useAlerts } from '../../alert';

interface Props {
  match: {
    params: {
      id: number;
    };
  };
};

export default ({
  match: {
    params
  }
}: Props) => {
  const common = commonStyles();
  const { history } = useRouter();

  // Alert handlers
  const alertContext = useAlerts('TASK-EXTERNAL-CLIENT');
  const setSuccess = useCallback(
    (heading: string, message: string) => {
      alertContext.alertSuccess(heading, message);
    },
    [alertContext]
  );
  const setError = useCallback(
    (heading: string, message: string) => {
      alertContext.alertError(heading, message);
    },
    [alertContext]
  );

  const { externalClients, create, fetch, deleteClient } = useExternalClientsOfTask(params.id);

  const DEFAULT_EXTERNAL_CLIENT: ExternalClient = {
    id: -1,
    name: '',
    token: '',
    task: null
  };
  const [newClient, setNewClient] = useState<ExternalClient>(DEFAULT_EXTERNAL_CLIENT);

  const { nameIsValid, helperText } = useNameCheck(
    newClient.name,
    []
  );

  const handleClientChange = (
    key: keyof ExternalClient,
    value: ExternalClient[keyof ExternalClient]
  ) => {
    setNewClient({
      ...newClient,
      [key]: value
    });
  };

  const handleClientAdd = () => {
    create(newClient)
      .then(client => {
        if (client.id > 0) {
          setSuccess('Success', 'Client is created');
          fetch();
        }
        else {
          setError('Server Error', 'Failed to create client');
        }
      })
      .catch(error =>
        setError('Server Error', error.message)
      );
  };

  const handleClientDelete = useCallback(
    (externalClientId: number) => {
      deleteClient(externalClientId)
        .then(response => {
          if (response.ok) {
            setSuccess('Success', `Client <ID ${externalClientId}> is deleted`);
            fetch();
          }
          else {
            setError('Server Error', 'Failed to delete client');
          }
        })
        .catch(error =>
          setError('Server Error', error.message)
        );
    },
    [deleteClient, setError, setSuccess, fetch]
  );

  const clientsListRender = useMemo(
    () => {
      if (externalClients === null) {
        return (
          <>
            <Typography>Loading external clients...</Typography>
            <LinearProgress variant='query' />
          </>
        );
      }
      if (externalClients.length === 0) {
        return <Typography variant='subtitle1'>Found no active external client</Typography>;
      }
      return <Paper
        style={{
          padding: '16px',
          overflowX: 'auto'
        }}
      >
        <Typography variant='h6'>
          {
            externalClients.length <= 1
              ? 'Active external client'
              : 'Active external clients'
          }
        </Typography>
        <Table style={{
          maxWidth: '100%',
          overflowX: 'auto'
        }}>
          <TableHead>
            <TableRow>
              <TableCell component='th'>Name</TableCell>
              <TableCell component='th'>Token</TableCell>
              <TableCell component='th'>Linked Task</TableCell>
              <TableCell component='th' />
            </TableRow>
          </TableHead>
          <TableBody>
            {externalClients.map((item, i) => (
              <TableRow
                key={i}
              >
                <TableCell
                  style={{
                    overflow: 'hidden',
                    textOverflow: 'ellipsis',
                    maxWidth: '40ch'
                  }}
                  title={item.name}
                >{item.name}</TableCell>
                <TableCell>{item.token}</TableCell>
                <TableCell>{item.task?.name || ''}</TableCell>
                <TableCell>
                  <Button
                    variant='text'
                    onClick={() => handleClientDelete(item.id)}
                  >
                    <Icon>delete</Icon>
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Paper>;
    },
    [externalClients, handleClientDelete]
  );

  return <>
    <div className={common.header}>
      <Typography variant='h4'>External Clients</Typography>
      <Fab variant='extended' color='primary'
        onClick={() => {
          history.push(`/task/${params.id}`);
        }}
      >
        <Icon className={common.icon}>
          arrow_back
        </Icon>
        Back to Task
      </Fab>
    </div>
    <Grid
      container
      style={{
        marginTop: '12px'
      }}
      spacing={4}
    >
      <Grid item xs={5}>
        <Paper
          style={{
            padding: '16px'
          }}
        >
          <Typography variant='h6'>Add an External Client</Typography>
          <TextField
            error={!nameIsValid}
            helperText={helperText}
            id='client-name-input'
            label='Name'
            name='client-name'
            value={newClient.name}
            fullWidth
            onChange={
              (e: React.ChangeEvent<HTMLInputElement>) =>
                handleClientChange('name', e.currentTarget.value)
            }
            style={{
              margin: '14px 0 18px'
            }}
          />
          <div style={{
            width: '100%',
            textAlign: 'right'
          }}>
            <Button
              disabled={!nameIsValid}
              color='primary'
              variant='contained'
              onClick={handleClientAdd}
            >
              Add
            </Button>
          </div>
        </Paper>
      </Grid>
      <Grid item xs={7}>
        <Typography variant='body1'>
          External clients allow the access to the external query API.<br />
          Each client can only query data under its linked Task.
        </Typography>
        <Typography variant='subtitle2'>
          To use, include the token as a header param "Client-Token"
          when making request to the external query API.
        </Typography>
      </Grid>
    </Grid>
    <Grid container style={{
      marginTop: '16px'
    }}>
      <Grid item xs={12}>
        {clientsListRender}
      </Grid>
    </Grid>
  </>;
};
