import React, { useState, useEffect } from 'react';
import clsx from 'clsx';
import hljs from 'highlight.js';
import { useDataConnection, useRouter } from '../../hooks';

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
import {
  JsonIngestDataConnectionConfiguration,
  JdbcDataConnectionConfiguration,
  JdbcDriver,
  TypeMetadata,
  DataConnection
} from '../../types';

import { getTypeMetadata } from '../../client';
import { commonStyles } from '../common';

const useStyles = makeStyles((theme) =>
  createStyles({
    paper: {
      margin: theme.spacing(2, 0)
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

const CustomTableCell = ({ editable, name, value, onChange, driverClassOptions, driverJarOptions }: TableCellProps) => {
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
  onChange: (e: React.ChangeEvent<{ name?: string | undefined; value: unknown }>) => void;
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
  const classes = useStyles();
  const { history } = useRouter();
  const [openDeleteDialog, setOpenDeleteDialog] = useState<boolean>(false);
  const [showPassword, setShowPassword] = useState<boolean>(false);
  const [showColumns, setShowColumns] = useState<boolean>(false);

  const [isEditMode, setEditMode] = useState<boolean>(false);

  const [previousConnection, setPreviousConnection] = useState<DataConnection| null>(null);

  const [drivers, setDrivers] = useState<JdbcDriver[]>([]);

  useEffect(() => {
    // Get drivers from server
    if (drivers.length === 0) {
      getTypeMetadata('JDBC')
        .then((result: TypeMetadata) => {
          if (
            result.jdbcTypeMetadata
            && result.jdbcTypeMetadata.drivers.length > 0
          ) {
            setDrivers(result.jdbcTypeMetadata.drivers);
          }
        });
    }
  }, [drivers.length]);

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

  const handleClickOpenDeleteDialog = () => {
    setOpenDeleteDialog(true);
  };

  const handleCloseDeleteDialog = () => {
    setOpenDeleteDialog(false);
  };

  const onChange = (e: React.ChangeEvent<{ name?: string | undefined; value: unknown }>) => {
    if (!e) {
      return undefined;
    }
    const { value, name } = e.target;
    const driver = drivers.find((driver) => driver.jar === value);
    if (name === 'name' || name === 'description') {
      setDataConnection({
        ...dataConnection,
        [name]: value
      });
    }
    else if (name === 'driverJar' && dataConnection.type === 'JDBC') {
      setDataConnection({
        ...dataConnection,
        configuration: {
          ...dataConnection.configuration,
          driverJar: value as string,
          driverClass: driver && driver.classes.length >= 1 ? driver.classes[0] : ''
        }
      });
    }
    else {
      setDataConnection({
        ...dataConnection,
        configuration: {
          ...dataConnection.configuration,
          [name]: value
        }
      });
    }
  };
  const saveEdit = () => {
    setEditMode(false);
    // then and catch here
    // print error here
    updateDataConnection(dataConnection).then(() => setPreviousConnection(dataConnection)).catch((e: Error) => {
      console.log(e);
    });
  };

  const cancelEdit = () => {
    setEditMode(false);
    setDataConnection(previousConnection);
  };

  const dataSeries = dataConnection.series;

  const dataConfiguration = dataConnection.configuration;

  const selectedDriver = drivers.find(
    (driver) => driver.jar === dataConfiguration.driverJar
  );

  const driverJarOptions = drivers.map((driver, idx) => (
    <MenuItem key={idx} value={driver.jar}>
      {driver.jar}
    </MenuItem>
  ));

  const driverClassOptions = selectedDriver
    ? selectedDriver.classes.map((driverClass: string, idx: number) => (
      <MenuItem key={idx} value={driverClass}>
        {driverClass}
      </MenuItem>
    ))
    : [];

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
                    value={dataConfiguration.password}
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
        </div>
      </div>
      <div className={classes.wrapper}>
        <Paper className={classes.paper}>{renderDataConnectionDetails()}</Paper>
        <T variant='h4'>Data series details</T>
        {dataSeries.length <= 0 ? (
          <i>No Data Series available</i>
        ) : (
          dataSeries.map((series, i) => (
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
                          __html: hljs.highlight(series.configuration.query, {
                            language: 'sql'
                          }).value
                        }}
                      />
                    </T>
                  </>}
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
                        onClick={() => setShowColumns(!showColumns)}
                      >
                        {showColumns ? (
                          <KeyboardArrowUpIcon />
                        ) : (
                          <KeyboardArrowDownIcon />
                        )}
                      </IconButton>
                    </TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell className={classes.tableCell} colSpan={8}>
                      <Collapse in={showColumns}>
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
