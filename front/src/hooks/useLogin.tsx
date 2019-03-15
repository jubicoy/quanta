import { useEffect, useCallback } from 'react';
import { useInitialContext, useAuth } from '.';
import { useHistory, useLocation } from 'react-router';
import * as client from '../client';
import 'regenerator-runtime/runtime';

import { LoginRequest } from '../types';

interface LoginContext {
  error: boolean;
  loading: boolean;
  login: (loginRequest: LoginRequest) => Promise<void>;
}

export const useLogin = (): LoginContext => {
  const { loading, error } = useInitialContext(false, false);
  const { search } = useLocation();
  const { push } = useHistory();
  const { setAuth, auth } = useAuth();

  const login = useCallback(async (loginRequest: LoginRequest) => {
    const authentication = await client.login(loginRequest);
    setAuth(authentication);
  }, [setAuth]);

  useEffect(() => {
    if (auth !== null) {
      if (search !== '') {
        push(search.replace(/^.*?=/, ''));
      }
      else {
        push('/upload');
      }
    }
  }, [auth, push, search]);

  return {
    error,
    loading,
    login
  };
};
