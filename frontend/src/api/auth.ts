import apiClient from './client';
import type { AuthResponse, LoginRequest, RegisterRequest, UserResponseDto } from '@/types';

export const authApi = {
  login: async (data: LoginRequest): Promise<AuthResponse> => {
    const res = await apiClient.post<AuthResponse>('/v1/auth/login', data);
    return res.data;
  },

  register: async (data: RegisterRequest): Promise<UserResponseDto> => {
    const res = await apiClient.post<UserResponseDto>('/v1/auth/register', data);
    return res.data;
  },

  refresh: async (refreshToken: string): Promise<AuthResponse> => {
    const res = await apiClient.post<AuthResponse>('/v1/auth/refresh', { refreshToken });
    return res.data;
  },

  logout: async (refreshToken: string): Promise<void> => {
    await apiClient.post('/v1/auth/logout', { refreshToken });
  },

  me: async (): Promise<string> => {
    const res = await apiClient.get<string>('/v1/auth/me');
    return res.data;
  },
};
