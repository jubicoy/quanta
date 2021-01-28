import { useCallback, useEffect, useState } from 'react';
import * as client from '../client';
import { Task } from '../types';

interface TaskContext {
  task: null | Task;
  update: (set: Task) => void;
  invoke: () => void;
  deleteTask: () => Promise<Task>;
  refresh: () => void;
}

export const useTask = (
  id: number
): TaskContext => {
  const [task, setTask] = useState<null | Task>(null);

  const refresh = useCallback(
    () => {
      client.getTask(id)
        .then(setTask);
    },
    [id]
  );

  const update = useCallback(
    (updatedTask: Task) => {
      client.updateTask(updatedTask)
        .then(setTask);
    },
    []
  );

  const invoke = useCallback(
    () => {
      client.invokeTask(id)
        .then(refresh);
    },
    [id, refresh]
  );

  const deleteTask = useCallback(
    () => client.deleteTask(id),
    [id]
  );

  useEffect(
    () => {
      refresh();
    },
    [refresh]
  );

  return {
    task,
    update,
    invoke,
    deleteTask,
    refresh
  };
};
