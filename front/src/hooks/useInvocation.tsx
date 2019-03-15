import { useState, useEffect } from 'react';
import { Invocation } from '../types';
import * as client from '../client';

interface InvocationContext {
  invocation: Invocation | null;
}

export const useInvocation = (id: number): InvocationContext => {
  const [invocation, setInvocation] = useState<Invocation | null>(null);

  useEffect(
    () => {
      client.getInvocation(id)
        .then(setInvocation);
    },
    [id]
  );

  return {
    invocation
  };
};
