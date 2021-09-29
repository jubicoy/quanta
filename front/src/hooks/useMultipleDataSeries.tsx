import { useState, useEffect, useCallback } from 'react';
import { DataSeries, DataSeriesQuery } from '../types';
import * as client from '../client';

interface DataSeriesContext {
  multipleDataSeries: null | DataSeries[];
}

export const useMultipleDataSeries = (
  query?: DataSeriesQuery
): DataSeriesContext => {
  const [multipleDataSeries, setMultipleDataSeries] = useState<DataSeries[] | null>(null);

  const fetchDataSeries = useCallback(
    () => {
      client.getAllDataSeries(query)
        .then(setMultipleDataSeries)
        .catch(() => setMultipleDataSeries(null));
    },
    [query]
  );

  useEffect(
    () => {
      fetchDataSeries();
    },
    [fetchDataSeries]
  );

  return {
    multipleDataSeries
  };
};
