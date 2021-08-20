import React, { useState, useEffect } from 'react';
import clsx from 'clsx';
import hljs from 'highlight.js';
import { useDataConnection,
  useRouter,
  useDataConnectionsTags,
  useDataConnectionTags,
  useDrivers } from '../../hooks';

import { updateDataConnectionTags } from '../../client';

import Autocomplete from '@material-ui/lab/Autocomplete';
import Chip from '@material-ui/core/Chip';

import {
  Table,
  TableCell,
  TableBody,
  TableHead,
  TableRow,
  Paper,
  makeStyles,
  createStyles,
  LinearProgress,
  Typography as T,
  Button,
  Link,
  Fab,
  Icon,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  TextField,
  InputAdornment,
  IconButton,
  Collapse,
  Box,
  Input,
  Select,
  MenuItem
} from '@material-ui/core';
import Visibility from '@material-ui/icons/Visibility';
import VisibilityOff from '@material-ui/icons/VisibilityOff';
import KeyboardArrowDownIcon from '@material-ui/icons/KeyboardArrowDown';
import KeyboardArrowUpIcon from '@material-ui/icons/KeyboardArrowUp';
import EditIcon from '@material-ui/icons/EditOutlined';
import DoneIcon from '@material-ui/icons/DoneAllTwoTone';
import RevertIcon from '@material-ui/icons/NotInterestedOutlined';
import FindReplaceIcon from '@material-ui/icons/FindReplace';
import {
  JsonIngestDataConnectionConfiguration,
  JdbcDataConnectionConfiguration,
  JdbcDataSeriesConfiguration,
  DataConnection,
  DataSeries
} from '../../types';
import { useAlerts } from '../../alert';
import { commonStyles } from '../common';

const useStyles = makeStyles((theme) =>
  createStyles({
    paper: {
      margin: theme.spacing(3, 0)
    },
    wrapper: {
      padding: '10px'
    },
    tableTitle: {
      padding: '16px'
    },
    chartContainer: {},
    chartToolbar: {
      marginLeft: '24px'
    },
    tableKey: {
      textTransform: 'capitalize'
    },
    tableCell: {
      paddingBottom: '0',
      paddingTop: '0'
    }
  })
);

const CustomTableCell = ({
  editable,
  name,
  value,
  onChange,
  driverClassOptions,
  driverJarOptions
}: TableCellProps) => {
  if (name === 'driverJar') {
    return (
      <TableCell align='left'>
        {editable ? (
          <Select
            fullWidth
            value={value}
            name={name}
            onChange={(event: any) => onChange(event)}
          >
            {driverJarOptions}
          </Select>
        ) : (
          value
        )}
      </TableCell>
    );
  }
  else if (name === 'driverClass') {
    return (
      <TableCell align='left'>
        {editable ? (
          <Select
            fullWidth
            value={value}
            name={name}
            onChange={(event: any) => onChange(event)}
          >{driverClassOptions}
          </Select>
        ) : (
          value
        )}
      </TableCell>
    );
  }

  return (
    <TableCell align='left'>
      {editable ? (
        <Input
          fullWidth
          value={value}
          name={name}
          onChange={(event: any) => onChange(event)}
        />
      ) : (
        value
      )}
    </TableCell>
  );
};

interface Props {
  match: {
    params: {
      id: number;
      name: string;
    };
  };
}

interface TableCellProps {
  editable: boolean;
  name: string;
  value: string;
  onChange: (e: React.ChangeEvent<{ name: string | undefined; value: unknown }>) => void;
  driverClassOptions?: JSX.Element[] | null;
  driverJarOptions?: JSX.Element[] | null;
}
export default ({ match: { params } }: Props) => {
  const common = commonStyles();
  const {
    dataConnection,
    deleteDataConnection,
    setDataConnection,
    updateDataConnection
  } = useDataConnection(params.id);
  const {
    drivers,
    driverJarOptions
  } = useDrivers();
  const classes = useStyles();
  const { history } = useRouter();
  const [openDeleteDialog, setOpenDeleteDialog] = useState<boolean>(false);
  const [showPassword, setShowPassword] = useState<boolean>(false);
  const [showColumns, setShowColumns] = useState<number>(-1);

  const [isEditMode, setEditMode] = useState<boolean>(false);

  const { tags } = useDataConnectionsTags();
  const { dataConnectionTags,
    setDataConnectionTags } = useDataConnectionTags(params.id);

  const [previousConnection, setPreviousConnection] = useState<DataConnection | null>(dataConnection);

  const names = tags && tags.map(({ name }) => name);

  useEffect(() => {
    if (dataConnection && previousConnection === null) {
      setPreviousConnection(dataConnection);
    }
  }, [dataConnection, previousConnection]);

  useEffect(() => {
    if (!params.name && dataConnection) {
      history.replace(`${params.id}/${encodeURI(dataConnection.name)}`);
    }
  }, [dataConnection, params.id, params.name, history]);

  if (!dataConnection) {
    return <LinearProgress variant='query' />;
  }
  // Alert handlers
  const alertContext = useAlerts('DATA-IMPORT');
  const setSuccess = (heading: string) => {
    alertContext.alertSuccess(heading, '');
  };
  const setError = (heading: string, error: Error) => {
    alertContext.alertError(heading, error.message);
  };

  const handleClickOpenDeleteDialog = () => {
    setOpenDeleteDialog(true);
  };

  const handleCloseDeleteDialog = () => {
    setOpenDeleteDialog(false);
  };

  const updateTags = () => {
    if (dataConnectionTags) {
      updateDataConnectionTags(params.id, dataConnectionTags).catch((e: Error) => {
        console.log(e);
      });
    }
    setEditMode(false);
  };

  const onChange = (e: React.ChangeEvent<{ name: string | undefined; value: unknown }>) => {
    if (!e) {
      return undefined;
    }
    const { value, name } = e.target;
    const driver = drivers.find((driver) => driver.jar === value);
    if (name === 'name' || name === 'description') {
      setDataConnection({
        ...dataConnection,
        [name as string]: value
      });
    }
    else if (name === 'driverJar' && dataConnection.type === 'JDBC') {
      setDataConnection({
        ...dataConnection,
        configuration: {
          ...dataConnection.configuration as JdbcDataConnectionConfiguration,
          'driverJar': value as string,
          'driverClass': (driver && driver.classes.length >= 1) ? driver.classes[0] : ''
        }
      });
    }
    else {
      setDataConnection({
        ...dataConnection,
        configuration: {
          ...dataConnection.configuration,
          [name as string]: value
        }
      });
    }
  };
  const saveEdit = () => {
    setEditMode(false);
    updateDataConnection(dataConnection).then((result) => {
      setPreviousConnection(result);
      setSuccess('Update data connection successfully!');
    }).catch((e: Error) => {
      setError('Invalid information', e);
      setDataConnection(previousConnection);
    });
  };

  const cancelEdit = () => {
    setEditMode(false);
    setDataConnection(previousConnection);
  };

  const dataSeries = dataConnection.series;

  const dataConfiguration = dataConnection.configuration;

  if (dataConnection.deletedAt) {
    return (
      <>
        <div className={common.header}>
          <T variant='h4'>
            Data connection {dataConnection.name} has been deleted
          </T>
        </div>
      </>
    );
  }

  const selectedDriver = drivers.find(
    (driver) => driver.jar === (dataConfiguration as JdbcDataConnectionConfiguration).driverJar
  );
  const driverClassOptions = selectedDriver
    ? selectedDriver.classes.map((driverClass: string, idx: number) => (
      <MenuItem key={idx} value={driverClass}>
        {driverClass}
      </MenuItem>
    ))
    : [];

  const renderDataConnectionDetails = () => {
    switch (dataConnection.type) {
      case 'CSV':
        return (
          <Table>
            <TableHead>
              <TableRow>
                <TableCell component='th'>Name</TableCell>
                <TableCell component='th'>Description</TableCell>
                <TableCell component='th'>Type</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              <TableRow>
                <TableCell>{dataConnection.name}</TableCell>
                <TableCell>{dataConnection.description}</TableCell>
                <TableCell>{dataConnection.type}</TableCell>
              </TableRow>
            </TableBody>
          </Table>
        );

      case 'JDBC':
        return (
          <Table>
            <TableBody>
              <TableRow>
                <TableCell component='th' scope='row' variant='head'>
                  Name
                </TableCell>
                <CustomTableCell
                  editable={isEditMode}
                  name='name'
                  value={dataConnection.name}
                  onChange={onChange}
                />
              </TableRow>
              <TableRow>
                <TableCell component='th' scope='row' variant='head'>
                  Description
                </TableCell>
                <CustomTableCell
                  editable={isEditMode}
                  name='description'
                  value={dataConnection.description}
                  onChange={onChange}
                />
              </TableRow>
              {Object.entries(
                dataConfiguration as JdbcDataConnectionConfiguration
              )
                .filter(([key]) => key !== 'password')
                .map(([key, value]) => (
                  <TableRow key={key}>
                    <TableCell
                      component='th'
                      scope='row'
                      variant='head'
                      className={classes.tableKey}
                    >
                      {key}
                    </TableCell>
                    <CustomTableCell
                      editable={isEditMode}
                      name={key}
                      value={value}
                      onChange={onChange}
                      driverJarOptions={driverJarOptions}
                      driverClassOptions={driverClassOptions}
                    />
                  </TableRow>
                ))}
              <TableRow>
                <TableCell component='th' scope='row' variant='head'>
                  Password
                </TableCell>
                <TableCell>
                  <TextField
                    disabled={!isEditMode}
                    name='password'
                    onChange={onChange}
                    type={showPassword ? 'text' : 'password'}
                    value={(dataConfiguration as JdbcDataConnectionConfiguration).password}
                    InputProps={{
                      endAdornment: (
                        <InputAdornment position='end'>
                          <IconButton
                            onClick={() => setShowPassword(!showPassword)}
                          >
                            {showPassword ? <Visibility /> : <VisibilityOff />}
                          </IconButton>
                        </InputAdornment>
                      )
                    }}
                  />
                </TableCell>
              </TableRow>
            </TableBody>
          </Table>
        );

      case 'JSON_INGEST':
        return (
          <Table>
            <TableHead>
              <TableRow>
                <TableCell component='th'>Name</TableCell>
                <TableCell component='th'>Description</TableCell>
                <TableCell component='th'>Type</TableCell>
                <TableCell component='th' colSpan={2}>
                  Token
                </TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              <TableRow>
                <TableCell>{dataConnection.name}</TableCell>
                <TableCell>{dataConnection.description}</TableCell>
                <TableCell>{dataConnection.type}</TableCell>
                <TableCell>
                  {
                    (
                      dataConnection.configuration as JsonIngestDataConnectionConfiguration
                    ).token
                  }
                </TableCell>
                <TableCell>
                  <Link href={`/json-ingest/${dataConnection.id}`}>
                    Submit a JSON to this DataConnection
                  </Link>
                </TableCell>
              </TableRow>
            </TableBody>
          </Table>
        );

      case 'IMPORT_WORKER':
        return (
          <Table>
            <TableHead>
              <TableRow>
                <TableCell component='th'>Name</TableCell>
                <TableCell component='th'>Description</TableCell>
                <TableCell component='th'>Type</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              <TableRow>
                <TableCell>{dataConnection.name}</TableCell>
                <TableCell>{dataConnection.description}</TableCell>
                <TableCell>{dataConnection.type}</TableCell>
              </TableRow>
            </TableBody>
          </Table>
        );

      default:
        break;
    }
  };

  return (
    <>
      <div className={common.header}>
        <T variant='h4'>Data connection details</T>
        <div className={common.toggle}>
          {isEditMode ? (
            <>
              <Fab
                className={common.leftMargin}
                variant='extended'
                color='primary'
                onClick={saveEdit}
              >
                <DoneIcon />
                Save
              </Fab>
              <Fab
                className={common.leftMargin}
                variant='extended'
                color='primary'
                onClick={cancelEdit}
              >
                <RevertIcon />
                Cancel
              </Fab>
            </>
          ) : (
            <Fab
              className={common.leftMargin}
              variant='extended'
              color='primary'
              onClick={() => setEditMode((prev) => !prev)}
            >
              <EditIcon />
            </Fab>
          )}

          <Fab
            className={common.leftMargin}
            variant='extended'
            color='primary'
            onClick={handleClickOpenDeleteDialog}
          >
            <Icon className={clsx(common.icon)}>{'delete'}</Icon>
            Delete
          </Fab>
          {isEditMode ? (
            <>
              <Fab
                className={common.leftMargin}
                variant='extended'
                color='primary'
                onClick={updateTags}
              >
                <Icon className={clsx(common.icon)}>
                  {'save'}
                </Icon>
                Save
              </Fab>
              <Fab
                className={common.leftMargin}
                variant='extended'
                color='primary'
                onClick={() => setEditMode((prev) => !prev)}
              >
                <Icon className={clsx(common.icon)}>
                  {'cancel'}
                </Icon>
                Cancel
              </Fab>
            </>
          ) : (
            <Fab
              className={common.leftMargin}
              variant='extended'
              color='primary'
              onClick={() => setEditMode((prev) => !prev)}
            >
              <Icon className={clsx(common.icon)}>
                {'edit'}
              </Icon>
              Edit
            </Fab>
          )}
        </div>
      </div>
      { !isEditMode ? (dataConnectionTags && dataConnectionTags.map((option, index) =>
        <Chip style={{ margin: '5px' }} key={index} variant='outlined' label={option} />
      )) : <Autocomplete
        multiple
        size='small'
        style={{ marginTop: '15px' }}
        options={names || []}
        value={dataConnectionTags || []}
        freeSolo
        onChange={(event, value) => setDataConnectionTags(value)}
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
          />
        )}
      />}
      <div className={classes.wrapper}>
        <Paper className={classes.paper}>{renderDataConnectionDetails()}</Paper>
        <Link href={`/data-connections/${dataConnection.id}/${encodeURI(dataConnection.name)}/series/new`}>
          {dataConnection.type === 'JDBC' && <Fab
            className={clsx(common.floatRight)}
            variant='extended'
            color='primary'
          >
            <Icon className={common.icon}>
              add
            </Icon>
            Create New

          </Fab>}
        </Link>
        <T variant='h4'>Data series details</T>

        {dataSeries.length <= 0 ? (
          <i>No Data Series available</i>
        ) : (
          dataSeries.filter(series => series.deletedAt === null).map((series: DataSeries, i) => (
            <div key={i}>
              <Paper className={classes.paper}>
                <div className={classes.tableTitle}>

                  <T variant='body1'>
                    <b>Name: </b>
                    {series.name}
                  </T>
                  <T variant='body1'>
                    <b>Description: </b>
                    {series.description}
                  </T>
                  {dataConnection.type === 'JDBC'
                  && <>
                    <T variant='body1'>
                      <b>Table Name: </b>
                      {series.tableName}
                    </T>
                    <T variant='body1'>
                      <b>SQL Query: </b>
                      <div
                        dangerouslySetInnerHTML={{
                          __html: hljs.highlight((series.configuration as JdbcDataSeriesConfiguration).query, {
                            language: 'sql'
                          }).value
                        }}
                      />
                    </T>
                  </>}
                  <Link href={`/data-series/${series.id}/replace`}>
                    {dataConnection.type === 'JDBC' && <Fab
                      className={clsx(common.floatRight)}
                      variant='extended'
                      color='primary'
                    >
                      <FindReplaceIcon style={{ marginRight: '5px' }} /> Replace
                    </Fab>}
                  </Link>
                </div>
                <>
                  <TableRow>
                    <TableCell
                      component='th'
                      scope='row'
                      variant='head'
                      colSpan={4}
                    >
                      Columns
                      <IconButton
                        edge='start'
                        size='small'
                        onClick={() => showColumns !== i ? setShowColumns(i) : setShowColumns(-1)}
                      >
                        {showColumns === i ? (
                          <KeyboardArrowUpIcon />
                        ) : (
                          <KeyboardArrowDownIcon />
                        )}
                      </IconButton>
                    </TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell className={classes.tableCell} colSpan={8}>
                      <Collapse in={showColumns === i}>
                        <Box margin={3}>
                          <Table size='small' aria-label='purchases'>
                            <TableHead>
                              <TableRow>
                                <TableCell
                                  component='th'
                                  scope='row'
                                  variant='head'
                                >
                                  Name
                                </TableCell>
                                <TableCell
                                  component='th'
                                  scope='row'
                                  variant='head'
                                >
                                  Format
                                </TableCell>
                                <TableCell
                                  component='th'
                                  scope='row'
                                  variant='head'
                                >
                                  Nullable
                                </TableCell>
                                <TableCell
                                  component='th'
                                  scope='row'
                                  variant='head'
                                >
                                  Class
                                </TableCell>
                              </TableRow>
                            </TableHead>

                            <TableBody>
                              {series.columns.map((column, i) => (
                                <TableRow key={i}>
                                  <TableCell>{column.name}</TableCell>
                                  <TableCell>{column.type.format}</TableCell>
                                  <TableCell>
                                    {column.type.nullable
                                      ? column.type.nullable.toString()
                                      : 'false'}
                                  </TableCell>
                                  <TableCell>{column.type.className}</TableCell>
                                </TableRow>
                              ))}
                            </TableBody>
                          </Table>
                        </Box>
                      </Collapse>
                    </TableCell>
                  </TableRow>
                </>
              </Paper>
            </div>
          ))
        )}
      </div>
      <Dialog
        open={openDeleteDialog}
        onClose={handleCloseDeleteDialog}
        aria-labelledby='alert-dialog-title'
        aria-describedby='alert-dialog-description'
      >
        <DialogTitle id='alert-dialog-title'>{'Delete'}</DialogTitle>
        <DialogContent>
          <DialogContentText id='alert-dialog-description'>
            Are you sure want to delete current data connection? This operation
            will also delete all of the tasks created with this data connection.
            Deleted task's invocations and results won't deleted.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDeleteDialog} color='primary'>
            Cancel
          </Button>
          <Button
            onClick={() =>
              deleteDataConnection().then(() =>
                history.push('/data-connections')
              )
            }
            color='primary'
            autoFocus
          >
            Delete
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
};
