import React, { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  LinearProgress,
  Typography as T,
  Icon,
  Fab,
  Paper,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle
} from '@material-ui/core';
import clsx from 'clsx';

import {
  ColumnSelectorTable,
  OutputColumnTable,
  InvocationTable,
  commonStyles
} from '../common';
import {
  useTask,
  useInvocations,
  useRouter,
  useTasks,
  useNameCheck,
  useCronValidation
} from '../../hooks';
import {
  Task,
  InvocationStatus,
  TaskType,
  WorkerParameter
} from '../../types';
import { TaskConfiguration } from './TaskConfiguration';

interface Props {
  match: { params: {id: string } };
}

export default ({
  match: { params: { id } }
}: Props) => {
  const {
    task,
    update,
    invoke,
    deleteTask
  } = useTask(parseInt(id));
  const common = commonStyles();
  const { tasks } = useTasks();
  const [editTask, setEditTask] = useState<Task | null>(null);
  const [editOpen, setEditOpen] = useState<boolean>(false);
  const [invocationStatus, setInvocationStatus] = useState<InvocationStatus|undefined>(undefined);
  const [openDeleteDialog, setOpenDeleteDialog] = useState<boolean>(false);

  const invocationsQuery = useMemo(
    () => ({
      task: parseInt(id),
      status: invocationStatus
    }),
    [id, invocationStatus]
  );
  const {
    invocations,
    refresh: refreshInvocations
  } = useInvocations(invocationsQuery);
  const { history } = useRouter();

  const dataSeries = (task && task.columnSelectors.length > 0)
    ? task?.columnSelectors[0].series
    : null;
  const dataConnection = dataSeries?.dataConnection ?? null;

  const triggersAreValid = !(editTask?.cronTrigger && editTask?.taskTrigger);
  const nameArray = useMemo(
    () => {
      return tasks?.filter(({ name }) => name !== task?.name).map(task => task.name) || [];
    },
    [task, tasks]
  );
  const { nameIsValid, helperText } = useNameCheck(
    editTask?.name ?? '',
    nameArray
  );

  const { isCronTriggerValid, cronHelperText } = useCronValidation(
    editTask?.cronTrigger ?? ''
  );

  const isEditTaskValid = (): boolean => {
    return editTask?.name !== ''
    && nameIsValid
    && triggersAreValid
    && isCronTriggerValid;
  };

  useEffect(
    () => {
      setEditTask(task);
      refreshInvocations();
    },
    [refreshInvocations, task]
  );

  if (!task || !editTask) {
    return (
      <LinearProgress variant='query' />
    );
  }

  const onUpdate = () => {
    if (editTask) {
      update(editTask);
    }
    setEditOpen(false);
  };

  const onCancel = () => {
    if (editTask) {
      // Clear task on cancel
      setEditTask(task);
    }
    setEditOpen(false);
  };

  const handleClickOpenDeleteDialog = () => {
    setOpenDeleteDialog(true);
  };

  const handleCloseDeleteDialog = () => {
    setOpenDeleteDialog(false);
  };

  if (task.deletedAt) {
    return (
      <div>
        <T variant='h4'>Task {task.name} has been deleted</T>
      </div>
    );
  }

  return (
    <>
      <div className={common.header}>
        <div>
          <T variant='h4'>Task {task.id}</T>
        </div>
        <div className={common.toggle}>
          <Fab className={common.leftMargin} variant='extended' color='primary'
            onClick={() => {
              history.push(`/task/${task.id}/clients`);
            }} >
            <Icon className={common.icon}>
              computer
            </Icon>
            Manage Clients
          </Fab>
          {editOpen
            ? (
              <>
                <Fab
                  disabled={!isEditTaskValid()}
                  className={common.leftMargin}
                  variant='extended'
                  color='primary'
                  onClick={onUpdate}
                >
                  <Icon className={common.icon}>
                    save_alt
                  </Icon>
                  Save
                </Fab>
                <Fab
                  variant='extended'
                  color='primary'
                  onClick={onCancel}
                  className={common.leftMargin}
                >
                  <Icon className={common.icon}>
                    clear
                  </Icon>
                  Cancel
                </Fab>
              </>
            )
            : (
              <>
                <Fab
                  className={common.leftMargin}
                  variant='extended'
                  color='primary'
                  onClick={() => setEditOpen(true)
                  }>
                  <Icon className={common.icon}>
                    edit
                  </Icon>
                  Edit
                </Fab>
                {task.workerDef?.type === 'Forecast'
                && invocations
                && invocations.filter(i => i.status === 'Completed').length > 0
                && (
                  <Fab
                    variant='extended'
                    color='primary'
                    className={common.leftMargin}
                    href={`/task/${id}/visualization`}
                  >
                    <Icon className={common.icon}>
                      timeline
                    </Icon>
                    Visualize
                  </Fab>
                )}
                <Fab
                  className={common.leftMargin}
                  variant='extended'
                  color='primary'
                  onClick={handleClickOpenDeleteDialog}
                >
                  <Icon className={common.icon}>
                    {'delete'}
                  </Icon>
                  Delete
                </Fab>
                <Fab className={common.leftMargin} variant='extended' color='primary'
                  onClick={() => {
                    invoke();
                    refreshInvocations();
                  }} >
                  <Icon className={common.icon}>
                    play_circle_outline
                  </Icon>
                  Invoke
                </Fab>
              </>
            )
          }
        </div>
      </div>
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
        {
          (task.taskType === TaskType.process)
            && <>
              <ColumnSelectorTable columnSelectors={
                task.columnSelectors
                  .sort((a, b) => a.id - b.id)
              } />
              <OutputColumnTable outputColumns={
                task.outputColumns
                  .sort((a, b) => a.index - b.index)
              } />
            </>
        }
      </Paper>
      <TaskConfiguration
        editable={editOpen}
        nameIsValid={nameIsValid}
        helperText={helperText}
        name={editTask.name}
        setName={name => setEditTask({
          ...editTask,
          name
        })}
        workerDef={editTask.workerDef}
        setWorkerDef={(workerDef, outputColumns, additionalParams) => setEditTask({
          ...editTask,
          workerDef,
          outputColumns,
          additionalParams
        })}
        cronTrigger={editTask.cronTrigger}
        setCronTrigger={cronTrigger => setEditTask({
          ...editTask,
          cronTrigger
        })}
        taskTrigger={editTask.taskTrigger}
        setTaskTrigger={taskTrigger => setEditTask({
          ...editTask,
          taskTrigger
        })}
        configurations={editTask.config}
        onConfigChange={config => setEditTask({
          ...editTask,
          config
        })}
        dataConnection={dataConnection}
        setTaskColumnSelectors={columnSelectors => setEditTask({
          ...editTask,
          columnSelectors
        })}
        setTaskOutputColumns={outputColumns => setEditTask({
          ...editTask,
          outputColumns
        })}
        tasks={tasks?.filter(({ id }) => id !== editTask.id)}
        taskType={editTask.taskType}
        setType={taskType => setEditTask({
          ...editTask,
          taskType
        })}
        additionalParams={task.additionalParams}
        setAdditionalParams={(additionalParams: Record<string, WorkerParameter>) => setEditTask({
          ...task,
          additionalParams
        })}
        isCronTriggerValid={isCronTriggerValid}
        triggersAreValid={triggersAreValid}
        cronHelperText={cronHelperText}
      />
      <T variant='h5'>Invocations</T>
      <Paper className={clsx(common.topMargin, common.bottomMargin)}>
        <InvocationTable
          invocations={
            invocations?.filter(({ invocationNumber }) => invocationNumber !== 'latest')
              .sort((a, b) => (a.invocationNumber as number) - (b.invocationNumber as number)) ?? []
          }
          status={invocationStatus}
          setStatus={setInvocationStatus}
          showWorker
          refreshInvocations={refreshInvocations}
        />
      </Paper>
      <Dialog
        open={openDeleteDialog}
        onClose={handleCloseDeleteDialog}
        aria-labelledby='alert-dialog-title'
        aria-describedby='alert-dialog-description'
      >
        <DialogTitle id='alert-dialog-title'>{'Delete'}</DialogTitle>
        <DialogContent>
          <DialogContentText id='alert-dialog-description'>
            Are you sure want to delete current task? This operation won't delete associated invocations and results
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDeleteDialog} color='primary'>
            Cancel
          </Button>
          <Button
            onClick={() => deleteTask()
              .then(() => history.push('/task-list'))
            }
            color='primary'
            autoFocus>
            Delete
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
};
