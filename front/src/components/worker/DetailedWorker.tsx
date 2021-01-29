import React, { useMemo, useState } from 'react';
import {
  LinearProgress,
  Typography as T,
  Icon,
  Fab,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow
} from '@material-ui/core';
import clsx from 'clsx';

import {
  commonStyles,
  InvocationTable,
  TableRowItem,
  WorkerDefColumnTable
} from '../common';
import {
  useInvocations,
  useRouter,
  useWorker
} from '../../hooks';

import { InvocationStatus } from '../../types';

interface TableProps {
  parameters: {
    name: string;
    description: string;
    nullable: boolean;
    type: string;
    defaultValue: string | null;
  }[];
}

const ParamTable = ({ parameters }: TableProps) => (
  <Table>
    <TableHead>
      <TableRow>
        <TableCell style={{ width: '20%' }}>Name</TableCell>
        <TableCell style={{ width: '20%' }}>Description</TableCell>
        <TableCell style={{ width: '20%' }}>Type</TableCell>
        <TableCell style={{ width: '20%' }}>Nullable</TableCell>
        <TableCell style={{ width: '20%' }}>Default value</TableCell>
      </TableRow>
    </TableHead>
    <TableBody>
      {parameters.map(row => (
        <TableRow key={row.name}>
          <TableCell>{row.name}</TableCell>
          <TableCell>{row.description}</TableCell>
          <TableCell>{row.type}</TableCell>
          <TableCell>
            <Icon>
              {row.nullable ? 'check_circle' : 'block_circle'}
            </Icon>
          </TableCell>
          <TableCell>{row.defaultValue}</TableCell>
        </TableRow>
      ))}
    </TableBody>
  </Table>
);

interface Props {
  match: { params: {id: string } };
}

export default ({
  match: { params: { id } }
}: Props) => {
  const {
    worker,
    onAuth,
    onUnauth,
    onDelete
  } = useWorker(parseInt(id));
  const common = commonStyles();
  const { history } = useRouter();
  const [invocationStatus, setInvocationStatus] = useState<InvocationStatus|undefined>(undefined);

  const invocationsQuery = useMemo(
    () => ({
      worker: parseInt(id),
      status: invocationStatus
    }),
    [id, invocationStatus]
  );
  const { invocations } = useInvocations(invocationsQuery);

  if (!worker) {
    return (
      <LinearProgress variant='query' />
    );
  }
  const parameters = worker.definition.parameters || [];

  return (
    <>
      <div className={clsx(common.verticalPadding, common.header)}>
        <div>
          <T variant='h4'>Worker {worker.id}: {worker.definition.name} {worker.deletedAt && '[deleted]'}</T>
        </div>
        <div className={common.toggle}>
          <Fab
            disabled={worker.deletedAt !== null}
            variant='extended'
            color='primary'
            onClick={() => {
              if (worker.acceptedOn) {
                onUnauth();
              }
              else {
                onAuth();
              }
            }}>
            <Icon className={common.icon}>
              {worker.acceptedOn ? 'remove' : 'add'}
            </Icon>
            {worker.acceptedOn ? 'Unauthorize' : 'Authorize'}
          </Fab>
          <Fab
            disabled={worker.deletedAt !== null}
            className={common.leftMargin}
            variant='extended'
            color='primary'
            onClick={() => {
              onDelete()
                .then(() => history.push('/worker-list'));
            }}
          >
            <Icon className={common.icon}>
              {'delete'}
            </Icon>
            Delete
          </Fab>
        </div>
      </div>
      <Paper className={common.bottomMargin}>
        <Table>
          <TableBody>
            <TableRowItem title='Type' value={worker.definition.type} />
            <TableRowItem title='Description' value={worker.definition.description} />
          </TableBody>
        </Table>
      </Paper>
      {parameters.length > 0 && (
        <>
          <T variant='h5'>Parameters</T>
          <Paper className={clsx(common.topMargin, common.bottomMargin)}>
            <ParamTable parameters={parameters} />
          </Paper>
        </>
      )}

      {worker.definition.columns.length > 0 && (
        <>
          <T variant='h5'>Input Columns</T>
          <Paper className={clsx(common.topMargin, common.bottomMargin)}>
            <WorkerDefColumnTable workerColumns={
              worker.definition.columns
                .filter(column => column.columnType === 'input')
                .sort((a, b) => a.index - b.index)
            } />
          </Paper>
          <T variant='h5'>Output Columns</T>
          <Paper className={clsx(common.topMargin, common.bottomMargin)}>
            <WorkerDefColumnTable workerColumns={
              worker.definition.columns
                .filter(column => column.columnType === 'output')
                .sort((a, b) => a.index - b.index)
            } />
          </Paper>
        </>
      )}

      <T variant='h5'>Invocations</T>
      <Paper className={clsx(common.topMargin, common.bottomMargin)}>
        <InvocationTable
          invocations={invocations}
          status={invocationStatus}
          setStatus={setInvocationStatus}
          showWorker
          showTask
        />
      </Paper>
    </>
  );
};
