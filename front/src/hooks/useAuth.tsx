import React, { useState, createContext, ReactNode, useContext, useMemo, useCallback, useEffect } from 'react';
import { useInitialContext } from '../hooks';
import { Authentication } from '../types';
import { useHistory, useLocation } from 'react-router';
import * as client from '../client';

interface AuthContext {
  loading: boolean;
  error: boolean;
  auth: null | Authentication;
  setAuth: (authentication: Authentication) => void;
  revokeAuth: () => void;
}

interface Props {
  children: ReactNode;
}

export const __AuthContext = createContext<AuthContext>(
/* eslint-disable @typescript-eslint/no-empty-function */
  {
    loading: true,
    error: false,
    auth: null,
    setAuth: () => {},
    revokeAuth: () => {}
  }
);

const TOKEN_STRING = 'token';
const EXPIRES_STRING = 'expires';

export const AuthProvider = ({ children }: Props) => {
  const { loading, error, setRequest } = useInitialContext(false, true);
  const [ auth, setStateAuth ] = useState<null | Authentication>(null);
  const { push } = useHistory();
  const { pathname } = useLocation();

  const setAuth = useCallback((authentication: Authentication) => {
    localStorage.setItem(TOKEN_STRING, authentication.token);
    localStorage.setItem(EXPIRES_STRING, authentication.expires);
    setStateAuth(authentication);
  }, [setStateAuth]);

  const revokeAuth = useCallback(() => {
    client.revokeSession();
    localStorage.removeItem(TOKEN_STRING);
    localStorage.removeItem(EXPIRES_STRING);
    setStateAuth(null);
    push('/login');
  }, [setStateAuth, push]);

  const testAuth = useCallback(async () => {
    const token = localStorage.getItem(TOKEN_STRING);
    const expires = localStorage.getItem(EXPIRES_STRING);

    if (token !== null && expires !== null) {
      try {
        const authSession = await client.checkSession(token);

        localStorage.setItem(TOKEN_STRING, authSession.token);
        localStorage.setItem(EXPIRES_STRING, authSession.expires);
        setStateAuth(authSession);
      }
      catch (e) {
        localStorage.removeItem(TOKEN_STRING);
        localStorage.removeItem(EXPIRES_STRING);
      }
    }

    setRequest(false, false);
  }, [setStateAuth, setRequest]);

  useMemo(testAuth, [setStateAuth]);

  useMemo(() => {
    if (auth === null && loading === false && pathname !== '/login') {
      push({
        pathname: '/login',
        search: pathname !== '/'
          ? `?redirect=${pathname}`
          : undefined
      });
    }
  }, [auth, loading, pathname, push]);

  useEffect(() => {
    const timer = setTimeout(testAuth, 1000 * 60 * 30);

    return () => {
      clearTimeout(timer);
    };
  }, [auth, testAuth]);

  return (
    <__AuthContext.Provider
      value={{
        error,
        loading,
        auth,
        setAuth,
        revokeAuth
      }}
    >
      {children}
    </__AuthContext.Provider>
  );
};

export const useAuth = (): AuthContext => useContext(__AuthContext);
