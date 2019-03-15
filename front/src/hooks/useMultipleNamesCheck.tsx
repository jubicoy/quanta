import { NAMING_PATTERN_REGEX } from '../types';
import { useCallback, useState, useEffect } from 'react';

interface Context {
  namesIsValid: boolean[];
  helperTexts: string[];
  validate: () => void;
}

export const useMultipleNamesCheck = (
  names: (string | undefined)[],
  noDuplicateArray: string[],
  allowEmptyString?: boolean
): Context => {
  const DEFAULT_MESSAGE = 'Allowed characters: letters, numbers, - and _';
  const [namesIsValid, setNamesIsValid] = useState<boolean[]>([true]);
  const [helperTexts, setHelperTexts] = useState<string[]>([DEFAULT_MESSAGE]);

  const validate = useCallback(
    () => {
      names.forEach((name, index) => {
        if (name === undefined || name.length === 0) {
          if (allowEmptyString) {
            setNamesIsValid(prevState => {
              const newState = [...prevState];
              newState[index] = true;
              return newState;
            });
            setHelperTexts(prevState => {
              const newState = [...prevState];
              newState[index] = DEFAULT_MESSAGE;
              return newState;
            });
            return;
          }
          else {
            setNamesIsValid(prevState => {
              const newState = [...prevState];
              newState[index] = false;
              return newState;
            });
            setHelperTexts(prevState => {
              const newState = [...prevState];
              newState[index] = 'String can not be empty';
              return newState;
            });
            return;
          }
        }
        if (!name.match(NAMING_PATTERN_REGEX)) {
          setNamesIsValid(prevState => {
            const newState = [...prevState];
            newState[index] = false;
            return newState;
          });
          setHelperTexts(prevState => {
            const newState = [...prevState];
            newState[index]
              = 'Invalid characters found. Allowed characters: letters, numbers, - and _';
            return newState;
          });
          return;
        }
        else if (
          noDuplicateArray
          && noDuplicateArray
            .some(value => value === name)
        ) {
          setNamesIsValid(prevState => {
            const newState = [...prevState];
            newState[index] = false;
            return newState;
          });
          setHelperTexts(prevState => {
            const newState = [...prevState];
            newState[index] = 'Name is already in use';
            return newState;
          });
          return;
        }

        setNamesIsValid(prevState => {
          const newState = [...prevState];
          newState[index] = true;
          return newState;
        });
        setHelperTexts(prevState => {
          const newState = [...prevState];
          newState[index] = DEFAULT_MESSAGE;
          return newState;
        });
      });
    },
    [names, noDuplicateArray, allowEmptyString]
  );

  useEffect(
    () => {
      validate();
    },
    [validate]
  );

  return {
    namesIsValid,
    helperTexts,
    validate
  };
};
