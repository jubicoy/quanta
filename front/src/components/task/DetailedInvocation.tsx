import React from 'react';
import { Link } from 'react-router-dom';
import {
  LinearProgress,
  Typography as T,
  Paper,
  Table,
  TableBody,
  TableRow,
  TableCell
} from '@material-ui/core';
import clsx from 'clsx';
// Disable time-parsing until backend's Instants-Timestamp problem is sorted out
// import moment from 'moment';

import {
  ColumnSelectorTable,
  OutputColumnTable,
  commonStyles,
  TableRowItem
} from '../common';
import { useInvocation } from '../../hooks';

interface Props {
  match: { params: {id: string } };
}

export default ({
  match: { params: { id } }
}: Props) => {
  const {
    invocation
  } = useInvocation(parseInt(id));
  const common = commonStyles();

  if (!invocation) {
    return (
      <LinearProgress variant='query' />
    );
  }

  const dataSeries = invocation.columnSelectors.length > 0
    ? invocation.columnSelectors[0].series : null;
  const dataConnection = dataSeries
    ? dataSeries.dataConnection : null;

  return (
    <>
      <div className={common.header}>
        <div>
          <T variant='h4'>Invocation {invocation.id}</T>
        </div>
      </div>
      <Paper className={clsx(common.topMargin, common.bottomMargin)}>
        <Table>
          <TableBody>
            <TableRowItem title='Invocation' />
            <TableRowItem title='Invocation number' value={invocation.invocationNumber} />
            <TableRowItem title='Status' value={invocation.status.toLocaleLowerCase()} />
            <TableRowItem
              title='Start time'
              // Temporary fix to time display until backend format is sorted out
              value={invocation.startTime}
            />
            <TableRowItem
              title='End time'
              // Temporary fix to time display until backend format is sorted out
              value={invocation.endTime}
            />
            <TableRow>
              <TableCell
                component='th'
                scope='row'
              >
                <Link to={`/task/${invocation.task.id}`}>
                  Task
                </Link>
              </TableCell>
              <TableCell />
            </TableRow>
            <TableRowItem title='Cron Trigger' value={invocation.task.cronTrigger || ''} />
            <TableRowItem title='Task Trigger' value={invocation.task.taskTrigger || ''} />
            {
              invocation.worker && (
                <>
                  <TableRow>
                    <TableCell
                      component='th'
                      scope='row'
                    >
                      <Link to={`/worker/${invocation.worker?.id}`}>
                        Worker
                      </Link>
                    </TableCell>
                    <TableCell />
                  </TableRow>
                  <TableRowItem title='Type' value={invocation.worker.definition.type} />
                  <TableRowItem title='Token' value={invocation.worker.token} />
                  <TableRowItem title='Name' value={invocation.worker.definition.name} />
                  <TableRowItem title='Description' value={invocation.worker.definition.description} />
                </>
              )
            }
            <TableRowItem title={'Parameters'} value={null} />
            {
              invocation.parameters && invocation.parameters
                .map((workerParameter, index) => (
                  <TableRowItem
                    key={index}
                    title={workerParameter.name}
                    value={workerParameter.value} />
                ))
            }
          </TableBody>
        </Table>
      </Paper>
      <T variant='h5'>Data Connection</T>
      <Paper className={clsx(common.topMargin, common.bottomMargin)}>
        {dataSeries
        && dataConnection
        && (
          <div className={common.padding}>
            <T variant='body1'>
              Connection Name:&nbsp;
              <Link
                to={`/data-connections/${dataConnection.id}/${dataConnection.name}`}
              >
                {dataConnection.name}
              </Link>
            </T>
            <T variant='body1'>Connection Description: <b>{dataConnection.description}</b></T>
            <T variant='body1'>Connection Type: <b>{dataConnection.type}</b></T>
            <T variant='body1'>Series Name: <b>{dataSeries.name}</b></T>
            <T variant='body1'>Series Description: <b>{dataSeries.description}</b></T>
          </div>
        )}
        <ColumnSelectorTable columnSelectors={
          invocation.columnSelectors
            .sort((a, b) => a.columnIndex - b.columnIndex)
        } />
        <OutputColumnTable outputColumns={
          invocation.outputColumns
            .sort((a, b) => a.index - b.index)
        } />
      </Paper>
    </>
  );
};
