import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { login } from '@/api/authApi';
import { authStore } from '@/store/authStore';
import { normalizeEmail } from '@/utils/authRules';
import '@/styles/auth.css';

const LOGIN_FAILED_LINES = ['아이디 또는 비밀번호가 잘못 되었습니다.', '아이디와 비밀번호를 정확히 입력해 주세요.'];

export default function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errorLines, setErrorLines] = useState([]);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!email.trim() || !password) {
      setErrorLines(['이메일과 비밀번호를 입력해 주세요.']);
      return;
    }
    setSubmitting(true);
    setErrorLines([]);
    try {
      const data = await login({ email: normalizeEmail(email), password });
      authStore.login(data);
      // 로그인 필요 팝업에서 넘어왔으면 원래 화면으로, 아니면 메인으로
      navigate(location.state?.from ?? '/', { replace: true });
    } catch (err) {
      const { status, data } = err.response ?? {};
      if (status === 401) setErrorLines(LOGIN_FAILED_LINES);
      else if (status === 403 && data?.message)
        setErrorLines([data.message]); // 정지 계정 (정지 종료 일시 안내)
      else setErrorLines(['로그인하지 못했습니다. 잠시 후 다시 시도해 주세요.']);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-page">
      <form className="auth__card auth__card--narrow" onSubmit={handleSubmit} noValidate>
        <h1 className="auth__title">로그인</h1>

        <div className="auth__field">
          <label className="auth__label" htmlFor="login-email">
            이메일
          </label>
          <input
            id="login-email"
            className="auth__input"
            type="email"
            autoComplete="username"
            placeholder="이메일 주소 입력"
            value={email}
            onChange={(e) => {
              setEmail(e.target.value);
              setErrorLines([]);
            }}
          />
        </div>

        <div className="auth__field">
          <label className="auth__label" htmlFor="login-password">
            비밀번호
          </label>
          <input
            id="login-password"
            className="auth__input"
            type="password"
            autoComplete="current-password"
            placeholder="비밀번호 입력"
            value={password}
            onChange={(e) => {
              setPassword(e.target.value);
              setErrorLines([]);
            }}
          />
          <div className="auth__under-row">
            <div>
              {errorLines.map((line) => (
                <p key={line} className="auth__msg auth__msg--error" role="alert">
                  {line}
                </p>
              ))}
            </div>
            <Link className="auth__link" to="/password-reset">
              비밀번호 찾기
            </Link>
          </div>
        </div>

        <button type="submit" className="auth__primary auth__primary--small" disabled={submitting}>
          {submitting ? '로그인 중…' : '로그인'}
        </button>

        <p className="auth__footer">
          아직 회원이 아니신가요?{' '}
          <Link className="auth__accent-link" to="/register">
            회원가입
          </Link>
        </p>
      </form>
    </div>
  );
}
