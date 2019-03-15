import React from 'react';
import { useAuth } from '../../hooks';
import { makeStyles, createStyles, Typography as T, Paper } from '@material-ui/core';

const useStyles = makeStyles(theme =>
  createStyles({
    container: {
      position: 'absolute',
      display: 'flex',
      height: '100vh',
      width: '100vw',
      overflow: 'hidden',
      justifyContent: 'center',
      alignItems: 'center'
    },
    center: {
      display: 'flex',
      flexDirection: 'column'
    },
    paper: {
      marginBottom: theme.spacing(3),
      padding: theme.spacing(2)
    },
    top: {
      display: 'flex',
      justifyContent: 'center',
      marginBottom: theme.spacing(1)
    },
    body: {
      top: 0,
      zIndex: -1,
      position: 'absolute',
      borderStyle: 'solid',
      borderWidth: '80px 100vw 12vh 0',
      borderColor: theme.palette.primary.main
    }
  })
);

interface Props {
  children: JSX.Element;
}

export const AuthLayer = ({ children }: Props) => {
  const classes = useStyles();
  const { loading, auth, error } = useAuth();

  if (error) {
    return (
      <>
        <div className={classes.body} />
        <div className={classes.container}>
          <div className={classes.center}>
            <Paper elevation={2} className={classes.paper}>
              <div className={classes.top} />
              <T variant='h5'>Error</T>
            </Paper>
          </div>
        </div>
      </>
    );
  }

  if (loading || auth === null) {
    return (
      <>
        <div className={classes.body} />
        <div className={classes.container}>
          <div className={classes.center}>
            <Paper elevation={2} className={classes.paper}>
              <div className={classes.top} />
              <T variant='h5'>Loading</T>
            </Paper>
          </div>
        </div>
      </>
    );
  }

  return children;
};
