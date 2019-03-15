import { useContext } from 'react';
import {
  __RouterContext,
  RouteComponentProps
} from 'react-router';

const useRouter = <Params extends { [K in keyof Params]?: string } = {}>(): RouteComponentProps<Params> => (
  useContext(__RouterContext) as RouteComponentProps<Params>
);

export default useRouter;
