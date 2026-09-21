import { apiClient } from '../../../lib/apiClient';
import type { AuthResponse, LoginRequest, RefreshTokenRequest, RegisterRequest } from '../../../types/auth';

export const authApi = {
  register: (payload: RegisterRequest) =>
    apiClient.post('/auth/register', payload),

  login: (payload: LoginRequest) =>
    apiClient.post<AuthResponse>('/auth/login', payload),

  me: () => apiClient.get('/auth/me'),

  refreshToken: (payload: RefreshTokenRequest) =>
    apiClient.post<AuthResponse>('/auth/refresh-token', payload),

  logout: () => apiClient.post('/auth/logout'),

  forgotPassword: (email: string) =>
    apiClient.post('/auth/forgot-password', { email }),

  resetPassword: (token: string, password: string, confirmPassword: string) =>
    apiClient.post('/auth/reset-password', {
      token,
      newPassword: password,
      confirmPassword,
    }),

  verifyEmail: (token: string) => apiClient.get(`/auth/verify-email?token=${token}`),
};
