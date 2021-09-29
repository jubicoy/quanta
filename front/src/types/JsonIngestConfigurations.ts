export interface JsonIngestDataConnectionConfiguration {
  type: 'JSON_INGEST';
  token: string;
}

export interface JsonIngestDataSeriesConfiguration {
  sampleJsonDocument?: string;
  type: 'JSON_INGEST';
  isCollections?: boolean;
  paths: string[];
}

export const DEFAULT_JSON_INGEST_DATA_SERIES_CONFIGURATION: JsonIngestDataSeriesConfiguration = {
  type: 'JSON_INGEST',
  isCollections: false,
  paths: []
};

export const DEFAULT_JSON_INGEST_DATA_CONNECTION_CONFIGURATION: JsonIngestDataConnectionConfiguration = {
  type: 'JSON_INGEST',
  token: ''
};
