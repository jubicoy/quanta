import React, { useMemo, useState } from 'react';
import { useDataConnections, useTags } from '../../hooks';
import { DataConnectionTable } from '.';
import { DataConnection } from '../../types';
import {
  Typography as T,
  TextField
} from '@material-ui/core';

import Autocomplete from '@material-ui/lab/Autocomplete';

export default () => {
  const dataConnectionQuery = useMemo(() => ({ notDeleted: true }), []);
  const { dataConnections } = useDataConnections(dataConnectionQuery);
  const { tags } = useTags();

  const [dataConnectionTags, setDataConnectionTags] = useState<string[]>();

  const connections: DataConnection[] = useMemo(() => {
    if (dataConnections) {
      if (dataConnectionTags && dataConnectionTags.length > 0) {
        return dataConnections.filter(({ tags }) => dataConnectionTags.every(t => tags.includes(t)));
      }
      return dataConnections;
    }
    return [];
  }, [dataConnections, dataConnectionTags]);

  return (
    <>
      <T variant='h4'>Data connections</T>
      {tags
      && <Autocomplete
        style={{ padding: '10px' }}
        multiple
        size='small'
        options={tags}
        getOptionLabel={(option) => option}
        filterSelectedOptions
        onChange={(event, value) => {
          setDataConnectionTags(value);
        }}
        renderInput={(params) => (
          <TextField
            {...params}
            variant='outlined'
            label='Tags'
            placeholder='Tag'
          />
        )}
      />
      }
      <DataConnectionTable
        connections={connections}
      />
    </>
  );
};
