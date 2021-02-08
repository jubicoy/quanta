import React from 'react';
import {
  BrowserRouter as Router,
  Route,
  Redirect,
  Switch
} from 'react-router-dom';
import { hot } from 'react-hot-loader';

import { CssBaseline, createMuiTheme, MuiThemeProvider } from '@material-ui/core';

import {
  Layout,
  DataImportPage
} from '.';

import {
  LoginView,
  AuthLayer
} from './auth/';
import { AuthProvider } from '../hooks';

import { DataConnectionPage } from './dataconnections';
import { DetailedDataConnection } from './dataconnection';
import { TaskPage } from './tasks';
import { DetailedTask, CreateTask, DetailedInvocation } from './task';
import { DetailedWorker } from './worker';
import { WorkerPage } from './workers';
import { ChartsPage, ForecastPage } from './visualization';
import { JsonIngestPage } from './data/json';
import { SettingsPage } from './settings';

import { AlertProvider } from '../alert';

import 'font-awesome/css/font-awesome.min.css';

const theme = createMuiTheme({
  palette: {
    primary: {
      main: '#fbdc4f'
    },
    secondary: {
      main: '#a18712'
    }
  },
  props: {
    MuiTextField: {
      color: 'secondary'
    }
  }
});

class App extends React.PureComponent<{}> {
  render () {
    const navItems = [{
      path: '/upload',
      icon: 'cloud_upload',
      text: 'New data connection'
    }, {
      path: '/data-connections',
      icon: 'storage',
      text: 'Data connections'
    }, {
      path: '/task-list',
      icon: 'alarm_on',
      text: 'Tasks'
    }, {
      path: '/worker-list',
      icon: 'transform',
      text: 'Workers'
    }, {
      path: '/visualization',
      icon: 'timeline',
      text: 'Visualization'
    }, {
      path: '/settings',
      icon: 'settings',
      text: 'Settings'
    }];

    return (
      <MuiThemeProvider theme={theme}>
        <CssBaseline />
        <Router>
          <AuthProvider>
            <AlertProvider>
              <Switch>
                <Route exact path='/login' component={LoginView} />
                <AuthLayer>
                  <Layout navItems={navItems}>
                    <Switch>
                      <Route path='/upload' component={DataImportPage} />
                      <Route exact path='/data-connections' component={DataConnectionPage} />
                      <Route path='/data-connections/:id/:name?' component={DetailedDataConnection} />
                      <Route path='/json-ingest/:id' component={JsonIngestPage} />
                      <Route path='/invocation/:id' component={DetailedInvocation} />
                      <Route path='/task-list' component={TaskPage} />
                      <Route path='/task-new/:dataConnectionId?' component={CreateTask} />
                      <Route exact path='/task/:id' component={DetailedTask} />
                      <Route exact path='/task/:id/visualization' component={ForecastPage} />
                      <Route path='/worker-list' component={WorkerPage} />
                      <Route path='/worker/:id' component={DetailedWorker} />
                      <Route path='/visualization' component={ChartsPage} />
                      <Route path='/settings' component={SettingsPage} />
                      <Redirect from='*' exact to='/upload' />
                    </Switch>
                  </Layout>
                </AuthLayer>
              </Switch>
            </AlertProvider>
          </AuthProvider>
        </Router>
      </MuiThemeProvider>
    );
  }
}

export default hot(module)(App);
