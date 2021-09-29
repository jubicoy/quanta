import {
  DataConnection,
  DataSeries,
  UploadProgress,
  DataConnectionType,
  FileUploadRequest,
  FileUploadResponse,
  SampleResponse,
  DataConnectionMetadata,
  TimeSeriesQuery,
  TypeMetadata,
  Task,
  TaskQuery,
  Invocation,
  InvocationQuery,
  Worker,
  WorkerQuery,
  WorkerDef,
  QueryResult,
  Authentication,
  LoginRequest,
  ExternalClient,
  DataConnectionQuery,
  WorkerDefQuery,
  DataSeriesQuery
} from './types';
import fetchProgress from 'fetch-progress';

const checkResponse = (response: Response): Response => {
  if (!response.ok) {
    throw new Error(`${response.status.toString()}: ${response.statusText}`);
  }
  return response;
};

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const queryEncode = (obj: Record<string, any>): string => {
  const encoded = Object.keys(obj)
    .filter((key) => obj[key] !== undefined)
    .map((key) => {
      return !Array.isArray(obj[key])
        ? `${key}=${encodeURIComponent(obj[key].toString())}`
        : obj[key].map(
          // eslint-disable-next-line @typescript-eslint/no-explicit-any
          (value: Record<string, any[]>) => `${key}=${encodeURIComponent(value.toString())}`)
          .join('&');
    })
    .join('&');

  if (encoded.length > 0) {
    return `?${encoded}`;
  }
  return '';
};

// External clients
export const createExternalClient = (
  client: ExternalClient
): Promise<ExternalClient> => {
  return window
    .fetch(
      `/api/external-clients`,
      {
        method: 'POST',
        headers: {
          'Content-type': 'application/json',
          'Authorization': localStorage.getItem('token') || ''
        },
        body: JSON.stringify(client)
      }
    )
    .then(res => checkResponse(res))
    .then(res => res.json());
};

export const createExternalClientOfTask = (
  client: ExternalClient
): Promise<ExternalClient> => {
  return window
    .fetch(
      `/api/external-clients`,
      {
        method: 'POST',
        headers: {
          'Content-type': 'application/json',
          'Authorization': localStorage.getItem('token') || ''
        },
        body: JSON.stringify(client)
      }
    )
    .then(res => checkResponse(res))
    .then(res => res.json());
};

export const deleteExternalClient = (
  clientId: number
): Promise<Response> => {
  return window
    .fetch(
      `/api/external-clients/${clientId}/delete`,
      {
        method: 'DELETE',
        headers: {
          'Authorization': localStorage.getItem('token') || ''
        }
      }
    )
    .then(res => checkResponse(res));
};

export const getExternalClients = (): Promise<ExternalClient[]> => {
  return window
    .fetch(
      `api/external-clients`,
      {
        method: 'GET',
        headers: {
          'Authorization': localStorage.getItem('token') || ''
        }
      }
    )
    .then(res => checkResponse(res))
    .then(res => res.json());
};

export const getExternalClientsOfTask = (taskId: number): Promise<ExternalClient[]> => {
  return window
    .fetch(
      `/api/external-clients/${taskId}`,
      {
        method: 'GET',
        headers: {
          'Authorization': localStorage.getItem('token') || ''
        }
      }
    )
    .then(res => checkResponse(res))
    .then(res => res.json());
};

// Import-v2
// Files upload
export const uploadFile = (
  request: FileUploadRequest,
  onProgress?: (o: UploadProgress) => void
): Promise<FileUploadResponse> => {
  const data = new FormData(); // eslint-disable-line no-undef
  data.append('file', request.file, request.file.name);

  return window
    .fetch(
      '/api/files', {
        method: 'POST',
        headers: {
          'Authorization': localStorage.getItem('token') || ''
        },
        body: data
      }
    )
    .then(fetchProgress({ onProgress }))
    .then(res => checkResponse(res))
    .then(res => res.json());
};

// Sample (return Column's types and sample rows)
export const sample = (
  dataConnectionId: number,
  request: DataSeries
): Promise<SampleResponse> => {
  return window
    .fetch(
      `/api/data-connections/${dataConnectionId}/sample`,
      {
        method: 'POST',
        headers: {
          'Content-type': 'application/json',
          'Authorization': localStorage.getItem('token') || ''
        },
        body: JSON.stringify(request)
      }
    )
    .then(res => {
      if (res.status === 400) {
        return res.json().then((res: Error) => {
          throw res;
        });
      }

      return checkResponse(res);
    })
    .then(res => res.json());
};

// Submit JSON to ingest
export const ingestJson = (
  dataConnectionToken: string,
  jsonDocument: string
): Promise<Response> => {
  return window
    .fetch(
      '/api/ingest',
      {
        method: 'POST',
        headers: {
          'Data-Connection-Token': dataConnectionToken,
          'Authorization': localStorage.getItem('token') || '',
          'Content-type': 'application/json'
        },
        body: JSON.stringify(
          jsonDocument
        )
      }
    )
    .then(res => checkResponse(res));
};

// Test DataConnection before submitting
export const testDataConnection = (
  dataConnection: DataConnection
): Promise<Response> => {
  return window
    .fetch(
      '/api/data-connections/test',
      {
        method: 'POST',
        headers: {
          'Content-type': 'application/json',
          'Authorization': localStorage.getItem('token') || ''
        },
        body: JSON.stringify(dataConnection)
      }
    )
    .then(res => checkResponse(res));
};

// Submit DataConnection
export const submitDataConnection = (
  dataConnection: DataConnection
): Promise<DataConnection> => {
  return testDataConnection(dataConnection)
    .then(() => window
      .fetch(
        '/api/data-connections',
        {
          method: 'POST',
          headers: {
            'Content-type': 'application/json',
            'Authorization': localStorage.getItem('token') || ''
          },
          body: JSON.stringify(dataConnection)
        }
      ))
    .then(res => checkResponse(res))
    .then(res => res.json());
};

export const updateDataConnection = (
  dataConnection: DataConnection
): Promise<DataConnection> => {
  return testDataConnection(dataConnection)
    .then(() => window
      .fetch(
        '/api/data-connections',
        {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': localStorage.getItem('token') || ''
          },
          body: JSON.stringify(dataConnection)
        }
      ))
    .then(res => checkResponse(res))
    .then(res => res.json());
};

// DataSeries

export const getAllDataSeries = (query: DataSeriesQuery = {}): Promise<DataSeries[]> => window
  .fetch(
    `/api/data-connections/data-series${queryEncode(query)}`,
    {
      method: 'GET',
      headers: {
        'Content-type': 'application/json',
        'Authorization': localStorage.getItem('token') || ''
      }
    }
  )
  .then(checkResponse)
  .then(res => res.json());

export const getDataSeries = (id: number): Promise<DataSeries> => window
  .fetch(
    `/api/data-connections/data-series/${id}`,
    {
      method: 'GET',
      headers: {
        'Content-type': 'application/json',
        'Authorization': localStorage.getItem('token') || ''
      }
    }
  )
  .then(checkResponse)
  .then((res) => res.json());

// Submit DataSeries
export const submitDataSeries = (
  dataConnectionId: number,
  dataSeries: DataSeries,
  skipImportData: boolean
): Promise<DataSeries> => {
  return window
    .fetch(
      `/api/data-connections/${dataConnectionId}/data-series/${skipImportData}`,
      {
        method: 'POST',
        headers: {
          'Content-type': 'application/json',
          'Authorization': localStorage.getItem('token') || ''
        },
        body: JSON.stringify(dataSeries)
      }
    )
    .then(res => checkResponse(res))
    .then(res => res.json());
};

export const deleteDataSeries = (id: number): Promise<DataSeries> => window
  .fetch(
    `/api/data-connections/data-series/${id}`,
    {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': localStorage.getItem('token') || ''
      }
    }
  )
  .then(checkResponse)
  .then((res) => res.json());

// Get DataConnection metadata (tables)
export const getDataConnectionMetadata = (
  id: number
): Promise<DataConnectionMetadata> => {
  return window
    .fetch(
      `/api/data-connections/${id}/metadata`,
      {
        method: 'GET',
        headers: {
          'Content-type': 'application/json',
          'Authorization': localStorage.getItem('token') || ''
        }
      }
    )
    .then(res => checkResponse(res))
    .then(res => res.json());
};

// Get DataConnection type metadata
// JDBC: drivers
// CSV: comment
export const getTypeMetadata = (
  type: DataConnectionType
): Promise<TypeMetadata> => window
  .fetch(
    `/api/data-connection-types/${type.toLowerCase()}`,
    {
      method: 'GET',
      headers: {
        'Content-type': 'application/json',
        'Authorization': localStorage.getItem('token') || ''
      }
    }
  )
  .then(res => checkResponse(res))
  .then(res => res.json());

// DataConnections
export const getDataConnections = (query: DataConnectionQuery = {}): Promise<DataConnection[]> => window
  .fetch(
    `/api/data-connections${queryEncode(query)}`,
    {
      method: 'GET',
      headers: {
        'Content-type': 'application/json',
        'Authorization': localStorage.getItem('token') || ''
      }
    }
  )
  .then(checkResponse)
  .then((res) => res.json());

export const getDataConnection = (id: number): Promise<DataConnection> => window
  .fetch(
    `/api/data-connections/${id}`,
    {
      method: 'GET',
      headers: {
        'Content-type': 'application/json',
        'Authorization': localStorage.getItem('token') || ''
      }
    }
  )
  .then(checkResponse)
  .then((res) => res.json());

export const deleteDataConnection = (id: number): Promise<DataConnection> => window
  .fetch(
    `/api/data-connections/${id}`,
    {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': localStorage.getItem('token') || ''
      }
    }
  )
  .then(checkResponse)
  .then((res) => res.json());

// Query measurements from DataSeries/SeriesResult
export const queryTimeSeries = (
  query: TimeSeriesQuery
): Promise<QueryResult[]> => window
  .fetch(
    `/api/query${queryEncode(query)}`,
    {
      method: 'GET',
      headers: {
        'Content-type': 'application/json',
        'Authorization': localStorage.getItem('token') || '',
        'Accept': 'application/json'
      }
    }
  )
  .then(checkResponse)
  .then((res) => res.json());

// Tasks

export const getTasks = (query: TaskQuery = {}): Promise<Task[]> => window
  .fetch(
    `/api/tasks${queryEncode(query)}`,
    {
      method: 'GET',
      headers: {
        'Content-type': 'application/json',
        'Authorization': localStorage.getItem('token') || ''
      }
    }
  )
  .then(checkResponse)
  .then((res) => res.json());

export const getTask = (id: number): Promise<Task> => window
  .fetch(
    `/api/tasks/${id}`,
    {
      method: 'GET',
      headers: {
        'Content-type': 'application/json',
        'Authorization': localStorage.getItem('token') || ''
      }
    }
  )
  .then(checkResponse)
  .then((res) => res.json());

export const createTask = (task: Task): Promise<Task> => window
  .fetch(
    '/api/tasks',
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': localStorage.getItem('token') || ''
      },
      body: JSON.stringify(task)
    }
  )
  .then(checkResponse)
  .then((res) => res.json());

export const updateTask = (task: Task): Promise<Task> => window
  .fetch(
    '/api/tasks',
    {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': localStorage.getItem('token') || ''
      },
      body: JSON.stringify(task)
    }
  )
  .then(checkResponse)
  .then((res) => res.json());

export const deleteTask = (id: number): Promise<Task> => window
  .fetch(
    `/api/tasks/${id}`,
    {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': localStorage.getItem('token') || ''
      }
    }
  )
  .then(checkResponse)
  .then((res) => res.json());

// Invocations

export const invokeTask = (id: number): Promise<Invocation> => window
  .fetch(
    `/api/tasks/${id}/invoke`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': localStorage.getItem('token') || ''
      }
    }
  )
  .then(checkResponse)
  .then((res) => res.json());

export const getInvocations = (query: InvocationQuery = {}): Promise<Invocation[]> => window
  .fetch(
    `/api/invocations${queryEncode(query)}`,
    {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': localStorage.getItem('token') || ''
      }
    }
  )
  .then(checkResponse)
  .then((res) => res.json());

export const getInvocation = (id: number): Promise<Invocation> => window
  .fetch(
    `/api/invocations/${id}`,
    {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': localStorage.getItem('token') || ''
      }
    }
  )
  .then(checkResponse)
  .then((res) => res.json());

// Workers

export const getWorkers = (query: WorkerQuery = {}): Promise<Worker[]> => window
  .fetch(
    `/api/workers${queryEncode(query)}`,
    {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': localStorage.getItem('token') || ''
      }
    }
  )
  .then(checkResponse)
  .then((res) => res.json());

export const getWorkerDefs = (query: WorkerDefQuery = {}): Promise<WorkerDef[]> => window
  .fetch(
    `/api/worker-definitions${queryEncode(query)}`,
    {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': localStorage.getItem('token') || ''
      }
    }
  )
  .then(checkResponse)
  .then((res) => res.json());

export const getWorker = (id: number): Promise<Worker> => window
  .fetch(
    `/api/workers/${id}`,
    {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': localStorage.getItem('token') || ''
      }
    }
  )
  .then(checkResponse)
  .then((res) => res.json());

export const authWorker = (id: number): Promise<Worker> => window
  .fetch(
    `/api/workers/${id}/authorize`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': localStorage.getItem('token') || ''
      }
    }
  )
  .then(checkResponse)
  .then(res => res.json());

export const unauthWorker = (id: number): Promise<Worker> => window
  .fetch(
    `/api/workers/${id}/unauthorize`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': localStorage.getItem('token') || ''
      }
    }
  )
  .then(checkResponse)
  .then(res => res.json());

export const deleteWorker = (id: number): Promise<Worker> => window
  .fetch(
    `/api/workers/${id}`,
    {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': localStorage.getItem('token') || ''
      }
    }
  )
  .then(checkResponse)
  .then(res => res.json());

export const login = (loginRequest: LoginRequest): Promise<Authentication> => fetch(
  `/api/login`,
  {
    method: 'POST',
    body: JSON.stringify(loginRequest),
    headers: {
      'Content-Type': 'application/json'
    }
  }
)
  .then(checkResponse)
  .then((res) => res.json());

export const checkSession = (token: string): Promise<Authentication> => fetch(
  `/api/me`,
  {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': token
    }
  }
)
  .then(checkResponse)
  .then((res) => res.json());

export const revokeSession = (): Promise<Response> => fetch(
  `/api/logout`,
  {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': localStorage.getItem('token') || ''
    }
  }
)
  .then(checkResponse);
