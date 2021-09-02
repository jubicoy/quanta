import {
  Worker,
  WorkerDef,
  WorkerDefColumn
} from './Worker';
import {
  DataSeries,
  Type
} from './DataConnections';
import { TIME_SERIES_MODIFIERS } from '.';

export enum TaskType {
  'process' = 'process',
  'sync' = 'sync',
}

export interface Parameter {
  id: number;
  name: string;
  value: string | null;
}

export interface OutputColumn {
  id: number;
  index: number;
  alias?: string;
  columnName: string;
  type: Type;
};

export interface Task {
  id: number;
  name: string;
  workerDef?: WorkerDef;
  columnSelectors: ColumnSelector[];
  outputColumns: OutputColumn[];
  cronTrigger: string | null;
  taskTrigger: number | null;
  taskType: TaskType;
  deletedAt?: string;
  parameters?: Parameter[];
  syncIntervalOffset?: number;
}

export interface TaskQuery {
  connection?: number;
  workerDef?: number;
  triggeredBy?: number;
  hasCronTrigger?: boolean;
  notDeleted?: boolean;
}

export interface ColumnSelector {
  id: number;
  columnIndex: number;
  columnName: string;
  type: Type;
  modifier?: TIME_SERIES_MODIFIERS;
  alias?: string;
  workerDefColumn?: WorkerDefColumn;
  series: DataSeries;
  invocation?: Invocation;
};

export function columnSelectorToString (
  columnSelector: ColumnSelector
): string {
  if (columnSelector.modifier) {
    return columnSelector.modifier + '(' + columnSelector.columnName + ')';
  }
  return columnSelector.columnName;
}

export type InvocationStatus = 'Error' | 'Completed' | 'Running' | 'Pending';

export interface SeriesResult {
  id: number;
  invocation?: Invocation;
  tableName: string;
}

export interface Invocation {
  id: number;
  invocationNumber: number | 'latest';
  status: InvocationStatus;
  task: Task;
  worker?: Worker;
  startTime: number | null;
  endTime: number | null;
  columnSelectors: ColumnSelector[];
  outputColumns: OutputColumn[];
  seriesResults: SeriesResult[];
  deletedAt?: string;
  parameters?: Parameter[];
}

export interface OutputColumnWithInvocation extends OutputColumn {
  invocation: Invocation;
};

export interface InvocationQuery {
  task?: number;
  worker?: number;
  status?: InvocationStatus;
}
