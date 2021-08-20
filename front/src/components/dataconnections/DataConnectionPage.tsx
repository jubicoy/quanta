import React, { useMemo, useState, useEffect } from 'react';
import { useDataConnections, useDataConnectionsTags } from '../../hooks';
import { searchDataConnections } from '../../client';
import { DataConnectionTable } from '.';
import { Tag } from '../../types';
import {
  Typography as T,
  TextField
} from '@material-ui/core';

import Autocomplete from '@material-ui/lab/Autocomplete';

export default () => {
  const dataConnectionQuery = useMemo(() => ({ notDeleted: true }), []);
  const { dataConnections } = useDataConnections(dataConnectionQuery);
  const { tags } = useDataConnectionsTags();

  const [dataConnectionTags, setDataConnectionTags] = useState<Tag[]>();
  const [filter, setFilter] = useState<number[]>();

  useEffect(
    () => {
      if (dataConnectionTags) {
        const searchTagIds = dataConnectionTags.map(({ id }) => id);
        searchDataConnections(searchTagIds).then(setFilter);
      }
    },
    [dataConnectionTags]
  );

  return (
    <>
      <T variant='h4'>Data connections</T>
      {tags
      && <Autocomplete
        style={{ padding: '10px' }}
        multiple
        size='small'
        options={tags}
        getOptionLabel={(option) => option.name}
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
      { (dataConnectionTags && filter && dataConnectionTags.length > 0 && filter.length === 0)
        ? <T color='error'>No data connection found!</T>
        : <DataConnectionTable
          filter={filter}
          connections={dataConnections}
        />
      }
    </>
  );
};
