import { Task } from '.';

export interface ExternalClient {
  id: number;
  name: string;
  token: string;
  task: Task | null;
  deletedAt?: number | null;
};
