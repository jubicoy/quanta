import React, { useState, useContext, useEffect } from 'react';
import { _DataConnectionConfiguratorContext } from '../../context';
import { SampleTable } from '..';
import StepperButtons from '../StepperButtons';
import { sample, updateDataConnection } from '../../../client';
import {
  Grid,
  Button,
  Input,
  Typography as T,
  Card,
  CardContent,
  Table,
  TableCell,
  TableRow,
  TableHead,
  TextField,
  TableBody
} from '@material-ui/core';

import { useTags } from '../../../hooks';
import { ImportWorkerDataSeriesConfiguration } from '../../../types';
import { SampleResponse } from '../../../types/Api';
import Autocomplete from '@material-ui/lab/Autocomplete';
import Chip from '@material-ui/core/Chip';

export const ImportWorkerDataPreprocessingConfigurator = () => {
  const {
    dataSeries,
    setDataSeries,

    setSampleResponse,

    setSampleData,

    setSuccess,
    setError,

    selectedWorkerDef,
    handleForward
  } = useContext(_DataConnectionConfiguratorContext);

  const { tags } = useTags();
  const selectedColumns = [...dataSeries.columns];
  const [complete, setComplete] = useState<boolean>(false);
  const [isEdit, setEdit] = useState<boolean>(false);

  const nextStep = () => {
    if (dataSeries.dataConnection && dataSeries.dataConnection.tags.length > 0) {
      updateDataConnection(dataSeries.dataConnection)
        .catch((e: Error) => {
          setError('Fail to add tags', e);
        });
    }

    handleForward();
  };

  const addTags = (value: string[]) => {
    if (dataSeries && dataSeries.dataConnection) {
      dataSeries.dataConnection.tags = value;
      setDataSeries(dataSeries);
    }
  };

  useEffect(() => {
    if (selectedColumns.length > 0) {
      setComplete(true);
    }
    else {
      setComplete(false);
    }
  }, [selectedColumns]);

  const handleEdit = () => {
    setEdit(!isEdit);
  };

  const handleSave = () => {
    setEdit(!isEdit);
    setSampleResponse(null);
    setSampleData([]);
    if (dataSeries.dataConnection) {
      sample(dataSeries.dataConnection.id, dataSeries)
        .then((res: SampleResponse) => {
          setDataSeries({
            ...dataSeries,
            columns: res.dataSeries.columns });
          setSampleResponse(res);
          setSampleData(res.data);
          setSuccess('Fetch sample successfully');
        })
        .catch((e: Error) => {
          setError('Sample failed to execute', e);
        });
    }
  };

  const handleInputChange = (e: any, index: number) => {
    const { name, value } = e.target;
    const list: any = [...(dataSeries.configuration as ImportWorkerDataSeriesConfiguration).parameters];
    list[index][name] = value;
    setDataSeries({
      ...dataSeries,
      columns: [],
      configuration: {
        ...dataSeries.configuration as ImportWorkerDataSeriesConfiguration,
        parameters: list
      }
    });
  };

  return (selectedWorkerDef !== null) ? (
    <>
      <Card>
        <CardContent>
          <Grid item xs={12}>
            <T variant='button'>Worker Name: <b>{selectedWorkerDef.name}</b></T>
          </Grid>
          <Grid item xs={12}>
            <T variant='button'>Description: <b>{selectedWorkerDef.description}</b></T>
          </Grid>
          {selectedWorkerDef.parameters && (
            <>
              <T variant='button'>Parameters:</T>
              <Table style={{ marginBottom: '30px' }}>
                <TableHead>
                  <TableRow>
                    <TableCell style={{ width: '50%' }}>Name</TableCell>
                    <TableCell>Value</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {(dataSeries.configuration as ImportWorkerDataSeriesConfiguration).parameters.map((row: any, i: number) => (
                    <TableRow key={i}>
                      {isEdit ? (
                        <>
                          <TableCell>{row.name}</TableCell>
                          <TableCell padding='none'>
                            <Input
                              fullWidth
                              value={row.value}
                              name='value'
                              onChange={(e) => handleInputChange(e, i)}
                            />
                          </TableCell>

                        </>
                      ) : (
                        <>
                          <TableCell>{row.name}</TableCell>
                          <TableCell>{row.value}</TableCell>

                        </>
                      )}
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
              <Button
                style={{ marginLeft: '10px' }}
                color='primary'
                variant='contained'
                onClick={handleSave}
              >
                Save
              </Button>
              <Button
                style={{ marginLeft: '10px' }}
                color='primary'
                variant='contained'
                onClick={handleEdit}
              >
                Edit
              </Button>

            </>
          )}
          <SampleTable
            editableType={false}
          />
        </CardContent>
      </Card>
      <Autocomplete
        multiple
        size='small'
        style={{ marginTop: '15px' }}
        options={tags || []}
        freeSolo
        onChange={(event, value) => addTags(value)}
        renderTags={(value, getTagProps) =>
          value.map((option, index) =>
            <Chip key={index} variant='outlined' label={option} {...getTagProps({ index })} />
          )
        }
        renderInput={(params) => (
          <TextField
            {...params}
            variant='outlined'
            label='Add Tag'
            placeholder='Tag'
          />
        )}
      />
      <StepperButtons
        onNextClick={nextStep}
        disableNext={!complete}
      />
    </>
  ) : <div>Loading</div>;
};
