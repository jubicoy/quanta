import { useState, useEffect, useCallback } from 'react';
import { DataConnection } from '../types';
import * as client from '../client';

interface DataConnectionContext {
  dataConnection: null | DataConnection;
  deleteDataConnection: () => Promise<DataConnection>;
  updateDataConnection: (updatedDataConnection: DataConnection | null) => Promise<DataConnection>;
  setDataConnection: (dataConnection: DataConnection) => void;
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

  const updateDataConnection = useCallback(
    (updatedDataConnection: DataConnection) => {
      client.updateDataConnection(updatedDataConnection)
        .then(setDataConnection);
    },
    []
  );

  return {
    dataConnection,
    deleteDataConnection,
    setDataConnection,
    updateDataConnection
  };
};
