export interface ImportWorkerDataConnectionConfiguration {
  type: 'IMPORT_WORKER';
  workerDefId: number;
}

export interface ImportWorkerDataSeriesConfiguration {
  type: 'IMPORT_WORKER';
  parameters: string[];
}

export const DefaultImportWorkerDataConnectionConfiguration: ImportWorkerDataConnectionConfiguration = {
  type: 'IMPORT_WORKER',
  workerDefId: 0
};

export const DefaultImportWorkerDataSeriesConfiguration: ImportWorkerDataSeriesConfiguration = {
  type: 'IMPORT_WORKER',
  parameters: []
};
