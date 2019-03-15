export interface JdbcTypeMetadata {
  drivers: JdbcDriver[];
}

export interface JdbcDataConnectionMetadata {
  tables: string[];
}

export interface JdbcDataConnectionConfiguration {
  type: 'JDBC';
  driverJar: string;
  driverClass: string;
  connectionString: string;
  username: string;
  password: string;
}

export interface JdbcDataSeriesConfiguration {
  type: 'JDBC';
  query: string;
}

export interface JdbcDriver {
  jar: string;
  classes: string[];
}

export const DefaultJdbcDataConnectionConfiguration: JdbcDataConnectionConfiguration = {
  type: 'JDBC',
  driverJar: '',
  driverClass: '',
  connectionString: '',
  username: '',
  password: ''
};
