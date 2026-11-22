import { useSyncExternalStore } from 'react';
import { authStore } from '@/store/authStore';

export default function useAuth() {
  const state = useSyncExternalStore(authStore.subscribe, authStore.getState);
  return {
    user: state.user,
    isLoggedIn: Boolean(state.accessToken),
    isAdmin: state.user?.role === 'ADMIN',
    loginRequired: state.loginRequired,
  };
}
