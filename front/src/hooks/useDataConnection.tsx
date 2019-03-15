import { useState, useEffect, useCallback } from 'react';
import { DataConnection } from '../types';
import * as client from '../client';

interface DataConnectionContext {
  dataConnection: null | DataConnection;
  deleteDataConnection: () => Promise<DataConnection>;
}

export const useDataConnection = (id: number): DataConnectionContext => {
  const [dataConnection, setDataConnection] = useState<DataConnection| null>(null);

  useEffect(
    () => {
      if (id !== -1) {
        client.getDataConnection(id)
          .then(setDataConnection)
          .catch(() => setDataConnection(null));
      }
      else {
        setDataConnection(null);
      }
    },
    [id]
  );

  const deleteDataConnection = useCallback(
    () => client.deleteDataConnection(id),
    [id]
  );

  return {
    dataConnection,
    deleteDataConnection
  };
};
