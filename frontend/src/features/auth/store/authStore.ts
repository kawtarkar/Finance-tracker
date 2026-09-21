import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { AuthResponse, User } from '../../../types/auth';

type AuthState = {
  user: User | null;
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  setAuth: (payload: AuthResponse) => void;
  clearAuth: () => void;
};

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      accessToken: null,
      refreshToken: null,
      isAuthenticated: false,
      setAuth: (payload) =>
        set({
          user: payload.user,
          accessToken: payload.accessToken,
          refreshToken: payload.refreshToken,
          isAuthenticated: true,
        }),
      clearAuth: () =>
        set({
          user: null,
          accessToken: null,
          refreshToken: null,
          isAuthenticated: false,
        }),
    }),
    {
      name: 'finance-tracker-auth',
    },
  ),
);
