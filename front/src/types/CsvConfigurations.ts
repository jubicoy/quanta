export interface CsvTypeMetadata {
  comment: string;
}

export interface CsvDataConnectionConfiguration {
  type: 'CSV';
  path: string;
  initialSyncFileName: string;
}

export interface CsvDataSeriesConfiguration {
  type: 'CSV';
  headers: string[] | null;
  delimiter: string;
  separator: string;
  charset: string;
  quote: string;
}

export const DefaultCsvDataConnectionConfiguration: CsvDataConnectionConfiguration = {
  type: 'CSV',
  path: '',
  initialSyncFileName: ''
};

export const DefaultCsvDataSeriesConfiguration: CsvDataSeriesConfiguration = {
  type: 'CSV',
  headers: null,
  delimiter: ',',
  separator: '\\r\\n',
  charset: 'UTF-8',
  quote: '"'
};

export const configurationIsCsv = (configurations: CsvDataSeriesConfiguration | null): configurations is CsvDataSeriesConfiguration => {
  return (configurations as CsvDataSeriesConfiguration).headers !== undefined
    && (configurations as CsvDataSeriesConfiguration).delimiter !== undefined
    && (configurations as CsvDataSeriesConfiguration).separator !== undefined
    && (configurations as CsvDataSeriesConfiguration).charset !== undefined
    && (configurations as CsvDataSeriesConfiguration).quote !== undefined;
};
