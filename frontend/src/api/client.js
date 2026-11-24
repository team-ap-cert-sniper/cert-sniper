import axios from 'axios';
import { authStore } from '@/store/authStore';

const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  timeout: 70000,
});

client.interceptors.request.use((config) => {
  const { accessToken } = authStore.getState();
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }
  return config;
});

let refreshing = null;

async function refreshToken() {
  const { refreshToken: token } = authStore.getState();
  if (!token) throw new Error('no refresh token');
  const { data } = await axios.post(`${client.defaults.baseURL}/auth/token-refresh`, { refresh_token: token });
  authStore.login(data);
}

client.interceptors.response.use(
  (response) => response,
  async (error) => {
    const { response, config } = error;
    const code = response?.data?.code;

    if (response?.status === 401 && code === 'INVALID_TOKEN' && !config._retry) {
      config._retry = true;
      try {
        refreshing = refreshing || refreshToken();
        await refreshing;
        return client(config);
      } catch {
        authStore.logout();
      } finally {
        refreshing = null;
      }
    }
    // 로그인 화면의 아이디·비밀번호 불일치(LOGIN_FAILED)는 로그인 필요 팝업 대상이 아님
    if (response?.status === 401 && code !== 'LOGIN_FAILED') {
      authStore.openLoginRequired();
    }
    return Promise.reject(error);
  },
);

export default client;
