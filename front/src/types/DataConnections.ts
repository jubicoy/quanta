import {
  CsvDataConnectionConfiguration,
  CsvDataSeriesConfiguration,
  JdbcDataConnectionConfiguration,
  JdbcDataSeriesConfiguration,
  JdbcTypeMetadata,
  CsvTypeMetadata,
  JdbcDataConnectionMetadata,
  DefaultCsvDataConnectionConfiguration,
  DefaultCsvDataSeriesConfiguration,
  DefaultJdbcDataConnectionConfiguration,
  Invocation,
  JsonIngestDataConnectionConfiguration,
  DEFAULT_JSON_INGEST_DATA_CONNECTION_CONFIGURATION,
  DEFAULT_JSON_INGEST_DATA_SERIES_CONFIGURATION
} from '.';
import { JsonIngestDataSeriesConfiguration } from './JsonIngestConfigurations';

export type DataConnectionType = 'CSV' | 'JDBC' | 'JSON_INGEST';

export interface TypeMetadata {
  type: DataConnectionType;
  jdbcTypeMetadata?: JdbcTypeMetadata;
  csvMetadata?: CsvTypeMetadata;
}

export interface DataConnectionMetadata {
  type: DataConnectionType;
  jdbcDataConnectionMetadata?: JdbcDataConnectionMetadata;
}

export interface DataConnection {
  id: number;
  name: string;
  description: string;
  type: DataConnectionType;
  configuration: CsvDataConnectionConfiguration | JdbcDataConnectionConfiguration | JsonIngestDataConnectionConfiguration;
  series: DataSeries[];
  deletedAt?: string;
}

export interface DataConnectionQuery {
  notDeleted?: boolean;
}

export interface DataSeries {
  id: number;
  name: string;
  type: DataConnectionType;
  description: string;
  columns: Column[];
  dataConnection?: DataConnection;
  configuration: CsvDataSeriesConfiguration | JdbcDataSeriesConfiguration | JsonIngestDataSeriesConfiguration;
}

export interface Column {
  id: number;
  name: string;
  type: Type;
  index: number;
  series?: DataSeries;
  invocation?: Invocation;
}

export interface Type {
  format?: string | null;
  className: string;
  nullable?: boolean;
}

export function isSupportedType (
  type: Type,
  inputType: Type
) {
  if (type.className === inputType.className) {
    return true;
  }
  if (inputType.className === 'java.lang.String') {
    return true;
  }
  return false;
}

export const NAMING_PATTERN_REGEX = /^[a-zA-Z0-9-_]+$/;

export const SupportedColumnClasses = [
  'java.lang.String',
  'java.lang.Boolean',
  'java.lang.Double',
  'java.time.Instant',
  'java.lang.Long'
];

export const DEFAULT_COLUMN: Column = {
  id: 0,
  name: '',
  type: {
    format: '',
    className: SupportedColumnClasses[0],
    nullable: true
  },
  index: -1
};

export const DEFAULT_JDBC_DATA_CONNECTION: DataConnection = {
  id: 0,
  name: '',
  description: '',
  type: 'JDBC',
  configuration: DefaultJdbcDataConnectionConfiguration,
  series: []
};

export const DEFAULT_JDBC_DATA_SERIES: DataSeries = {
  id: 0,
  name: '',
  description: '',
  type: 'JDBC',
  columns: [],
  configuration: {
    type: 'JDBC',
    query: ''
  }
};

export const DEFAULT_CSV_DATA_CONNECTION: DataConnection = {
  id: 0,
  name: '',
  description: '',
  type: 'CSV',
  configuration: DefaultCsvDataConnectionConfiguration,
  series: []
};

export const DEFAULT_CSV_DATA_SERIES: DataSeries = {
  id: 0,
  name: '',
  type: 'CSV',
  description: '',
  columns: [],
  configuration: DefaultCsvDataSeriesConfiguration
};

export const DEFAULT_JSON_INGEST_DATA_CONNECTION: DataConnection = {
  id: 0,
  name: '',
  description: '',
  type: 'JSON_INGEST',
  configuration: DEFAULT_JSON_INGEST_DATA_CONNECTION_CONFIGURATION,
  series: []
};

export const DEFAULT_JSON_INGEST_DATA_SERIES: DataSeries = {
  id: 0,
  name: '',
  type: 'JSON_INGEST',
  description: '',
  columns: [],
  configuration: DEFAULT_JSON_INGEST_DATA_SERIES_CONFIGURATION
};
