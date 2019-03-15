import React, { useMemo } from 'react';
import { useDataConnections } from '../../hooks';
import { DataConnectionTable } from '.';

import {
  Typography as T
} from '@material-ui/core';

export default () => {
  const dataConnectionQuery = useMemo(() => ({ notDeleted: true }), []);
  const { dataConnections } = useDataConnections(dataConnectionQuery);

  return (
    <>
      <T variant='h4'>Data connections</T>
      <DataConnectionTable
        connections={dataConnections}
      />
    </>
  );
};
