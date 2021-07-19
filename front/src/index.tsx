import React from 'react';
import ReactDOM from 'react-dom';

import { App } from './components';

import 'material-icons';
import 'highlight.js/styles/github.css';
import 'typeface-roboto';
import './index.less';

const mount = document.getElementById('mount');
const render = () => {
  if (!mount) {
    console.error('No mountpoint found!');
    return;
  }

  ReactDOM.render(<App />, mount);
};

render();

if (module.hot) {
  module.hot.accept('./components', render);
}
