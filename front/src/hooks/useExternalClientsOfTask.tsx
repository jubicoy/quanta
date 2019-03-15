import { useState, useEffect, useCallback } from 'react';
import { ExternalClient } from '../types';
import * as client from '../client';

interface Context {
  externalClients: ExternalClient[] | null;
  create: (newClient: ExternalClient) => Promise<ExternalClient>;
  fetch: () => void;
  deleteClient: (clientId: number) => Promise<Response>;
}

export const useExternalClientsOfTask = (taskId: number): Context => {
  const [externalClients, setExternalClients] = useState<ExternalClient[] | null>(null);

  const fetch = useCallback(
    () => {
      client.getExternalClientsOfTask(taskId)
        .then(setExternalClients);
    },
    [taskId]
  );

  const create = useCallback(
    (newClient: ExternalClient) =>
      client.createExternalClientOfTask(taskId, newClient),
    [taskId]
  );

  const deleteClient = (clientId: number) =>
    client.deleteExternalClient(clientId);

  useEffect(
    () => {
      fetch();
    },
    [fetch]
  );

  return {
    externalClients,
    create,
    fetch,
    deleteClient
  };
};
