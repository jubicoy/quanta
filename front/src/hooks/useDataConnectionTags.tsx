import { useState, useEffect } from 'react';
import { Tag } from '../types';
import * as client from '../client';

export const useDataConnectionTags = (id: number) => {
  const [dataConnectionTags, setDataConnectionTags] = useState<string[] | null>(null);

  useEffect(
    () => {
      if (id !== -1) {
        client.getDataConnectionTags(id)
          .then((result: Tag[]) => {
            const tagNames = result && result.map(({ name }) => name);
            setDataConnectionTags(tagNames);
          }
          )
          .catch(() => setDataConnectionTags(null));
      }
      else {
        setDataConnectionTags(null);
      }
    },
    [id]
  );

  return {
    dataConnectionTags,
    setDataConnectionTags
  };
};
