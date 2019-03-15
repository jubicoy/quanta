import React, { useContext } from 'react';

import { Button } from '@material-ui/core';

import { _DataConnectionConfiguratorContext } from './DataImportPage';
import { dataStyles } from './DataStyles';

interface Props {
  onNextClick: () => void;
  disableNext: boolean;
}

const StepperButtons = ({ onNextClick, disableNext }: Props) => {
  const classes = dataStyles();
  const {
    handleBack
  } = useContext(_DataConnectionConfiguratorContext);

  return (
    <>
      <Button className={classes.topSpacing} onClick={() => handleBack()}>
        Back
      </Button>
      <Button
        className={classes.topSpacing}
        disabled={disableNext}
        color='primary'
        variant='contained'
        onClick={onNextClick}>
        Next
      </Button>
    </>
  );
};

export default StepperButtons;
