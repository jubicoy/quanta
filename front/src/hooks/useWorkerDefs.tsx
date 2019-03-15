import { useEffect, useState } from 'react';
import { WorkerDef, WorkerDefQuery } from '../types';
import * as client from '../client';

interface WorkerDefContext {
  workerDefs: WorkerDef[] | null;
}

export const useWorkerDefs = (workerDefQuery?: WorkerDefQuery): WorkerDefContext => {
  const [workerDefs, setWorkerDefs] = useState<WorkerDef[] | null>(null);

  useEffect(
    () => {
      client.getWorkerDefs(workerDefQuery)
        .then(setWorkerDefs)
        .catch(() => setWorkerDefs(null));
    },
    [workerDefQuery]
  );

  return {
    workerDefs
  };
};
