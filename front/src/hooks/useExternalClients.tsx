import { useState, useEffect, useCallback } from 'react';
import { ExternalClient } from '../types';
import * as client from '../client';

interface Context {
  externalClients: ExternalClient[] | null;
  create: (newClient: ExternalClient) => Promise<ExternalClient>;
  fetch: () => void;
  deleteClient: (clientId: number) => Promise<Response>;
}

export const useExternalClients = (
): Context => {
  const [externalClients, setExternalClients] = useState<ExternalClient[] | null>(null);

  const fetch = useCallback(
    () => {
      client.getExternalClients()
        .then(setExternalClients)
        .catch(() => setExternalClients([]));
    },
    []
  );

  const create = useCallback(
    (newClient: ExternalClient) =>
      client.createExternalClient(newClient),
    []
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
