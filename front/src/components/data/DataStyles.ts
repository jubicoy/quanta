import { makeStyles, createStyles } from '@material-ui/core/styles';
import { grey } from '@material-ui/core/colors';

const tableSelectedBoxShadow = '0px 2px 4px 0px rgba(0, 0, 0, 0.75)';
const typeTextColor = '#ffffff';

export const dataStyles = makeStyles(theme =>
  createStyles({
    dataConnectionName: {
      '&.invalid': {
      }
    },
    tableListItem: {
      cursor: 'pointer',
      paddingLeft: '6px'
    },
    dataConnectionButton: {
      color: '#000',
      backgroundColor: grey[50],
      '&:disabled': {
        backgroundColor: grey[200]
      },
      '&:hover': {
        backgroundColor: grey[300]
      },
      height: '200px',
      width: '90%',
      '& .MuiButton-label': {
        display: 'block',
        textAlign: 'center'
      }
    },
    dataConnectionButtonIcon: {
      color: '#bdbdbd',
      display: 'block',
      margin: 'auto',
      textAlign: 'center',
      fontSize: '10em'
    },
    dbTableList: {
      margin: 0,
      padding: 0,
      maxHeight: '25vh',
      overflowY: 'auto'
    },
    editMode: {
      WebkitTransitionProperty: 'background, border-bottom',
      background: theme.palette.background.default,
      boxShadow: tableSelectedBoxShadow,
      color: typeTextColor,
      opacity: 1,
      // Chrome rendering bug hack
      // (chrome show border between header cell if borderBottom is 0)
      borderBottom: '1px solid ' + theme.palette.background.default,
      WebkitTransition: '200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms',
      transition: '200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms'
    },
    header: {
      padding: theme.spacing(2, 0)
    },
    headerRow: {
      textTransform: 'uppercase',
      fontWeight: 'bold',
      fontSize: '1.1em'
    },
    headerCell: {
      WebkitTransitionProperty: 'background, border-bottom',
      WebkitTransition: '200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms',
      transition: '200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms'
    },
    previewTableCell: {
      borderRight: `1px solid ${theme.palette.divider}`,
      minWidth: '180px'
    },
    previewTableLast: {
      width: '100%'
    },
    queryField: {
      height: '25vh'
    },
    sampleTableGrid: {
      marginTop: theme.spacing(2),
      overflowY: 'hidden',
      overflowX: 'auto'
    },
    typeSelectStickies: {
      position: 'sticky',
      left: '0',
      right: '0',
      overflow: 'hidden'
    },
    stepper: {
      marginBottom: theme.spacing(2)
    },
    tooltip: {
      padding: theme.spacing(2)
    },
    topSpacing: {
      marginTop: theme.spacing(2),
      marginLeft: theme.spacing(1)
    },
    typeText: {
      margin: theme.spacing(0, 2),
      color: typeTextColor
    },
    typography: {
      paddingLeft: 0
    }
  })
);
