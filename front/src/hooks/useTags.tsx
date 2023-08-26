import { useState, useEffect, useCallback } from 'react';
import * as client from '../client';

export const useTags = () => {
  const [tags, setTags] = useState<string[] | null>(null);

  const fetchTags = useCallback(
    () => {
      client.getTags().then(result => {
        const names = result.map(({ name }) => name);
        setTags(names);
      });
    },
    []
  );

  useEffect(
    () => {
      fetchTags();
    },
    [fetchTags]
  );

  return {
    tags, setTags
  };
};
