import React from 'react';
import {
  Button,
  Grid,
  Icon,
  Typography
} from '@material-ui/core';

export const FileSelect = (props: {
  file: File | null;
  accept: string;
  onChange: (file: File) => void;
  onClear: () => void;
}) => {
  const {
    file,
    accept,
    onChange,
    onClear
  } = props;

  const fileInput = React.createRef<HTMLInputElement>();

  const handleChange = (event: {target: HTMLInputElement}) => {
    if (event.target.files) {
      onChange(event.target.files[0]);
    }
  };

  const handleClear = () => {
    if (fileInput.current) {
      fileInput.current.value = '';
      onClear();
    }
  };

  return (
    <Grid container spacing={2} alignItems='center'>
      <Grid item>
        <Typography variant='button'>
          {file
            ? `Selected: ${file.name}`
            : 'No file selected'}
        </Typography>
      </Grid>
      <Grid item>
        <Button variant='outlined'>
          <input
            onChange={handleChange}
            ref={fileInput}
            type='file'
            accept={accept}
            style={{
              opacity: 0,
              position: 'absolute',
              left: 0,
              top: 0,
              width: '100%',
              height: '100%'
            }}
          />
          Select
          <Icon className='button-icon right'>note_add</Icon>
        </Button>
      </Grid>
      {file && <Grid item>
        <Button onClick={handleClear} variant='outlined'>
          Delete
          <Icon className='button-icon right'>cancel</Icon>
        </Button>
      </Grid>}
    </Grid>
  );
};
