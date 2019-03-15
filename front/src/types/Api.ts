import { DataSeries } from '.';

export interface UploadProgress {
  eta: number;
  percentage: number;
  remaining: number;
  speed: number;
  total: number;
  transferred: number;
}

// Import-v2
// File upload
export interface FileUploadRequest {
  file: File;
}
export interface FileUploadResponse {
  fileName: string;
}

// Sampling
export interface SampleResponse {
  dataSeries: DataSeries;
  data: string[][];
}

export interface RequestContext {
  error: boolean;
  loading: boolean;
  setRequest: (setError: boolean, setLoading: boolean) => void;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface User {
  id: string;
  role: string;
  name: string;
  creationDate: string;
}

export interface Authentication {
  token: string;
  expires: string;
  user: User;
}
