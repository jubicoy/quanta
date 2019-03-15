import { useEffect, useState } from 'react';
import { Worker, WorkerQuery } from '../types';
import * as client from '../client';

interface WorkerContext {
  workers: Worker[] | null;
}

export const useWorkers = (query?: WorkerQuery): WorkerContext => {
  const [workers, setWorkers] = useState<Worker[] | null>(null);

  useEffect(
    () => {
      client.getWorkers(query)
        .then(setWorkers)
        .catch(() => setWorkers(null));
    },
    [query]
  );

  return {
    workers
  };
};
