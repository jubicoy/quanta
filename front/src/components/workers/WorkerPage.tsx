import React, { useMemo, useState } from 'react';
import {
  Tabs,
  Tab,
  Typography as T,
  Paper
} from '@material-ui/core';
import clsx from 'clsx';

import { commonStyles } from '../common';
import { useWorkers } from '../../hooks';
import WorkerTable from './WorkerTable';

import { WorkerStatus } from '../../types';

export default () => {
  const common = commonStyles();
  const [activeTab, setActiveTab] = useState(0);

  const status = useMemo(
    () => {
      switch (activeTab) {
        case 1:
          return 'Accepted' as WorkerStatus;
        case 2:
          return 'Pending' as WorkerStatus;
        default:
          return undefined;
      }
    },
    [activeTab]
  );
  const workerQuery = useMemo(
    () => ({
      status,
      notDeleted: true
    }),
    [status]
  );

  const { workers } = useWorkers(workerQuery);

  return (
    <>
      <T variant='h4'>
        Workers
      </T>
      <Paper className={clsx(common.topMargin, common.bottomMargin)}>
        <Tabs
          value={activeTab}
          onChange={(_, v) => setActiveTab(v)}
          indicatorColor='primary'
          textColor='secondary'
          variant='scrollable'
          scrollButtons='auto'
        >
          <Tab label='Show All' />
          <Tab label='Show Authorized' />
          <Tab label='Show Unauthzorized' />
        </Tabs>
      </Paper>
      <WorkerTable
        workers={workers}
      />
    </>
  );
};
