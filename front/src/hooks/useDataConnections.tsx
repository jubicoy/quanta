import { useState, useEffect, useCallback } from 'react';
import { DataConnection, DataConnectionQuery } from '../types';
import * as client from '../client';

interface DataConnectionContext {
  dataConnections: null | DataConnection[];
}

export const useDataConnections = (
  query?: DataConnectionQuery
): DataConnectionContext => {
  const [dataConnections, setDataConnections] = useState<DataConnection[] | null>(null);

  const fetchDataConnections = useCallback(
    () => {
      client.getDataConnections(query)
        .then(setDataConnections);
    },
    [query]
  );

  useEffect(
    () => {
      fetchDataConnections();
    },
    [fetchDataConnections]
  );

  return {
    dataConnections
  };
};
