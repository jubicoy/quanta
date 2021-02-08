import React, {
  useMemo,
  useState,
  useCallback,
  useContext,
  useEffect
} from 'react';
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
  MenuItem,
  InputLabel,
  Select
} from '@material-ui/core';

import { commonStyles } from '../common';
import {
  useExternalClients,
  useNameCheck,
  useTasks,
  useRouter
} from '../../hooks';
import { ExternalClient, Task, User } from '../../types';
import { useAlerts } from '../../alert';
import { __AuthContext } from '../../hooks/useAuth';

export default () => {
  const common = commonStyles();
  const { auth } = useContext(__AuthContext);
  const { history } = useRouter();
  const DEFAULT_USER: User = auth?.user || {
    id: '-1',
    name: '',
    role: ''
  };
  const DEFAULT_EXTERNAL_CLIENT: ExternalClient = {
    id: -1,
    name: '',
    token: '',
    description: '',
    createdBy: DEFAULT_USER,
    task: null
  };

  // Alert handlers
  const alertContext = useAlerts('EXTERNAL-CLIENT');
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

  // States
  const [showToken, setShowToken] = useState<boolean[]>([]);
  const [newClient, setNewClient] = useState<ExternalClient>(DEFAULT_EXTERNAL_CLIENT);
  const [taskId, setTaskId] = useState<number>(-1);
  const { nameIsValid, helperText } = useNameCheck(
    newClient.name,
    []
  );

  const taskQuery = useMemo(
    () => ({
      notDeleted: true
    }),
    []
  );

  const { tasks } = useTasks(taskQuery);

  const {
    externalClients,
    create,
    fetch,
    deleteClient
  } = useExternalClients();

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
    setNewClient(DEFAULT_EXTERNAL_CLIENT);
    setTaskId(-1);
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

  const handleShowToken = useCallback(
    (indexToken: number) => {
      setShowToken(
        showToken.map(
          (item, index) => {
            if (index === indexToken) {
              return !item;
            }
            return item;
          }
        ));
    },
    [showToken]
  );

  const taskOptions = [
    <MenuItem key={-1} value={-1}>Unset</MenuItem>,
    ...(tasks || []).map((task) => (
      <MenuItem key={task.id} value={task.id}>{task.name}</MenuItem>
    ))
  ];

  const goToTask = useCallback(
    (item: Task | null) => {
      if (item) {
        history.push(`/task/${item.id}`);
      }
    },
    [history]
  );

  useEffect(
    () => {
      if (externalClients) {
        setShowToken(Array(externalClients.length).fill(false));
      }
    }, [externalClients, setShowToken]
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
        <Table style={{
          maxWidth: '100%',
          overflowX: 'auto'
        }}>
          <TableHead>
            <TableRow>
              <TableCell component='th'>Name</TableCell>
              <TableCell component='th'>Token</TableCell>
              <TableCell component='th'>Description</TableCell>
              <TableCell component='th'>Created By</TableCell>
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
                <TableCell>
                  {showToken[i] ? item.token : null}
                  {showToken[i]
                    ? <Icon
                      style={{ margin: '0 0 0 16px' }}
                      onClick={() => handleShowToken(i)}>
                      visibility_off
                    </Icon>
                    : <Icon
                      style={{ margin: '0 0 0 16px' }}
                      onClick={() => handleShowToken(i)}>
                      visibility
                    </Icon>}
                </TableCell>
                <TableCell>{item.description}</TableCell>
                <TableCell>{item.createdBy.name}</TableCell>
                <TableCell
                  onClick={() => goToTask(item.task)}>
                  {item.task?.name || ''}
                </TableCell>
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
    [externalClients, handleClientDelete, goToTask, showToken, handleShowToken]
  );

  return (
    <>
      <div className={common.header}>
        <Typography variant='h4'>External Clients</Typography>
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
            <Typography variant='h6'>Add an External Client Token</Typography>
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
            <TextField
              label='Description'
              name='description'
              fullWidth
              style={{
                margin: '0 0 18px'
              }}
              value={newClient.description}
              onChange={
                (e: React.ChangeEvent<HTMLInputElement>) =>
                  handleClientChange('description', e.currentTarget.value)
              }
            />
            <InputLabel
              htmlFor='task-connection-select'
              shrink
            >
              Tasks
            </InputLabel>
            <Select
              fullWidth
              style={{
                margin: '0 0 18px'
              }}
              value={taskId || -1}
              onChange={e => {
                const value = e.target.value as number;
                handleClientChange('task', tasks?.find(task => task.id === value));
                setTaskId(value !== -1 ? value : -1);
              }}
              inputProps={{ id: 'task-connection-select' }}
            >
              {taskOptions}
            </Select>
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
            External clients allow the access to the query API.<br />
            Each client can only query data by using the API Token.<br />
            If token does not linked to any task, it would have the
            global scope.
          </Typography>
          <Typography variant='subtitle2'>
            To use, include the token as a header param "Authorization"
            when making request to the query API.
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
    </>
  );
};
