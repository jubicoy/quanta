import { useCallback, useEffect, useState } from 'react';

import * as client from '../client';

import { Invocation, InvocationQuery } from '../types';

interface InvocationsContext {
  invocations: Invocation[] | null;
  refresh: () => void;
}

export const useInvocations = (
  query?: InvocationQuery
): InvocationsContext => {
  const [invocations, setInvocations] = useState<Invocation[]>([]);

  const refresh = useCallback(
    () => {
      client
        .getInvocations(query)
        .then(setInvocations)
        .catch(() => setInvocations([]));
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
    invocations,
    refresh
  };
};
