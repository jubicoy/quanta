import React, { useState } from 'react';
import { NavLink } from 'react-router-dom';
import clsx from 'clsx';
import {
  AppBar,
  Container,
  Drawer,
  Icon,
  IconButton,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  ListItemSecondaryAction,
  Snackbar,
  Toolbar
} from '@material-ui/core';
import { amber, green } from '@material-ui/core/colors';
import {
  createStyles,
  makeStyles,
  Theme
} from '@material-ui/core/styles';

import { useAlerts } from '../alert';
import { useAuth } from '../hooks';

import { Hexagon } from './common';

const drawerWidth = 240;

const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    appBar: {
      zIndex: theme.zIndex.drawer + 1,
      transition: theme.transitions.create(['width', 'margin'], {
        easing: theme.transitions.easing.sharp,
        duration: theme.transitions.duration.leavingScreen
      })
    },
    appBarShift: {
      marginLeft: drawerWidth,
      width: `calc(100% - ${drawerWidth}px)`,
      transition: theme.transitions.create(['width', 'margin'], {
        easing: theme.transitions.easing.sharp,
        duration: theme.transitions.duration.enteringScreen
      })
    },
    navItem: {
      color: theme.palette.text.primary
    },
    navLogItem: {
      color: theme.palette.text.primary
    },
    active: {
      backgroundColor: theme.palette.primary.main,
      color: '#000'
    },
    menuButton: {
      marginRight: 36
    },
    hide: {
      display: 'none'
    },
    drawer: {
      width: drawerWidth,
      flexShrink: 0,
      whiteSpace: 'nowrap'
    },
    drawerOpen: {
      width: drawerWidth,
      transition: theme.transitions.create('width', {
        easing: theme.transitions.easing.sharp,
        duration: theme.transitions.duration.enteringScreen
      })
    },
    drawerClose: {
      transition: theme.transitions.create('width', {
        easing: theme.transitions.easing.sharp,
        duration: theme.transitions.duration.leavingScreen
      }),
      overflowX: 'hidden',
      width: theme.spacing(6) + 1,
      [theme.breakpoints.up('sm')]: {
        width: theme.spacing(6) + 1
      }
    },
    toolbar: {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'flex-end',
      ...theme.mixins.toolbar
    },
    content: {
      flexGrow: 1,
      paddingTop: theme.spacing(4)
    },
    contentOpen: {
      marginLeft: drawerWidth
    },
    contentClose: {
      marginLeft: theme.spacing(6) + 1
    },
    snackBar: {
      backgroundColor: 'rgba(0,0,0,0)',
      border: 'none',
      boxShadow: 'none'
    },
    snackBarAlert: {
      borderRadius: 4
    },
    success: {
      backgroundColor: green[600]
    },
    error: {
      backgroundColor: theme.palette.error.dark
    },
    warning: {
      backgroundColor: amber[700]
    }
  })
);

interface Props {
  children: React.ReactNode;
  navItems: {
    path: string;
    icon: string;
    text: string;
  }[];
}

const Layout = ({ children, navItems }: Props) => {
  const classes = useStyles();
  const [open, setOpen] = useState<boolean>(false);
  const { revokeAuth } = useAuth();
  const {
    alerts,
    clearAlert
  } = useAlerts('LAYOUT');

  return <React.Fragment>
    <AppBar
      className={clsx(classes.appBar)}
    >
      <Toolbar disableGutters>
        <IconButton
          onClick={() => setOpen(!open)}
          className={clsx(classes.menuButton)}
        >
          <Icon>menu</Icon>
        </IconButton>
        <div style={{
          flexGrow: 1
        }} />
        <div style={{
          position: 'relative',
          display: 'inline-block',
          lineHeight: '0',
          margin: '2px 0',
          width: '150px'
        }}>
          <Hexagon
            width='50px'
            height='50px'
            strokeColor='#0f0f0f'
            strokeWidth={0}
            fillColor='#fff'
          />
          <div style={{
            position: 'absolute',
            top: '50%',
            left: '14px',
            transform: 'translateY(-50%)',
            fontSize: '30px',
            color: '#0f0f0f',
            fontFamily: 'Prompt'
          }}>Quanta</div>
        </div>
      </Toolbar>
    </AppBar>
    <Drawer
      variant='permanent'
      className={clsx(
        classes.drawer, {
          [classes.drawerOpen]: open,
          [classes.drawerClose]: !open
        })}
      classes={{
        paper: clsx({
          [classes.drawerOpen]: open,
          [classes.drawerClose]: !open
        })
      }}
      open={open}
    >
      <div className={classes.toolbar} />
      <List style={{ paddingTop: 0, height: '90vh' }}>
        {navItems.map((nav) => (
          <ListItem
            key={nav.path}
            disableGutters
            component={NavLink}
            className={classes.navItem}
            activeClassName={classes.active}
            to={nav.path}
          >
            <ListItemIcon style={{ paddingLeft: 12, color: 'inherit' }}>
              <Icon color='inherit'>{nav.icon}</Icon>
            </ListItemIcon>
            <ListItemText
              primary={nav.text}
              primaryTypographyProps={{ variant: 'button' }}
            />
          </ListItem>
        ))}
      </List>
      <List style={{ paddingTop: 0, height: '10vh' }}>
        <ListItem
          key={'logout'}
          disableGutters
          component={NavLink}
          className={classes.navLogItem}
          activeClassName={classes.active}
          to={'/login'}
          onClick={revokeAuth}
        >
          <ListItemIcon style={{ paddingLeft: 12, color: 'inherit' }}>
            <Icon color='inherit'>logout</Icon>
          </ListItemIcon>
          <ListItemText
            primary={'Log out'}
            primaryTypographyProps={{ variant: 'button' }}
          />
        </ListItem>
      </List>
    </Drawer>
    <div className={classes.toolbar} />
    <div className={clsx(
      classes.content, {
        [classes.contentOpen]: open,
        [classes.contentClose]: !open
      })}>
      <Container>
        <Snackbar
          ContentProps={{ className: clsx(classes.snackBar) }}
          open={alerts.length > 0}
          anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
          message={<List dense>
            {alerts.map(alert => (
              <ListItem
                key={alert.key}
                divider
                className={clsx(classes[alert.type], classes.snackBarAlert)}>
                <ListItemText
                  primary={alert.heading}
                  primaryTypographyProps={{ variant: 'button' }}
                  secondary={alert.text}
                  secondaryTypographyProps={{ style: { color: 'white' } }}
                />
                <ListItemSecondaryAction>
                  <IconButton edge='end' onClick={() => clearAlert(alert)}>
                    <Icon style={{ color: 'white' }}>clear</Icon>
                  </IconButton>
                </ListItemSecondaryAction>
              </ListItem>
            ))}
          </List>}
        />
        {children}
      </Container>
    </div>
  </React.Fragment>;
};

export default Layout;
