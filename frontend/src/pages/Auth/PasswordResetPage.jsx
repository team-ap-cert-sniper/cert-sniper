import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import PopupModal from '@/components/common/PopupModal';
import { resetPassword, sendEmailCode, verifyEmailCode } from '@/api/authApi';
import { ALLOWED_EMAIL_DOMAINS, MESSAGES, isValidEmail, isValidPassword, normalizeEmail } from '@/utils/authRules';
import '@/styles/auth.css';

const PURPOSE = 'PASSWORD_RESET';
const CODE_LIMIT_SECONDS = 180;

const error = (text) => ({ type: 'error', text });
const success = (text) => ({ type: 'success', text });
const serverMessage = (e, fallback) => e.response?.data?.message ?? fallback;

function FieldMessage({ message }) {
  if (!message) return null;
  return (
    <p className={`auth__msg auth__msg--${message.type}`} role={message.type === 'error' ? 'alert' : 'status'}>
      {message.text}
    </p>
  );
}

/** 비밀번호 재설정 (VIEW-04): 1단계 이메일 인증 → 2단계 새 비밀번호 → 완료 팝업 */
export default function PasswordResetPage() {
  const navigate = useNavigate();
  const [step, setStep] = useState('verify'); // 'verify' | 'reset'

  // 1단계: 이메일 인증
  const [email, setEmail] = useState('');
  const [emailMsg, setEmailMsg] = useState(null);
  const [emailFocused, setEmailFocused] = useState(false);
  const [activeSuggestion, setActiveSuggestion] = useState(-1);
  const [emailSent, setEmailSent] = useState(false);
  const [sending, setSending] = useState(false);
  const [code, setCode] = useState('');
  const [codeMsg, setCodeMsg] = useState(null);
  const [remaining, setRemaining] = useState(0);
  const [verifying, setVerifying] = useState(false);
  const [verified, setVerified] = useState(false);

  // 2단계: 새 비밀번호
  const [password, setPassword] = useState('');
  const [passwordTouched, setPasswordTouched] = useState(false);
  const [passwordConfirm, setPasswordConfirm] = useState('');
  const [submitAttempted, setSubmitAttempted] = useState(false);
  const [submitMsg, setSubmitMsg] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [done, setDone] = useState(false);

  // 인증번호 3분 타이머
  useEffect(() => {
    if (remaining <= 0) return undefined;
    const timer = setTimeout(() => setRemaining(remaining - 1), 1000);
    return () => clearTimeout(timer);
  }, [remaining]);

  const codeExpired = emailSent && !verified && remaining === 0;
  const shownCodeMsg = codeExpired ? error(MESSAGES.codeExpired) : codeMsg;
  const timerText = `${String(Math.floor(remaining / 60)).padStart(2, '0')}:${String(remaining % 60).padStart(2, '0')}`;

  /* ---------- 1단계 ---------- */
  const handleEmailChange = (value) => {
    setEmail(value);
    setEmailMsg(null);
    if (emailSent) {
      setEmailSent(false);
      setRemaining(0);
      setCode('');
      setCodeMsg(null);
    }
  };

  // '@'를 치면 허용 도메인 목록, '@n'처럼 더 치면 그 글자로 시작하는 도메인만 아래에 표시 (회원가입과 같음)
  const emailSuggestions = (() => {
    const at = email.indexOf('@');
    if (at <= 0 || email.indexOf('@', at + 1) !== -1) return [];
    const local = email.slice(0, at);
    const typed = email.slice(at + 1).toLowerCase();
    return ALLOWED_EMAIL_DOMAINS.filter((d) => d.startsWith(typed) && d !== typed).map((d) => `${local}@${d}`);
  })();
  const showSuggestions = emailFocused && !verified && emailSuggestions.length > 0;

  const selectSuggestion = (value) => {
    handleEmailChange(value);
    setActiveSuggestion(-1);
  };

  const handleEmailKeyDown = (e) => {
    if (!showSuggestions) return;
    if (e.key === 'ArrowDown') {
      e.preventDefault();
      setActiveSuggestion((i) => (i + 1) % emailSuggestions.length);
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      setActiveSuggestion((i) => (i <= 0 ? emailSuggestions.length - 1 : i - 1));
    } else if (e.key === 'Enter' && activeSuggestion >= 0) {
      e.preventDefault();
      selectSuggestion(emailSuggestions[activeSuggestion]);
    } else if (e.key === 'Escape') {
      setEmailFocused(false);
    }
  };

  // 이메일 확인 = 가입 여부 확인 + 인증번호 발송 (서버가 미가입 이메일이면 404로 거절)
  const handleCheckEmail = async () => {
    const normalized = normalizeEmail(email);
    setEmail(normalized);
    if (!isValidEmail(normalized)) {
      setEmailMsg(error(MESSAGES.emailInvalid));
      return;
    }
    setSending(true);
    try {
      await sendEmailCode({ email: normalized, purpose: PURPOSE });
      setEmailSent(true);
      setRemaining(CODE_LIMIT_SECONDS);
      setCode('');
      setCodeMsg(null);
      setEmailMsg(success('등록된 이메일입니다. 인증번호를 보냈어요.'));
    } catch (e) {
      if (e.response?.status === 404) setEmailMsg(error('가입되지 않은 이메일입니다. 이메일을 다시 확인해 주세요.'));
      else setEmailMsg(error(serverMessage(e, '인증번호를 보내지 못했습니다. 잠시 후 다시 시도해 주세요.')));
    } finally {
      setSending(false);
    }
  };

  const handleVerifyCode = async () => {
    if (!code) {
      setCodeMsg(error(MESSAGES.codeRequired));
      return;
    }
    if (codeExpired) return;
    setVerifying(true);
    try {
      await verifyEmailCode({ email, purpose: PURPOSE, code });
      setVerified(true);
      setRemaining(0);
      setCodeMsg(success('인증이 완료됐습니다.'));
    } catch (e) {
      setCodeMsg(error(serverMessage(e, MESSAGES.codeMismatch)));
    } finally {
      setVerifying(false);
    }
  };

  /* ---------- 2단계 ---------- */
  const passwordValid = isValidPassword(password);
  const showPasswordError = (passwordTouched || submitAttempted) && !passwordValid;
  let confirmMsg = null;
  if (passwordConfirm) {
    confirmMsg = password === passwordConfirm ? success(MESSAGES.passwordMatch) : error(MESSAGES.passwordMismatch);
  } else if (submitAttempted) {
    confirmMsg = error(MESSAGES.passwordMismatch);
  }

  const handleReset = async (e) => {
    e.preventDefault();
    setSubmitAttempted(true);
    setSubmitMsg(null);
    if (!passwordValid || password !== passwordConfirm) return;
    setSubmitting(true);
    try {
      await resetPassword({ email, new_password: password });
      setDone(true);
    } catch (err) {
      setSubmitMsg(error(serverMessage(err, '비밀번호를 변경하지 못했습니다. 잠시 후 다시 시도해 주세요.')));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-page">
      {step === 'verify' ? (
        <section className="auth__card">
          <h1 className="auth__title">비밀번호 재설정</h1>
          <p className="auth__desc">가입한 이메일을 입력하면 인증번호를 보내드립니다.</p>

          <div className="auth__field auth__field--wide">
            <label className="auth__label" htmlFor="reset-email">
              이메일
            </label>
            <div className="auth__row">
              <div className="auth__input-wrap">
                <input
                  id="reset-email"
                  className="auth__input"
                  type="email"
                  autoComplete="off"
                  placeholder="example@email.com"
                  value={email}
                  disabled={verified}
                  role="combobox"
                  aria-expanded={showSuggestions}
                  aria-controls="reset-email-suggestions"
                  onChange={(e) => {
                    handleEmailChange(e.target.value);
                    setEmailFocused(true);
                    setActiveSuggestion(-1);
                  }}
                  onFocus={() => setEmailFocused(true)}
                  onBlur={() => {
                    setEmailFocused(false);
                    setActiveSuggestion(-1);
                  }}
                  onKeyDown={handleEmailKeyDown}
                />
                {showSuggestions && (
                  <ul id="reset-email-suggestions" className="auth__suggest" role="listbox">
                    {emailSuggestions.map((value, i) => (
                      <li
                        key={value}
                        role="option"
                        aria-selected={i === activeSuggestion}
                        className={i === activeSuggestion ? 'is-active' : undefined}
                        onMouseDown={(e) => {
                          e.preventDefault(); // 입력칸 포커스 유지
                          selectSuggestion(value);
                        }}
                      >
                        {value.slice(value.indexOf('@') + 1)}
                      </li>
                    ))}
                  </ul>
                )}
              </div>
              <button
                type="button"
                className="auth__button"
                disabled={!email || verified || sending}
                onClick={handleCheckEmail}
              >
                {emailSent && !verified ? '인증번호 재발송' : '이메일 확인'}
              </button>
            </div>
            <FieldMessage message={emailMsg} />
          </div>

          <div className="auth__field auth__field--wide">
            <label className="auth__label" htmlFor="reset-code">
              인증코드
            </label>
            <div className="auth__row">
              <div className="auth__input-wrap">
                <input
                  id="reset-code"
                  className="auth__input"
                  inputMode="numeric"
                  maxLength={6}
                  placeholder="인증번호 6자리"
                  value={code}
                  disabled={!emailSent || verified}
                  onChange={(e) => {
                    setCode(e.target.value.replace(/\D/g, ''));
                    setCodeMsg(null);
                  }}
                />
                {emailSent && !verified && <span className="auth__timer">{timerText}</span>}
              </div>
              <button
                type="button"
                className="auth__button"
                disabled={!emailSent || verified || verifying || codeExpired}
                onClick={handleVerifyCode}
              >
                인증 확인
              </button>
            </div>
            <FieldMessage message={shownCodeMsg} />
            {emailSent && !verified && <p className="auth__hint">{MESSAGES.spamNotice}</p>}
          </div>

          <button
            type="button"
            className="auth__primary auth__primary--small"
            disabled={!verified}
            onClick={() => setStep('reset')}
          >
            다음
          </button>
        </section>
      ) : (
        <form className="auth__card" onSubmit={handleReset} noValidate>
          <h1 className="auth__title">비밀번호 재설정</h1>
          <p className="auth__desc">새로 사용할 비밀번호를 입력해 주세요.</p>

          <div className="auth__field auth__field--wide">
            <label className="auth__label" htmlFor="reset-password">
              새 비밀번호
            </label>
            <div className="auth__input-wrap">
              <input
                id="reset-password"
                className="auth__input auth__input--with-badge"
                type="password"
                autoComplete="new-password"
                maxLength={20}
                placeholder={MESSAGES.passwordRule}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                onBlur={() => setPasswordTouched(true)}
              />
              {password && (
                <span
                  className={passwordValid ? 'auth__badge auth__badge--ok' : 'auth__badge auth__badge--ng'}
                  aria-hidden="true"
                >
                  {passwordValid ? 'V' : 'X'}
                </span>
              )}
            </div>
            {showPasswordError && <FieldMessage message={error(MESSAGES.passwordRule)} />}
          </div>

          <div className="auth__field auth__field--wide">
            <label className="auth__label" htmlFor="reset-password-confirm">
              새 비밀번호 확인
            </label>
            <input
              id="reset-password-confirm"
              className="auth__input"
              type="password"
              autoComplete="new-password"
              maxLength={20}
              placeholder="비밀번호 다시 입력"
              value={passwordConfirm}
              onChange={(e) => setPasswordConfirm(e.target.value)}
            />
            <FieldMessage message={confirmMsg} />
          </div>

          <button type="submit" className="auth__primary auth__primary--small" disabled={submitting}>
            {submitting ? '변경 중…' : '비밀번호 변경 완료'}
          </button>
          <FieldMessage message={submitMsg} />
        </form>
      )}

      {done && (
        <PopupModal
          title="비밀번호가 변경되었습니다"
          message="새로운 비밀번호로 로그인해 주세요."
          buttons={[{ text: '로그인하러 가기', onClick: () => navigate('/login') }]}
        />
      )}
    </div>
  );
}
