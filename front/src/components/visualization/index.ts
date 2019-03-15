import {
  makeStyles,
  createStyles
} from '@material-ui/core';

import ChartsPage from './ChartsPage';
import ForecastPage from './ForecastPage';

export {
  ChartsPage,
  ForecastPage
};

export interface ChartDataPoint {
  time: number;
  [key: string]: unknown;
};

export const generateColor = (i: number, maxLines: number): string => {
  // Generate colors for chart lines
  // from a maxLines-color palette (to maintain consistency)
  return `hsl(${i * 360 / maxLines}, 50%, 50%)`;
};

export const tooltipStyles = makeStyles(theme =>
  createStyles({
    chartTooltip: {
      backgroundColor: '#fff',
      border: '1px solid #bdbdbd',
      padding: theme.spacing(2),
      '& p': {
        margin: 0
      }
    },
    labelContainer: {
      borderBottom: '1px solid #00000080',
      paddingBottom: theme.spacing(1),
      marginBottom: theme.spacing(1)
    },
    labelDateTime: {
      margin: 0
    },
    contentLegend: {
      width: `.8em`,
      height: `.8em`,
      marginRight: theme.spacing(1),
      display: `inline-block`
    },
    contentName: {
      paddingRight: theme.spacing(1)
    },
    contentValue: {
      float: 'right',
      fontWeight: 'bold'
    }
  })
);
