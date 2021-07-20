import React, { useState, useEffect } from 'react';
import {
  MenuItem
} from '@material-ui/core';
import { getTypeMetadata } from '../client';
import {
  JdbcDriver,
  TypeMetadata
} from '../types';

interface DriversContext {
  drivers: JdbcDriver[];
  driverJarOptions: JSX.Element[] | null;
}

export const useDrivers = (): DriversContext => {
  const [drivers, setDrivers] = useState<JdbcDriver[]>([]);
  useEffect(() => {
    // Get drivers from server
    if (drivers.length === 0) {
      getTypeMetadata('JDBC')
        .then((result: TypeMetadata) => {
          if (
            result.jdbcTypeMetadata
            && result.jdbcTypeMetadata.drivers.length > 0
          ) {
            setDrivers(result.jdbcTypeMetadata.drivers);
          }
        });
    }
  }, [drivers.length]);

  const driverJarOptions = drivers.map((driver, idx) => (
    <MenuItem key={idx} value={driver.jar}>
      {driver.jar}
    </MenuItem>
  ));

  return {
    drivers,
    driverJarOptions
  };
};
