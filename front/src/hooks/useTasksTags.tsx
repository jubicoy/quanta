import { useState, useEffect, useCallback } from 'react';
import { Tag } from '../types';
import * as client from '../client';

export const useTasksTags = () => {
  const [tags, setTags] = useState<Tag[] | null>(null);

  const fetchTags = useCallback(
    () => {
      client.getTasksTags().then(setTags);
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
