import React, { useState, useCallback } from 'react';
import { useLogin } from '../../hooks';

import {
  Container,
  Typography as T,
  createStyles,
  makeStyles,
  Button,
  Paper,
  CircularProgress,
  TextField,
  InputAdornment,
  Icon
} from '@material-ui/core';
import { Hexagon } from '../common';

const useStyles = makeStyles(theme =>
  createStyles({
    background: {
    },
    container: {
      color: theme.palette.text.primary,
      position: 'absolute',
      top: '50%',
      left: '50%',
      transform: 'translate(-50%, -50%)'
    },
    top: {
      margin: theme.spacing(1, 0),
      marginTop: theme.spacing(8),
      fontSize: theme.typography.h5.fontSize,
      [theme.breakpoints.down('xs')]: {
        fontSize: theme.typography.h5.fontSize
      }
    },
    textField: {
      margin: theme.spacing(1.5, 0)
    },
    submit: {
      marginTop: theme.spacing(1.5),
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center'
    },
    text: {
      fontSize: theme.typography.h3.fontSize,
      [theme.breakpoints.down('xs')]: {
        fontSize: theme.typography.h4.fontSize
      }
    },
    headerPanel: {
      backgroundColor: theme.palette.primary.main,
      height: '100px',
      position: 'relative',
      borderBottom: '4px solid rgb(66, 66, 66)',
      boxSizing: 'content-box'
    },
    main: {
      padding: theme.spacing(3),
      display: 'flex',
      flexDirection: 'column'
    },
    imgWrap: {
      marginBottom: theme.spacing(2),
      display: 'flex',
      justifyContent: 'center'
    },
    body: {
      top: 0,
      zIndex: -1,
      position: 'absolute',
      borderStyle: 'solid',
      borderWidth: '80px 100vw 12vh 0',
      borderColor: theme.palette.primary.main
    },
    input: {
      margin: theme.spacing(1.5, 0)
    }
  })
);

export const LoginView = () => {
  const classes = useStyles();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [status, setStatus] = useState({ loading: false, error: false });
  const { login } = useLogin();

  const handleUsername = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    setUsername(e.target.value);
  }, [setUsername]);

  const handlePassword = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    setPassword(e.target.value);
  }, [setPassword]);

  const onSubmit = useCallback(async (e?: React.FormEvent) => {
    if (username === '' || password === '') {
      return;
    }

    if (e) {
      e.preventDefault();
    }

    setStatus({ loading: true, error: false });

    try {
      await login({ username, password });
    }
    catch (e) {
      setStatus({ loading: false, error: true });
    }
  }, [login, username, password, setStatus]);

  const handleKeyDown = useCallback((e: React.KeyboardEvent<HTMLDivElement>) => {
    if (e.key === 'Enter') {
      onSubmit();
    }
  }, [onSubmit]);

  return (
    <div className={classes.background}>
      <Container className={classes.container} maxWidth='sm'>
        <Paper elevation={2}>
          <div className={classes.headerPanel}>
            <div style={{
              lineHeight: '0',
              position: 'absolute',
              left: '50%',
              transform: 'translate(-50%, 0)',
              bottom: '-75px'
            }}>
              <Hexagon
                width='150px'
                height='150px'
                strokeColor='rgb(66, 66, 66)'
                strokeWidth={12}
                fillColor='#fff'
              />
              <div style={{
                position: 'absolute',
                top: '50%',
                left: '50%',
                transform: 'translate(-50%, -50%)',
                fontSize: '32px',
                color: '#0f0f0f',
                fontFamily: 'Prompt'
              }}>Quanta</div>
            </div>
          </div>
          <main className={classes.main}>
            <form onSubmit={onSubmit}>
              <T variant='h3' align='center' className={classes.top}>Log in</T>
              <TextField
                label='Username'
                type='username'
                fullWidth
                autoComplete='username'
                error={status.error}
                className={classes.input}
                value={username}
                onChange={handleUsername}
                onKeyDown={handleKeyDown}
                variant='outlined'
                InputProps={{
                  startAdornment: (
                    <InputAdornment position='start'>
                      <Icon>{'person'}</Icon>
                    </InputAdornment>
                  )
                }}

              />
              <TextField
                label='Password'
                fullWidth
                type='password'
                error={status.error}
                className={classes.input}
                value={password}
                onChange={handlePassword}
                variant='outlined'
                onKeyDown={handleKeyDown}
                autoComplete='current-password'
                InputProps={{
                  startAdornment: (
                    <InputAdornment position='start'>
                      <Icon>lock</Icon>
                    </InputAdornment>
                  )
                }}
              />
              <div className={classes.submit}>
                <Button
                  variant='contained'
                  color='primary'
                  type='submit'
                  fullWidth
                  disabled={status.loading || username.length === 0 || password.length === 0}
                >
                  {status.loading ? (
                    <CircularProgress
                      color='inherit'
                      size={20}
                    />
                  ) : 'Log in'}
                </Button>
              </div>
            </form>
          </main>
        </Paper>
      </Container>
    </div>
  );
};
