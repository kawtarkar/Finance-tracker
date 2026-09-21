import axios from 'axios';
import { env } from '../config/env';
import { useAuthStore } from '../features/auth/store/authStore';

export const apiClient = axios.create({
  baseURL: `${env.apiUrl}/api/v1`,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken;

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      useAuthStore.getState().clearAuth();
    }

    return Promise.reject(error);
  },
);
