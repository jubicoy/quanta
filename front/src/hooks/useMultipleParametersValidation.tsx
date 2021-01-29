import { useCallback, useState, useEffect } from 'react';

import { Parameter, WorkerParameter } from '../types';

interface Context {
  parametersIsValid: boolean[];
  helperTexts: string[];
  validate: () => void;
}

export const useMultipleParametersValidation = (
  parameters?: Parameter[],
  workerParameters?: WorkerParameter[]
): Context => {
  const DEFAULT_MESSAGE = 'Please fill the value of the parameter';
  const [parametersIsValid, setParametersIsValid] = useState<boolean[]>([true]);
  const [helperTexts, setHelperTexts] = useState<string[]>([DEFAULT_MESSAGE]);

  const validate = useCallback(
    () => {
      if (!workerParameters) {
        return;
      }

      // Initialize validation if the parameters have not set yet
      if (!parameters) {
        workerParameters.map((parameter, index) => {
          setParametersIsValid(prevState => {
            const newState = [...prevState];
            newState[index] = true;
            return newState;
          });
          setHelperTexts(prevState => {
            const newState = [...prevState];
            newState[index] = 'Parameter is accepted.';
            return newState;
          });
        });
        return;
      }

      parameters.forEach((parameter, index) => {
        if (parameter.value === null) {
          if (workerParameters.find(workerParam => workerParam.name === parameter.name)?.nullable) {
            setParametersIsValid(prevState => {
              const newState = [...prevState];
              newState[index] = true;
              return newState;
            });
            setHelperTexts(prevState => {
              const newState = [...prevState];
              newState[index] = 'Parameter is accepted.';
              return newState;
            });
          }
          else {
            setParametersIsValid(prevState => {
              const newState = [...prevState];
              newState[index] = false;
              return newState;
            });
            setHelperTexts(prevState => {
              const newState = [...prevState];
              newState[index] = 'Parameter can not be null.';
              return newState;
            });
          }
        }
        else {
          setParametersIsValid(prevState => {
            const newState = [...prevState];
            newState[index] = true;
            return newState;
          });
          setHelperTexts(prevState => {
            const newState = [...prevState];
            newState[index] = 'Parameter is accepted.';
            return newState;
          });
        }
      }
      );
    },
    [parameters, workerParameters]
  );

  useEffect(
    () => {
      validate();
    },
    [validate]
  );

  return {
    parametersIsValid,
    helperTexts,
    validate
  };
};
