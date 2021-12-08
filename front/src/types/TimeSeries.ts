import {
  Type
} from './DataConnections';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export interface TimeSeriesQuery extends Record<string, any> {
  start?: string;
  end?: string;
  selectors: string[];
  interval?: string;
}

export interface Measurement {
  time: string;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  values: Record<string, any>;
}

export interface QueryResult {
  dataSeriesId?: number;
  seriesResultId?: number;
  measurements: Measurement[];
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  groupingParams: Record<string, any>;
}

/* eslint-disable no-multi-spaces */
export const QUERY_SELECTOR_REGEX
  = '^(?:(avg|min|max|sum|group_by|where|count)\\()?'       // G1: (Optional) Modifiers (grouping/modifier)
  + '(series|result|result_output):'                        // G2: Type
  + '([a-zA-Z0-9-_]+)'                                      // G3: Series name
  + '(?:\\.([0-9]+|latest))?'                               // G4: (Optional) InvocationNumber
  + '\\.([a-zA-Z0-9-_]+)'                                   // G5: Column name
  + '((?:\\s)?(?:=|!=|>|>=|<|<=)(?:\\s)?\'[^\'()]+\')?\\)?' // G6: (Optional) Filter condition
  + '(?: as ([a-zA-Z0-9-_]+))?$';                           // G7: (Optional) Alias
/* eslint-enable no-multi-spaces */

export enum VALID_DATA_TYPES {
  'java.lang.Integer' = 'java.lang.Integer',
  'java.lang.Long' = 'java.lang.Long',
  'java.lang.Float' = 'java.lang.Float',
  'java.lang.Double' = 'java.lang.Double'
};

export enum TIME_SERIES_MODIFIERS {
  'avg' = 'avg',
  'min' = 'min',
  'max' = 'max',
  'sum' = 'sum',
  'group_by' = 'group_by',
  'where' = 'where',
  'count' = 'count'
}

export enum TIME_SERIES_QUERY_TYPE {
  'series' = 'series',
  'result' = 'result',
  'result_output' = 'result_output'
}

export class DataSeriesSelector {
  name: string;
  columnName: string;

  constructor (name: string, columnName: string) {
    this.name = name;
    this.columnName = columnName;
  }

  toString = () => {
    return `series:${this.name}.${this.columnName}`;
  };
}

export class SeriesResultSelector {
  name: string;
  columnName: string;
  invocationSelector: string;

  constructor (name: string, columnName: string, invocationSelector: string) {
    this.name = name;
    this.columnName = columnName;
    this.invocationSelector = invocationSelector;
  }

  toString = () => {
    return `result:${this.name}.${this.invocationSelector}.${this.columnName}`;
  };
}

export class SeriesResultOutputSelector {
  name: string;
  columnName: string;
  invocationSelector: string;

  constructor (name: string, columnName: string, invocationSelector: string) {
    this.name = name;
    this.columnName = columnName;
    this.invocationSelector = invocationSelector;
  }

  toString = () => {
    return `result_output:${this.name}.${this.invocationSelector}.${this.columnName}`;
  };
}

export class QuerySelector {
  modifier?: TIME_SERIES_MODIFIERS;
  alias?: string;
  selector: DataSeriesSelector | SeriesResultSelector | SeriesResultOutputSelector;
  filterCondition?: string;

  constructor (
    selector: DataSeriesSelector | SeriesResultSelector | SeriesResultOutputSelector,
    modifier?: TIME_SERIES_MODIFIERS,
    alias?: string,
    filterCondition?: string
  ) {
    this.modifier = modifier;
    this.selector = selector;
    this.alias = alias;
    this.filterCondition = filterCondition;
  }

  getType = (): string => {
    if (this.selector instanceof DataSeriesSelector) {
      return TIME_SERIES_QUERY_TYPE.series;
    }
    else if (this.selector instanceof SeriesResultSelector) {
      return TIME_SERIES_QUERY_TYPE.result;
    }
    else if (this.selector instanceof SeriesResultOutputSelector) {
      return TIME_SERIES_QUERY_TYPE.result_output;
    }
    throw new Error('Invalid QuerySelector.Type');
  };

  setFilterCondition = (filterCondition: string) => {
    this.filterCondition = filterCondition;
  };

  toString = (aliasSelector = true) => {
    const selectorHasAlias = this.alias && this.alias.length > 0;
    if (this.modifier) {
      if (this.modifier === TIME_SERIES_MODIFIERS.where && this.filterCondition) {
        return `where(${this.selector.toString()} ${this.filterCondition})`;
      }
      if (selectorHasAlias && aliasSelector) {
        return `${this.modifier}(${this.selector.toString()}) as ` + this.alias;
      }
      return `${this.modifier}(${this.selector.toString()})`;
    }
    else {
      if (selectorHasAlias && aliasSelector) {
        return `${this.selector.toString()} as ` + this.alias;
      }
      return `${this.selector.toString()}`;
    }
  };
};

export function supportedModifiers (
  inputType: Type
): TIME_SERIES_MODIFIERS[] {
  if (inputType.className === 'java.lang.Double' || inputType.className === 'java.lang.Long') {
    return [
      TIME_SERIES_MODIFIERS.avg,
      TIME_SERIES_MODIFIERS.group_by,
      TIME_SERIES_MODIFIERS.max,
      TIME_SERIES_MODIFIERS.min,
      TIME_SERIES_MODIFIERS.sum
    ];
  }
  if (inputType.className === 'java.lang.String'
    || inputType.className === 'java.time.Instant') {
    return [
      TIME_SERIES_MODIFIERS.group_by
    ];
  }

  return [];
}
