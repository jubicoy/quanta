import { useState, useEffect } from 'react';
import { DataSeries } from '../types';
import * as client from '../client';

interface DataSeriesContext {
  dataSeries: null | DataSeries;
}

export const useDataSeries = (
  id: number
): DataSeriesContext => {
  const [dataSeries, setDataSeries] = useState<DataSeries | null>(null);

  useEffect(
    () => {
      if (id !== -1) {
        client.getDataSeries(id)
          .then(setDataSeries)
          .catch(() => setDataSeries(null));
      }
      else {
        setDataSeries(null);
      }
    },
    [id]
  );

  return {
    dataSeries
  };
};
