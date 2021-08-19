import { useState, useEffect } from 'react';
import { Tag } from '../types';
import * as client from '../client';

export const useTaskTags = (id: number) => {
  const [taskTags, setTaskTags] = useState<string[] | null>(null);

  useEffect(
    () => {
      if (id !== -1) {
        client.getTaskTags(id)
          .then((result: Tag[]) => {
            const tagNames = result && result.map(({ name }) => name);
            setTaskTags(tagNames);
          }
          )
          .catch(() => setTaskTags(null));
      }
      else {
        setTaskTags(null);
      }
    },
    [id]
  );

  return {
    taskTags,
    setTaskTags
  };
};
