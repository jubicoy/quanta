import {
  Type,
  Column
} from './DataConnections';
import { OutputColumn } from './Task';
import { TIME_SERIES_MODIFIERS } from './TimeSeries';

export interface WorkerParameter {
  id: number;
  name: string;
  description: string;
  type: string;
  nullable: boolean;
  defaultValue: string | null;
}

export type WorkerType = 'Sync' | 'Detect' | 'Forecast' | 'Import';

export type WorkerStatus = 'Accepted' | 'Pending';

export type WorkerDefColumnType = 'input' | 'output';

export interface WorkerQuery {
  status?: WorkerStatus;
  notDeleted?: boolean;
}

export interface WorkerDefQuery {
  notDeleted?: boolean;
}

export interface WorkerDefColumn {
  id: number;
  valueType: Type;
  name: string;
  description: string;
  columnType: WorkerDefColumnType;
  index: number;
}

export interface WorkerDefInputColumnWithSelector extends WorkerDefColumn {
  selectedColumn?: Column;
  alias?: string;
  modifier?: TIME_SERIES_MODIFIERS;
  inputWorkerDefColumn?: WorkerDefInputColumnWithSelector;
}

export interface WorkerDefOutputColumnWithAlias extends WorkerDefColumn {
  alias?: string;
}

export function mapWorkerDefInputColumnWithoutSelector (
  inputColumn: WorkerDefInputColumnWithSelector
): WorkerDefColumn {
  return {
    id: inputColumn.id,
    valueType: inputColumn.valueType,
    name: inputColumn.name,
    description: inputColumn.description,
    columnType: inputColumn.columnType,
    index: inputColumn.index
  };
}

export function mapWorkerDefInputColumnWithSelector (
  inputColumn: WorkerDefColumn
): WorkerDefInputColumnWithSelector {
  return {
    ...inputColumn,
    selectedColumn: undefined,
    modifier: undefined,
    inputWorkerDefColumn: undefined
  };
}

export function mapWorkerDefOutputColumnWithAlias (
  outputColumn: WorkerDefColumn
): WorkerDefOutputColumnWithAlias {
  return {
    ...outputColumn,
    alias: undefined
  };
}

export function mapWorkerDefColumnToOutputColumn (
  workerDefOutputColumn: WorkerDefColumn
): OutputColumn {
  return {
    id: -1,
    index: workerDefOutputColumn.index,
    columnName: workerDefOutputColumn.name,
    type: workerDefOutputColumn.valueType
  };
}

export function mapWorkerDefColumnWithAliasToOutputColumn (
  workerDefColumnWithAlias: WorkerDefOutputColumnWithAlias
): OutputColumn {
  return {
    id: -1,
    index: workerDefColumnWithAlias.index,
    columnName: workerDefColumnWithAlias.name,
    type: workerDefColumnWithAlias.valueType,
    alias: workerDefColumnWithAlias.alias ?? undefined
  };
}

export interface WorkerDef {
  id: number;
  type: WorkerType;
  name: string;
  description: string;
  parameters?: WorkerParameter[];
  columns: WorkerDefColumn[];
}

export interface Worker {
  id: number;
  definition: WorkerDef;
  token: string;
  acceptedOn: string | null;
  lastSeen: string | null;
  deletedAt: string | null;
}
