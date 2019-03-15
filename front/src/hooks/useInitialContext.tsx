import { useState } from 'react';
import { RequestContext } from '../types';

export const useInitialContext = (
  initialError: boolean,
  initialLoading: boolean
): RequestContext => {
  const [state, setState] = useState({
    loading: initialLoading,
    error: initialError
  });

  const setRequest = (setError: boolean, setLoading: boolean) => setState({
    loading: setLoading,
    error: setError
  });

  return {
    error: state.error,
    loading: state.loading,
    setRequest
  };
};
