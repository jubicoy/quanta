import React from 'react';
import {
  Dialog,
  CircularProgress,
  LinearProgress,
  makeStyles,
  createStyles,
  Typography as T
} from '@material-ui/core';

const useStyles = makeStyles(theme =>
  createStyles({
    progress: {
      margin: theme.spacing(1),
      color: '#fff'
    },
    paper: {
      background: 'inherit',
      boxShadow: 'inherit',
      color: '#fff'
    },
    center: {
      display: 'flex',
      flexDirection: 'column',
      overflow: 'hidden',
      alignItems: 'center'
    },
    text: {
      marginBottom: theme.spacing(1)
    },
    progressLine: {
      width: '100%'
    }
  })
);

interface Props {
  progressive?: boolean;
  progress?: number;
  message?: string;
  isLoading: boolean;
}

export const DataLoader = ({
  progressive = true,
  progress = 0,
  message = 'Loading',
  isLoading
}: Props) => {
  const classes = useStyles();
  return (
    <Dialog
      open={isLoading}
      fullWidth
      maxWidth='md'
      classes={{ paper: classes.paper }}
    >
      <div className={classes.center}>
        <CircularProgress className={classes.progress} />
        <T variant='button' className={classes.text}>{message}</T>
        {progressive && (
          <LinearProgress
            variant='determinate'
            value={progress}
            className={classes.progressLine}
          />
        )}
      </div>
    </Dialog>
  );
};
