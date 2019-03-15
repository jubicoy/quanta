import { NAMING_PATTERN_REGEX } from '../types';
import { useCallback, useState, useEffect } from 'react';

interface Context {
  nameIsValid: boolean;
  helperText: string;
}

export const useNameCheck = (
  name: string,
  arrayToCheck: string[]
): Context => {
  const DEFAULT_MESSAGE = 'Allowed characters: letters, numbers, - and _';
  const [nameIsValid, setNameIsValid] = useState<boolean>(true);
  const [helperText, setHelperText] = useState<string>(DEFAULT_MESSAGE);

  const validate = useCallback(
    () => {
      if (!name.match(NAMING_PATTERN_REGEX)) {
        setNameIsValid(false);
        setHelperText('Invalid characters found. Allowed characters: letters, numbers, - and _');
        return;
      }
      else if (
        arrayToCheck
        && arrayToCheck
          .some(value => value === name)
      ) {
        setNameIsValid(false);
        setHelperText('Name is already in use.');
        return;
      }

      setNameIsValid(true);
      setHelperText(DEFAULT_MESSAGE);
    },
    [name, arrayToCheck]
  );

  useEffect(
    () => {
      validate();
    },
    [validate]
  );

  return {
    nameIsValid,
    helperText
  };
};
