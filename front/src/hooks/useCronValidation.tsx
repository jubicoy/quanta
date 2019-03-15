import { useCallback, useState, useEffect } from 'react';
import cron from 'cron-validate';

interface Context {
  isCronTriggerValid: boolean;
  cronHelperText: string;
}

const cronValidationOptions = {
  preset: 'aws-cloud-watch',
  override: {
    useSeconds: true,
    useYears: false,
    useBlankDay: true,
    allowOnlyOneBlankDayField: true,
    mustHaveBlankDayField: true
  }
};

export const useCronValidation = (
  cronTrigger?: string
): Context => {
  const DEFAULT_MESSAGE = 'CRON expression is valid';
  const [isCronTriggerValid, setIsCronTriggerValid] = useState<boolean>(true);
  const [cronHelperText, setCronHelperText] = useState<string>(DEFAULT_MESSAGE);

  const validate = useCallback(
    () => {
      if (cronTrigger && cronTrigger.length > 0) {
        const isValid = cron(cronTrigger, cronValidationOptions).isValid();
        setIsCronTriggerValid(isValid);
        if (!isValid) {
          setCronHelperText(
            cron(cronTrigger, cronValidationOptions).getError()[0].split('.')[0]
          );
        }
        else {
          setCronHelperText(DEFAULT_MESSAGE);
        }
      }
      else {
        setIsCronTriggerValid(true);
        setCronHelperText(DEFAULT_MESSAGE);
      }
    },
    [cronTrigger]
  );

  useEffect(
    () => {
      validate();
    },
    [validate]
  );

  return {
    isCronTriggerValid,
    cronHelperText
  };
};
