import React, { useContext, useEffect, useState } from 'react';
import moment, { Moment } from 'moment';

export interface Alert {
  type: 'error'|'warning'|'success';
  key: string;
  _until?: Moment; // Used internally
  heading: string;
  text: string;
}

interface AlertContext {
  alerts: Alert[];
  clearAlert: (alert: Alert) => void;
  clearAlerts: () => void;
  alertSuccess: (heading: string, text: string) => void;
  alertSuccessPersistent: (heading: string, text: string) => void;
  alertWarning: (heading: string, text: string) => void;
  alertWarningPersistent: (heading: string, text: string) => void;
  alertError: (heading: string, text: string) => void;
  alertErrorPersistent: (heading: string, text: string) => void;
}

export const __AlertContext = React.createContext({
  /* eslint-disable @typescript-eslint/no-empty-function, @typescript-eslint/no-unused-vars */
  alerts: [] as Alert[],
  clearAlert: (alert: Alert) => {},
  clearAlerts: (key: string) => {},
  alertSuccess: (key: string, heading: string, text: string) => {},
  alertSuccessPersistent: (key: string, heading: string, text: string) => {},
  alertWarning: (key: string, heading: string, text: string) => {},
  alertWarningPersistent: (key: string, heading: string, text: string) => {},
  alertError: (key: string, heading: string, text: string) => {},
  alertErrorPersistent: (key: string, heading: string, text: string) => {}
  /* eslint-enable @typescript-eslint/no-empty-function, @typescript-eslint/no-unused-vars */
});

interface Props {
  children: React.ReactNode;
}

export const AlertProvider = ({ children }: Props) => {
  const [alerts, setAlerts] = useState<Alert[]>([]);

  const delay = 5000;

  const clearAlert = (alert: Alert) => {
    setAlerts(alerts.filter(
      filter => filter !== alert
    ));
  };

  const clearAlerts = (key: string) => {
    setAlerts(alerts.filter(
      filter => filter.key !== key
    ));
  };

  const alertSuccess = (key: string, heading: string, text: string) => {
    setAlerts([
      ...alerts.filter(
        filter => filter.key !== key
      ),
      {
        type: 'success',
        key,
        _until: moment().add(delay, 'milliseconds'),
        heading,
        text
      }
    ]);
  };

  const alertSuccessPersistent = (key: string, heading: string, text: string) => {
    setAlerts([
      ...alerts.filter(
        filter => filter.key !== key
      ),
      {
        type: 'success',
        key,
        heading,
        text
      }
    ]);
  };

  const alertWarning = (key: string, heading: string, text: string) => {
    setAlerts([
      ...alerts.filter(
        filter => filter.key !== key
      ),
      {
        type: 'warning',
        key,
        _until: moment().add(delay, 'millisecond'),
        heading,
        text
      }
    ]);
  };

  const alertWarningPersistent = (key: string, heading: string, text: string) => {
    setAlerts([
      ...alerts.filter(
        filter => filter.key !== key
      ),
      {
        type: 'warning',
        key,
        heading,
        text
      }
    ]);
  };

  const alertError = (key: string, heading: string, text: string) => {
    setAlerts([
      ...alerts.filter(
        filter => filter.key !== key
      ),
      {
        type: 'error',
        key,
        _until: moment().add(delay, 'millisecond'),
        heading,
        text
      }
    ]);
  };

  const alertErrorPersistent = (key: string, heading: string, text: string) => {
    setAlerts([
      ...alerts.filter(
        filter => filter.key !== key
      ),
      {
        type: 'error',
        key,
        heading,
        text
      }
    ]);
  };

  useEffect(() => {
    const interval = setInterval(
      () => {
        const now = moment();

        const filteredAlerts = alerts.filter(
          (alert: Alert, index) => (
            !alert._until
            || !moment.isMoment(alert._until)
            || now.isBefore(alert._until)
          )
          && alerts.findIndex(find => find === alert) === index
        );

        if (filteredAlerts.length !== alerts.length) {
          setAlerts(filteredAlerts);
        }
      },
      500
    );
    return () => clearInterval(interval);
  });

  return (
    <__AlertContext.Provider
      value={{
        alerts,
        clearAlert,
        clearAlerts,
        alertSuccess,
        alertSuccessPersistent,
        alertWarning,
        alertWarningPersistent,
        alertError,
        alertErrorPersistent
      }}
    >
      {children}
    </__AlertContext.Provider>
  );
};

export const useAlerts = (key: string): AlertContext => {
  const context = useContext(__AlertContext);
  const {
    alerts,
    clearAlert
  } = context;

  const clearAlerts = () => context.clearAlerts(key);
  const alertSuccess = (heading: string, text: string) => context.alertSuccess(key, heading, text);
  const alertSuccessPersistent = (heading: string, text: string) => context.alertSuccessPersistent(key, heading, text);
  const alertWarning = (heading: string, text: string) => context.alertWarning(key, heading, text);
  const alertWarningPersistent = (heading: string, text: string) => context.alertWarningPersistent(key, heading, text);
  const alertError = (heading: string, text: string) => context.alertError(key, heading, text);
  const alertErrorPersistent = (heading: string, text: string) => context.alertErrorPersistent(key, heading, text);

  return {
    alerts,
    clearAlert,
    clearAlerts,
    alertSuccess,
    alertSuccessPersistent,
    alertWarning,
    alertWarningPersistent,
    alertError,
    alertErrorPersistent
  };
};
