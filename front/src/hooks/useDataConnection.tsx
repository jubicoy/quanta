import { useState, useEffect, useCallback } from 'react';
import { DataConnection } from '../types';
import * as client from '../client';

interface DataConnectionContext {
  dataConnection: null | DataConnection;
  deleteDataConnection: () => Promise<DataConnection>;
  updateDataConnection: (updatedDataConnection: DataConnection) => Promise<DataConnection>;
  setDataConnection: (dataConnection: DataConnection | null) => void;
}

export const useDataConnection = (id: number): DataConnectionContext => {
  const [dataConnection, setDataConnection] = useState<null | DataConnection>(null);

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
      return client.updateDataConnection(updatedDataConnection)
        .then(connection => {
          setDataConnection(connection);
          return connection;
        });
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
