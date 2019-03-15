import {
  makeStyles,
  createStyles,
  colors
} from '@material-ui/core';

export const commonStyles = makeStyles(theme =>
  createStyles({
    accept: { color: colors.green[400] },
    unaccept: { color: colors.red[400] },
    allMargin: {
      margin: theme.spacing(2)
    },
    bottomMargin: {
      marginBottom: theme.spacing(2)
    },
    topMargin: {
      marginTop: theme.spacing(2)
    },
    leftMargin: {
      marginLeft: theme.spacing(2)
    },
    horizontalPadding: {
      padding: theme.spacing(0, 2)
    },
    verticalPadding: {
      padding: theme.spacing(2, 0)
    },
    padding: {
      padding: theme.spacing(2)
    },
    leftPadding: {
      paddingLeft: theme.spacing(2)
    },
    floatRight: {
      float: 'right'
    },
    header: {
      display: 'flex',
      justifyContent: 'space-between',
      marginBottom: '5px'
    },
    toggle: {
      display: 'flex',
      alignItems: 'center'
    },
    icon: {
      marginRight: theme.spacing(0.5)
    },
    previewTableCell: {
      borderRight: `1px solid ${theme.palette.divider}`,
      minWidth: '180px'
    },
    previewTableLast: {
      width: '100%'
    },
    row: {
      cursor: 'pointer'
    },
    typography: {
      paddingLeft: 0
    }
  })
);
