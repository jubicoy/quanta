import { useState, useEffect, useCallback } from 'react';
import {
  Column,
  ColumnSelector,
  OutputColumnWithInvocation
} from '../types';
import * as client from '../client';
import { arrayDistinctBy } from '../utils';

interface ColumnsContext {
  dataSeriesColumns?: Column[];
  seriesResultColumnSelectors?: ColumnSelector[];
  seriesResultWorkerOutputs?: OutputColumnWithInvocation[];
}

interface ColumnsProps {
  dataConnectionId?: number;
  taskId?: number;
}

export const useColumns = ({
  dataConnectionId,
  taskId
}: ColumnsProps): ColumnsContext => {
  const [dataSeriesColumns, setDataSeriesColumns] = useState<Column[]>();
  const [seriesResultColumnSelectors, setSeriesResultColumnSelectors] = useState<ColumnSelector[]>();
  const [seriesResultWorkerOutputs, setSeriesResultWorkerOutputs] = useState<OutputColumnWithInvocation[]>();

  const fetchColumns = useCallback(
    () => {
      // Fetch DataSeries columns
      if (dataConnectionId) {
        client.getDataConnection(dataConnectionId)
          .then(({ series }) => {
            setDataSeriesColumns(
              series.flatMap(dataSeries =>
                dataSeries.columns.flatMap(col => {
                  return {
                    ...col,
                    series: dataSeries
                  };
                })
              )
            );
          });
      }
      else {
        client.getDataConnections()
          .then(dataConnections => {
            Promise
              .all(
                dataConnections.map(dataConnection =>
                  client.getDataConnection(dataConnection.id)
                )
              )
              .then(dataConnectionsWithSeries => {
                const dataSeriesWithColumns = dataConnectionsWithSeries.flatMap(dataConnection =>
                  dataConnection.series
                );
                setDataSeriesColumns(
                  dataSeriesWithColumns.flatMap(dataSeries =>
                    dataSeries.columns.flatMap(col => {
                      return {
                        ...col,
                        series: dataSeries
                      };
                    })
                  )
                );
              });
          });
      };

      // Fetch SeriesResult columns (Invocation's)
      client.getInvocations({ task: taskId })
        .then(invocations => {
          Promise
            .all(
              invocations.map(invocation =>
                client.getInvocation(invocation.id)
              )
            )
            .then(invocationsWithSeriesResults => {
              const validInvocations = invocationsWithSeriesResults
                .filter(invocation => invocation.status === 'Completed'
                && invocation.seriesResults.length > 0);
              setSeriesResultColumnSelectors(() => {
                return validInvocations
                  .concat(
                    // Add "latest" Invocation only once for each Task
                    arrayDistinctBy(
                      invocation => invocation.task.name,
                      validInvocations
                    ).map((invocation) => {
                      return {
                        ...invocation,
                        invocationNumber: 'latest'
                      };
                    })
                  )
                  .flatMap(invocation =>
                    invocation.columnSelectors
                      .flatMap(columnSelector => {
                        // Avoid recursive nesting
                        const invocationNoColumns = {
                          ...invocation,
                          columnSelectors: []
                        };
                        return {
                          ...columnSelector,
                          invocation: invocationNoColumns
                        };
                      })
                  );
              }
              );
              // Fetch SeriesResult Worker Definition output columns (Invocation's)
              setSeriesResultWorkerOutputs(() => {
                return validInvocations
                  .concat(
                    // Add "latest" Invocation only once for each Task
                    arrayDistinctBy(
                      invocation => invocation.task.name,
                      validInvocations
                    ).map((invocation) => {
                      return {
                        ...invocation,
                        invocationNumber: 'latest'
                      };
                    })
                  )
                  .flatMap(invocation => {
                    return invocation.outputColumns
                      .filter(outputColumn => outputColumn.index !== 0)
                      .map(outputColumn => {
                        return {
                          ...outputColumn,
                          invocation
                        };
                      });
                  });
              }
              );
            });
        });
    },
    [dataConnectionId, taskId]
  );

  useEffect(
    () => {
      fetchColumns();
    },
    [fetchColumns, dataConnectionId, taskId]
  );

  return {
    dataSeriesColumns,
    seriesResultColumnSelectors,
    seriesResultWorkerOutputs
  };
};
