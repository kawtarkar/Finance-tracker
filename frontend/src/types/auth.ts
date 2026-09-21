export type Role = 'USER' | 'ADMIN' | string;

export interface User {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: Role;
  enabled: boolean;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}
