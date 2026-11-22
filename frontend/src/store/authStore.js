const STORAGE_KEY = 'cert-sniper-auth';

const empty = { accessToken: null, refreshToken: null, user: null, loginRequired: false };

function load() {
  try {
    const saved = JSON.parse(localStorage.getItem(STORAGE_KEY));
    return saved ? { ...empty, ...saved, loginRequired: false } : empty;
  } catch {
    return empty;
  }
}

let state = load();
const listeners = new Set();

function setState(next) {
  state = { ...state, ...next };
  const { accessToken, refreshToken, user } = state;
  try {
    if (accessToken) localStorage.setItem(STORAGE_KEY, JSON.stringify({ accessToken, refreshToken, user }));
    else localStorage.removeItem(STORAGE_KEY);
  } catch {
    // 저장소를 쓸 수 없으면 메모리에만 유지
  }
  listeners.forEach((listener) => listener());
}

export const authStore = {
  getState: () => state,
  subscribe(listener) {
    listeners.add(listener);
    return () => listeners.delete(listener);
  },
  login(data) {
    setState({
      accessToken: data.access_token,
      refreshToken: data.refresh_token,
      user: { userId: data.user_id, nickname: data.nickname, role: data.role },
    });
  },
  logout() {
    setState({ accessToken: null, refreshToken: null, user: null });
  },
  openLoginRequired() {
    setState({ loginRequired: true });
  },
  closeLoginRequired() {
    setState({ loginRequired: false });
  },
};
