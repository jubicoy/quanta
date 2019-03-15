import { useCallback, useEffect, useState } from 'react';

import * as client from '../client';

import { Task, TaskQuery } from '../types';

interface TaskContext {
  tasks: Task[] | undefined;
  create: (task: Task) => Promise<Task>;
  refresh: () => void;
}

export const useTasks = (
  query?: TaskQuery
): TaskContext => {
  const [tasks, setTasks] = useState<Task[]>([]);

  const create = (set: Task) => client.createTask(set);

  const refresh = useCallback(
    () => {
      client
        .getTasks(query)
        .then(setTasks)
        .catch(() => setTasks([]));
    },
    [query]
  );

  useEffect(
    () => {
      refresh();
    },
    [refresh]
  );

  return {
    tasks,
    create,
    refresh
  };
};
