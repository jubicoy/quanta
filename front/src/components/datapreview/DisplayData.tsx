import React, {
  useEffect,
  useState,
} from 'react';

import {
  queryTimeSeries
} from '../../client';

import {
  DataSeries,
  QueryResult,
  TimeSeriesQuery,
  QUERY_SELECTOR_REGEX,
  columnSelectorToString
} from '../../types';

import {
  Theme,
  createStyles,
  makeStyles,
  useTheme,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableRow,
  TableFooter,
  TableHead,
  TablePagination,
  Paper,
  Typography as T,
  Fab
} from '@material-ui/core';

import IconButton from '@material-ui/core/IconButton';
import FirstPageIcon from '@material-ui/icons/FirstPage';
import KeyboardArrowLeft from '@material-ui/icons/KeyboardArrowLeft';
import KeyboardArrowRight from '@material-ui/icons/KeyboardArrowRight';
import LastPageIcon from '@material-ui/icons/LastPage';
import RefreshIcon from '@material-ui/icons/Refresh';
import { commonStyles } from '../common';



const useStyles1 = makeStyles((theme: Theme) =>
  createStyles({
    root: {
      flexShrink: 0,
      marginLeft: theme.spacing(2.5)
    }
  })
);

const useStyles = makeStyles(theme =>
  createStyles({
    paper: {
      margin: theme.spacing(2, 0)
    },
    row: {
      cursor: 'pointer'
    }
  })
);

interface TablePaginationActionsProps {
  count: number;
  page: number;
  rowsPerPage: number;
  onPageChange: (event: React.MouseEvent<HTMLButtonElement>, newPage: number) => void;
}

function TablePaginationActions (props: TablePaginationActionsProps) {
  const classes = useStyles1();
  const theme = useTheme();
  const { count, page, rowsPerPage, onPageChange } = props;

  const handleFirstPageButtonClick = (event: React.MouseEvent<HTMLButtonElement>) => {
    onPageChange(event, 0);
  };

  const handleBackButtonClick = (event: React.MouseEvent<HTMLButtonElement>) => {
    onPageChange(event, page - 1);
  };

  const handleNextButtonClick = (event: React.MouseEvent<HTMLButtonElement>) => {
    onPageChange(event, page + 1);
  };

  const handleLastPageButtonClick = (event: React.MouseEvent<HTMLButtonElement>) => {
    onPageChange(event, Math.max(0, Math.ceil(count / rowsPerPage) - 1));
  };

  return (
    <div className={classes.root}>
      <IconButton
        onClick={handleFirstPageButtonClick}
        disabled={page === 0}
        aria-label='first page'
      >
        {theme.direction === 'rtl' ? <LastPageIcon /> : <FirstPageIcon />}
      </IconButton>
      <IconButton onClick={handleBackButtonClick} disabled={page === 0} aria-label='previous page'>
        {theme.direction === 'rtl' ? <KeyboardArrowRight /> : <KeyboardArrowLeft />}
      </IconButton>
      <IconButton
        onClick={handleNextButtonClick}
        disabled={page >= Math.ceil(count / rowsPerPage) - 1}
        aria-label='next page'
      >
        {theme.direction === 'rtl' ? <KeyboardArrowLeft /> : <KeyboardArrowRight />}
      </IconButton>
      <IconButton
        onClick={handleLastPageButtonClick}
        disabled={page >= Math.ceil(count / rowsPerPage) - 1}
        aria-label='last page'
      >
        {theme.direction === 'rtl' ? <FirstPageIcon /> : <LastPageIcon />}
      </IconButton>
    </div>
  );
}

interface Props {
  series: DataSeries;
}

export default ({ series }: Props) => {
  const [data, setData] = useState<QueryResult[]>([]);
  const [refreshData, setRefreshData] = useState(false)
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(5);
  const classes = useStyles();
  const common = commonStyles(); 


  const handleChangePage = (event: React.MouseEvent<HTMLButtonElement> | null, newPage: number) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (
    event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  

  useEffect(() => {
    const getData = () => {
      const selectors = series.columns.map(col => `series:${series.name}.${col.name}`);
      const query: TimeSeriesQuery = {
        selectors: selectors
      };
      queryTimeSeries(query).then((res) => {
        setData(res);
      });
    };   
    getData();
  }, [refreshData]);

  
  return (
    <>
      <div className={common.header} style={{display: 'flex', justifyContent:'flex-end'}}>
        <Fab
            variant='extended'
            color='primary'
            onClick={() => setRefreshData(!refreshData)}
          >
            <RefreshIcon />
            Refresh
          </Fab>
      </div>
      <Paper className={classes.paper}>
        {
          data.map(result => (
            <TableContainer>
              <Table aria-label='custom pagination table'>
                <TableHead>
                  <TableRow>
                    {
                      series.columns.map(col => (<TableCell>{col.name}</TableCell>))
                    }
                  </TableRow>
                </TableHead>
                <TableBody>
                  {(rowsPerPage > 0
                    ? result.measurements.sort((a, b) => 
                    {
                      if (new Date(a.time) < new Date(b.time)) {
                        return 1;
                      } else if (new Date(a.time) > new Date(b.time)) {
                        return -1;
                      }
                      return 0;
                    }
                    ).slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                    : result.measurements 
                  ).map(
                    (item, index) =>
                      (
                        <TableRow key={index}>
                          {
                            series.columns.map(col => 
                              {
                              const measurement = Object.entries(item.values).filter(
                                val => {
                                  const matches = val[0].match(QUERY_SELECTOR_REGEX)
                                  if (!matches) {
                                    return null;
                                  }
                                  const column = matches[5];
                                  return column === col.name;
                                })
                                return <TableCell>{measurement[0][1]}</TableCell>;
                            })
                          }
                        </TableRow>
                        ))}
                      </TableBody>
                <TableFooter>
                  <TableRow>
                    <TablePagination
                      rowsPerPageOptions={[5, 10, 20, 50]}
                      colSpan={3}
                      count={result.measurements.length}
                      rowsPerPage={rowsPerPage}
                      page={page}
                      SelectProps={{
                        inputProps: { 'aria-label': 'rows per page' },
                        native: true,
                      }}
                      onPageChange={handleChangePage}
                      onRowsPerPageChange={handleChangeRowsPerPage}
                      ActionsComponent={TablePaginationActions}
                    />
                  </TableRow>
                </TableFooter>
              </Table>
            </TableContainer>
          ))
        }
      </Paper>
    </>
  );
};
