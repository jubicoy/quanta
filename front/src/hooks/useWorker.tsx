import { useState, useEffect, useCallback } from 'react';

import * as client from '../client';

import { Worker } from '../types';

interface WorkerContext {
  worker: null | Worker;
  onAuth: () => void;
  onUnauth: () => void;
  onDelete: () => Promise<Worker>;
}

export const useWorker = (
  id: number
): WorkerContext => {
  const [worker, setWorker] = useState<null | Worker>(null);

  const onAuth = useCallback(
    () => client.authWorker(id)
      .then(setWorker),
    [id]
  );

  const onUnauth = useCallback(
    () => client.unauthWorker(id)
      .then(setWorker),
    [id]
  );

  const onDelete = useCallback(
    () => client.deleteWorker(id),
    [id]
  );

  useEffect(
    () => {
      client.getWorker(id)
        .then(setWorker);
    },
    [id]
  );

  return {
    worker,
    onAuth,
    onUnauth,
    onDelete
  };
};
