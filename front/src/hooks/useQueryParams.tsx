import { useHistory, useLocation } from 'react-router-dom';

interface QueryParamContext {
  get: (key: string) => string | null;
  set: (key: string, value: string) => void;
  remove: (key: string) => void;
}

export const useQueryParams = (): QueryParamContext => {
  const {
    push
  } = useHistory();
  const {
    pathname,
    search
  } = useLocation();
  const query = new URLSearchParams(search);

  const get = (key: string) => query.get(key);

  const set = (key: string, value: string) => {
    query.set(key, value);
    push({
      pathname,
      search: `?${query.toString()}`
    });
  };

  const remove = (key: string) => {
    query.delete(key);
    push({
      pathname,
      search: `?${query.toString()}`
    });
  };

  return {
    get,
    set,
    remove
  };
};
