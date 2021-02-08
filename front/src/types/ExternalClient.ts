import { Task, User } from '.';

export interface ExternalClient {
  id: number;
  name: string;
  token: string;
  description: string;
  createdBy: User;
  task: Task | null;
  deletedAt?: number | null;
};
