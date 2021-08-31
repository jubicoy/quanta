import React from 'react';
import {
  FileUploadResponse,
  DataConnection,
  DataSeries,
  DEFAULT_JDBC_DATA_CONNECTION,
  DEFAULT_JDBC_DATA_SERIES,
  Worker
} from '../../types';

import { SampleResponse } from '../../types/Api';

interface DataConnectionConfiguratorContext {
  uploadedData: FileUploadResponse | null;
  setUploadedData: (data: FileUploadResponse | null) => void;

  selectedWorker: Worker | null;
  setSelectedWorker: (data: Worker | null) => void;

  dataConnection: DataConnection;
  setDataConnection: (dataConnection: DataConnection) => void;

  dataSeries: DataSeries;
  setDataSeries: (dataSeries: DataSeries) => void;
  onSubmitDataSeriesSuccess: () => void;

  sampleResponse: SampleResponse | null;
  setSampleResponse: (sampleResponse: SampleResponse | null) => void;

  sampleData: string[][];
  setSampleData: (sampleData: string[][]) => void;

  setUploadProgress: (progress: number | null) => void;

  setSuccess: (heading: string) => void;
  setError: (heading: string, error: Error) => void;
  handleForward: () => void;
  handleBack: () => void;
  // deleteDataSeries
}

export const _DataConnectionConfiguratorContext = React.createContext<DataConnectionConfiguratorContext>({
  /* eslint-disable @typescript-eslint/no-empty-function */
  // Data uploaded to server
  // In context because Step 2 is uploading files
  uploadedData: null,
  setUploadedData: () => {},
  // In context because Step 2
  setUploadProgress: () => {},

  // In context because one DataSeries (containing DataConnection)
  // is maintained in the importing process
  dataConnection: DEFAULT_JDBC_DATA_CONNECTION,
  setDataConnection: () => {},
  dataSeries: DEFAULT_JDBC_DATA_SERIES,
  setDataSeries: () => {},

  // Handle delete data series in ReplaceDataSeries Page
  onSubmitDataSeriesSuccess: () => {},

  // In context to keep response as "source of truth"
  sampleResponse: null,
  setSampleResponse: () => {},

  // In context because sample is get @ Step 2
  // and used @ Step 3
  sampleData: [],
  setSampleData: () => {},

  // In context so every step can show alerts
  setSuccess: () => {},
  setError: () => {},

  selectedWorker: null,
  setSelectedWorker: () => {},

  // In context so every step can operate Stepper
  handleForward: () => {},
  handleBack: () => {}
  /* eslint-enable @typescript-eslint/no-empty-function */
});
