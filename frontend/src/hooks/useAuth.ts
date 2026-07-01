import { useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { authApi } from '@/api/auth';
import { useAuthStore } from '@/store/authStore';
import { extractErrorMessage } from '@/api/client';
import { decodeJwtPayload } from '@/utils/jwt';
import type { LoginRequest, RegisterRequest } from '@/types';

export function useLogin() {
  const { setAuth } = useAuthStore();
  const navigate = useNavigate();

  return useMutation({
    mutationFn: (data: LoginRequest) => authApi.login(data),
    onSuccess: async (authResponse) => {
      // Fetch user details to get role
      // We store role from the store; backend /me only returns email.
      // Role comes from user registration — stored in auth response via register.
      // For login, we call /me to get email and decode JWT for userId.
      const payload = decodeJwtPayload(authResponse.accessToken);
      const userId = typeof payload?.userId === 'number' ? payload.userId : 0;
      const email = typeof payload?.sub === 'string' ? payload.sub : '';

      // Use role from backend response if present, otherwise fallback
      const role = authResponse.role ?? 'USER';

      setAuth(authResponse.accessToken, authResponse.refreshToken, {
        email,
        userId,
        role,
      });
      navigate('/dashboard');
    },
    onError: (error) => {
      toast.error(extractErrorMessage(error));
    },
  });
}

export function useRegister() {
  const navigate = useNavigate();

  return useMutation({
    mutationFn: (data: RegisterRequest) => authApi.register(data),
    onSuccess: (_data, variables) => {
      // Store role for the next login
      const store = useAuthStore.getState();
      if (store.user) {
        // Update role for pre-login state (edge case)
        store.setAuth(store.accessToken!, store.refreshToken!, {
          ...store.user,
          role: variables.role,
        });
      }
      toast.success('Account created! Please log in.');
      navigate('/login');
    },
    onError: (error) => {
      toast.error(extractErrorMessage(error));
    },
  });
}

export function useLogout() {
  const { clearAuth, refreshToken } = useAuthStore();
  const navigate = useNavigate();

  return async () => {
    if (refreshToken) {
      try {
        await authApi.logout(refreshToken);
      } catch {
        // Ignore — clear local state regardless
      }
    }
    clearAuth();
    navigate('/login');
  };
}
