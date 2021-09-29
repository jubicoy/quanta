import { useMemo } from 'react';
import {
  useMultipleDataSeries,
  useNameCheck
} from './index';

interface Context {
  nameIsValid: boolean;
  helperText: string;
}

export const useDataSeriesNameCheck = (
  name: string
): Context => {
  const { multipleDataSeries } = useMultipleDataSeries();

  const dataSeriesNameArray = useMemo(
    () => {
      return multipleDataSeries?.map(dc => dc.name) || [];
    },
    [multipleDataSeries]
  );

  const { nameIsValid, helperText } = useNameCheck(
    name,
    dataSeriesNameArray
  );

  return {
    nameIsValid,
    helperText
  };
};
