import { useState, useEffect } from 'react';
import { DataSeries } from '../types';
import * as client from '../client';

// Get all 'chart-able' time-series
// DataSeries & SeriesResult

interface TimeSeriesContext {
  allDataSeries: DataSeries[];
  // seriesResult: SeriesResult[];
}

export const useTimeSeries = (): TimeSeriesContext => {
  const [allDataSeries, setAllDataSeries] = useState<DataSeries[]>([]);

  const fetchAllSeries = () => {
    client
      .getDataConnections()
      .then(dataConnections => {
        let results: DataSeries[] = [];
        dataConnections.forEach((dataConnection) => {
          client.getDataConnection(dataConnection.id)
            .then((singleDataConnection) => {
              if (singleDataConnection.series && singleDataConnection.series.length > 0) {
                results = results.concat(singleDataConnection.series);
              }

              // When everything is done
              setAllDataSeries(results);
            })
            .catch();
        });
      })
      .catch(() => setAllDataSeries([]));
  };

  // TODO: Also get SeriesResult

  useEffect(
    () => {
      fetchAllSeries();
    },
    []
  );

  return {
    allDataSeries: allDataSeries
  };
};
