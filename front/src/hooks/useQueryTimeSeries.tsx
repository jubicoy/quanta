import { useState, useEffect, useCallback } from 'react';
import { TimeSeriesQuery, QueryResult } from '../types';
import * as client from '../client';

interface QueryTimeSeriesContext {
  timeSeriesQueryResult: QueryResult[];
  refresh: () => void;
}

export const useQueryTimeSeries = (
  setLoading: (isLoading: boolean) => void,
  setError: (heading: string, message: string) => void,
  query: TimeSeriesQuery,
  isQueryValid: boolean
): QueryTimeSeriesContext => {
  const [timeSeriesQueryResult, setTimeSeriesQueryResult] = useState<QueryResult[]>([]);

  const refresh = useCallback(
    () => {
      if (query.selectors.length <= 0 || !isQueryValid) {
        // Reset chart if inputs are invalid
        setTimeSeriesQueryResult([]);
        return;
      }
      setLoading(true);
      client
        .queryTimeSeries(query)
        .then((result) => {
          if (result.length <= 0) {
            setError('Query error', 'No results found. You may want to increase the date range.');
            return;
          }
          setTimeSeriesQueryResult(result);
        })
        .catch((response) => {
          setError('Server error', response.toString());
          setTimeSeriesQueryResult([]);
        })
        .finally(() => setLoading(false));
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [query, isQueryValid]
  );

  useEffect(
    () => {
      refresh();
    },
    [refresh]
  );

  return {
    timeSeriesQueryResult,
    refresh
  };
};
