import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AlertModal from '@/components/common/AlertModal';
import SearchSelect from '@/components/common/SearchSelect';
import { checkNickname, register, sendEmailCode, verifyEmailCode } from '@/api/authApi';
import { getOptions } from '@/api/optionApi';
import { GRADES } from '@/constants/grades';
import {
  ALLOWED_EMAIL_DOMAINS,
  MESSAGES,
  isValidEmail,
  isValidName,
  isValidPassword,
  nicknameError,
  normalizeEmail,
} from '@/utils/authRules';
import '@/styles/register.css';

const PURPOSE = 'REGISTER';
const CODE_LIMIT_SECONDS = 180;

const error = (text) => ({ type: 'error', text });
const success = (text) => ({ type: 'success', text });
const serverMessage = (e, fallback) => e.response?.data?.message ?? fallback;

/** 입력칸 아래 안내 문구: 상황에 맞는 문구 하나만 표시 (빨강 = 오류, 초록 = 성공) */
function FieldMessage({ message }) {
  if (!message) return null;
  return (
    <p className={`register__msg register__msg--${message.type}`} role={message.type === 'error' ? 'alert' : 'status'}>
      {message.text}
    </p>
  );
}

function Label({ htmlFor, children, required }) {
  return (
    <label className="register__label" htmlFor={htmlFor}>
      {children}
      {required && <span className="register__required"> *</span>}
    </label>
  );
}

function SearchIcon() {
  return (
    <svg className="register__icon" width="20" height="20" viewBox="0 0 24 24" aria-hidden="true">
      <circle cx="11" cy="11" r="7" fill="none" stroke="currentColor" strokeWidth="1.8" />
      <path d="M20 20l-4-4" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" />
    </svg>
  );
}

function ChevronIcon() {
  return (
    <svg className="register__icon" width="20" height="20" viewBox="0 0 24 24" aria-hidden="true">
      <path d="M6 9l6 6 6-6" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" />
    </svg>
  );
}

export default function RegisterPage() {
  const navigate = useNavigate();

  // 1. 계정 정보
  const [email, setEmail] = useState('');
  const [emailMsg, setEmailMsg] = useState(null);
  const [emailFocused, setEmailFocused] = useState(false);
  const [activeSuggestion, setActiveSuggestion] = useState(-1);
  const [emailSent, setEmailSent] = useState(false);
  const [emailVerified, setEmailVerified] = useState(false);
  const [sending, setSending] = useState(false);
  const [code, setCode] = useState('');
  const [codeMsg, setCodeMsg] = useState(null);
  const [remaining, setRemaining] = useState(0);
  const [verifying, setVerifying] = useState(false);
  const [password, setPassword] = useState('');
  const [passwordTouched, setPasswordTouched] = useState(false);
  const [passwordConfirm, setPasswordConfirm] = useState('');

  // 2. 기본 정보
  const [name, setName] = useState('');
  const [nameMsg, setNameMsg] = useState(null);
  const [nickname, setNickname] = useState('');
  const [nicknameMsg, setNicknameMsg] = useState(null);
  const [checkedNickname, setCheckedNickname] = useState(null); // 사용 가능 확인을 받은 닉네임
  const [checkingNickname, setCheckingNickname] = useState(false);

  // 3. 학력 및 관심 정보
  const [noSchool, setNoSchool] = useState(false);
  const [university, setUniversity] = useState(null);
  const [major, setMajor] = useState(null);
  const [grade, setGrade] = useState('');
  const [schoolMsg, setSchoolMsg] = useState(null);
  const [certs, setCerts] = useState([]);
  const [certSearchKey, setCertSearchKey] = useState(0);

  // 제출
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

  const codeExpired = emailSent && !emailVerified && remaining === 0;
  const shownCodeMsg = codeExpired ? error(MESSAGES.codeExpired) : codeMsg;
  const timerText = `${String(Math.floor(remaining / 60)).padStart(2, '0')}:${String(remaining % 60).padStart(2, '0')}`;

  /* ---------- 이메일 ---------- */
  const handleEmailChange = (value) => {
    setEmail(value);
    if (emailSent) {
      // 발송 후 이메일을 바꾸면 인증을 처음부터 다시
      setEmailSent(false);
      setRemaining(0);
      setCode('');
      setCodeMsg(null);
    }
    setEmailMsg(null);
  };

  const handleEmailBlur = () => {
    setEmailFocused(false);
    setActiveSuggestion(-1);
    if (email && !isValidEmail(normalizeEmail(email))) setEmailMsg(error(MESSAGES.emailInvalid));
  };

  // '@'를 치면 허용 도메인 목록, '@g'처럼 더 치면 그 글자로 시작하는 도메인만 아래에 표시
  const emailSuggestions = (() => {
    const at = email.indexOf('@');
    if (at <= 0 || email.indexOf('@', at + 1) !== -1) return [];
    const local = email.slice(0, at);
    const typed = email.slice(at + 1).toLowerCase();
    return ALLOWED_EMAIL_DOMAINS.filter((d) => d.startsWith(typed) && d !== typed).map((d) => `${local}@${d}`);
  })();
  const showSuggestions = emailFocused && !emailVerified && emailSuggestions.length > 0;

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

  const handleSendCode = async () => {
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
      setEmailMsg(success(MESSAGES.emailSent));
    } catch (e) {
      // 409 이미 가입된 이메일, 429 요청 횟수 초과 등은 서버 문구 그대로
      setEmailMsg(error(serverMessage(e, '인증번호를 보내지 못했습니다. 잠시 후 다시 시도해 주세요.')));
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
      setEmailVerified(true);
      setRemaining(0);
      setEmailMsg(null);
      setCodeMsg(success(MESSAGES.emailVerified));
    } catch (e) {
      // 불일치·만료·시도 횟수 초과 구분은 서버 문구를 따름
      setCodeMsg(error(serverMessage(e, MESSAGES.codeMismatch)));
    } finally {
      setVerifying(false);
    }
  };

  /* ---------- 비밀번호 ---------- */
  const passwordValid = isValidPassword(password);
  const showPasswordError = (passwordTouched || submitAttempted) && !passwordValid;
  let confirmMsg = null;
  if (passwordConfirm) {
    confirmMsg = password === passwordConfirm ? success(MESSAGES.passwordMatch) : error(MESSAGES.passwordMismatch);
  } else if (submitAttempted) {
    confirmMsg = error(MESSAGES.passwordMismatch);
  }

  /* ---------- 이름 ---------- */
  const validateName = (value) => {
    if (!value.trim()) return error(MESSAGES.nameRequired);
    if (!isValidName(value)) return error(MESSAGES.nameLength);
    return null;
  };

  /* ---------- 닉네임 ---------- */
  const handleNicknameChange = (value) => {
    setNickname(value);
    setCheckedNickname(null);
    setNicknameMsg(null);
  };

  const handleCheckNickname = async () => {
    const value = nickname.trim();
    const ruleError = nicknameError(value);
    if (ruleError) {
      setNicknameMsg(error(ruleError));
      return;
    }
    setCheckingNickname(true);
    try {
      const result = await checkNickname(value);
      if (result.available) {
        setCheckedNickname(value);
        setNicknameMsg(success(MESSAGES.nicknameAvailable));
      } else {
        setCheckedNickname(null);
        setNicknameMsg(error(MESSAGES.nicknameTaken));
      }
    } catch (e) {
      setNicknameMsg(error(serverMessage(e, '닉네임을 확인하지 못했습니다. 잠시 후 다시 시도해 주세요.')));
    } finally {
      setCheckingNickname(false);
    }
  };

  /* ---------- 학력 ---------- */
  const fetchUniversities = useCallback((query) => getOptions('universities', { query, limit: 50 }), []);
  const fetchMajors = useCallback(
    (query) =>
      university
        ? getOptions('majors', { university_id: university.id, query, limit: 50 })
        : Promise.resolve({ items: [] }),
    [university],
  );
  const fetchQualifications = useCallback((query) => getOptions('qualifications', { query, limit: 50 }), []);

  const handleNoSchool = (checked) => {
    setNoSchool(checked);
    setUniversity(null);
    setMajor(null);
    setGrade('');
    setSchoolMsg(null);
  };

  const handleSelectUniversity = (item) => {
    setUniversity(item);
    setMajor(null); // 학교가 바뀌면 전공 다시 선택
    setSchoolMsg(null);
  };

  const validateSchool = () => {
    if (noSchool) return null;
    if (university && major && grade) return null;
    return error(MESSAGES.schoolIncomplete);
  };

  /* ---------- 보유 자격증 ---------- */
  const handleSelectCert = (item) => {
    if (!certs.some((c) => c.jmcd === item.jmcd)) {
      setCerts([...certs, { jmcd: item.jmcd, jmfldnm: item.jmfldnm }]);
    }
    setCertSearchKey((k) => k + 1); // 검색칸 비우기
  };

  const removeCert = (jmcd) => setCerts(certs.filter((c) => c.jmcd !== jmcd));

  /* ---------- 제출 ---------- */
  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitAttempted(true);
    setSubmitMsg(null);

    let ok = true;
    if (!emailVerified) {
      ok = false;
      if (!isValidEmail(normalizeEmail(email))) setEmailMsg(error(MESSAGES.emailInvalid));
      else if (!emailSent) setEmailMsg(error(MESSAGES.emailNotVerified));
      else if (!codeExpired) setCodeMsg(error(MESSAGES.emailNotVerified));
    }
    if (!passwordValid || password !== passwordConfirm) ok = false;

    const nameError = validateName(name);
    setNameMsg(nameError);
    if (nameError) ok = false;

    if (!nickname.trim()) {
      setNicknameMsg(error(MESSAGES.nicknameRequired));
      ok = false;
    } else if (checkedNickname !== nickname.trim()) {
      if (!nicknameMsg || nicknameMsg.type !== 'error') setNicknameMsg(error(MESSAGES.nicknameNotChecked));
      ok = false;
    }

    const schoolError = validateSchool();
    setSchoolMsg(schoolError);
    if (schoolError) ok = false;

    if (!ok) {
      setSubmitMsg(error(MESSAGES.requiredMissing));
      return;
    }

    // "(학부 전체)" 항목은 id가 'college:학부ID' 형식 → 학부 ID로 보냄. 일반 전공은 전공 ID로 보냄
    const isFaculty = String(major?.id ?? '').startsWith('college:');
    const request = {
      email: normalizeEmail(email),
      password,
      name: name.trim(),
      nickname: nickname.trim(),
      university_id: noSchool ? null : Number(university.id),
      college_id: noSchool || !isFaculty ? null : Number(String(major.id).slice('college:'.length)),
      major_id: noSchool || isFaculty ? null : Number(major.id),
      grade: noSchool ? null : grade,
      owned_jmcds: certs.map((c) => c.jmcd),
    };

    setSubmitting(true);
    try {
      await register(request);
      setDone(true);
    } catch (err) {
      const message = serverMessage(err, '회원가입을 완료하지 못했습니다. 잠시 후 다시 시도해 주세요.');
      if (err.response?.status === 409 && message.includes('닉네임')) {
        // 그사이 다른 사람이 같은 닉네임으로 먼저 가입한 경우
        setCheckedNickname(null);
        setNicknameMsg(error(message));
      } else if (err.response?.status === 409 && message.includes('이메일')) {
        setEmailMsg(error(message));
      }
      setSubmitMsg(error(message));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="register-page">
      <h1 className="register__title">회원가입</h1>

      <form className="register__card" onSubmit={handleSubmit} noValidate>
        {/* 1. 계정 정보 */}
        <section className="register__section">
          <h2 className="register__section-title register__section-title--with-note">1. 계정 정보</h2>
          <p className="register__required-note">
            <span className="register__required">*</span> 는 필수 입력 항목입니다.
          </p>

          <div className="register__field">
            <Label htmlFor="register-email" required>
              아이디
            </Label>
            <div className="register__row">
              <div className="register__input-wrap">
                <input
                  id="register-email"
                  className="register__input"
                  type="email"
                  autoComplete="off"
                  placeholder="이메일 주소 입력"
                  value={email}
                  disabled={emailVerified}
                  role="combobox"
                  aria-expanded={showSuggestions}
                  aria-controls="register-email-suggestions"
                  onChange={(e) => {
                    handleEmailChange(e.target.value);
                    setEmailFocused(true);
                    setActiveSuggestion(-1);
                  }}
                  onFocus={() => setEmailFocused(true)}
                  onBlur={handleEmailBlur}
                  onKeyDown={handleEmailKeyDown}
                />
                {showSuggestions && (
                  <ul id="register-email-suggestions" className="register__suggest" role="listbox">
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
                className="register__button"
                disabled={!email || emailVerified || sending}
                onClick={handleSendCode}
              >
                {emailSent && !emailVerified ? '인증번호 재발송' : '인증번호 발송'}
              </button>
            </div>
            <FieldMessage message={emailMsg} />
          </div>

          <div className="register__field">
            <div className="register__row">
              <div className="register__input-wrap">
                <input
                  className="register__input"
                  inputMode="numeric"
                  maxLength={6}
                  placeholder="인증번호 6자리 입력"
                  aria-label="인증번호"
                  value={code}
                  disabled={!emailSent || emailVerified}
                  onChange={(e) => {
                    setCode(e.target.value.replace(/\D/g, ''));
                    setCodeMsg(null);
                  }}
                />
                {emailSent && !emailVerified && <span className="register__timer">{timerText}</span>}
              </div>
              <button
                type="button"
                className="register__button"
                disabled={!emailSent || emailVerified || verifying || codeExpired}
                onClick={handleVerifyCode}
              >
                인증확인
              </button>
            </div>
            <FieldMessage message={shownCodeMsg} />
            {emailSent && !emailVerified && <p className="register__hint">{MESSAGES.spamNotice}</p>}
          </div>

          <div className="register__field">
            <Label htmlFor="register-password" required>
              비밀번호
            </Label>
            <div className="register__input-wrap">
              <input
                id="register-password"
                className="register__input register__input--with-badge"
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
                  className={
                    passwordValid ? 'register__badge register__badge--ok' : 'register__badge register__badge--ng'
                  }
                  aria-hidden="true"
                >
                  {passwordValid ? 'V' : 'X'}
                </span>
              )}
            </div>
            {showPasswordError && <FieldMessage message={error(MESSAGES.passwordRule)} />}
          </div>

          <div className="register__field">
            <Label htmlFor="register-password-confirm" required>
              비밀번호 확인
            </Label>
            <input
              id="register-password-confirm"
              className="register__input register__input--full"
              type="password"
              autoComplete="new-password"
              maxLength={20}
              placeholder="비밀번호 재입력"
              value={passwordConfirm}
              onChange={(e) => setPasswordConfirm(e.target.value)}
            />
            <FieldMessage message={confirmMsg} />
          </div>
        </section>

        {/* 2. 기본 정보 */}
        <section className="register__section">
          <h2 className="register__section-title">2. 기본 정보</h2>

          <div className="register__field">
            <Label htmlFor="register-name" required>
              이름
            </Label>
            <input
              id="register-name"
              className="register__input register__input--full"
              autoComplete="name"
              maxLength={4}
              placeholder="이름 입력"
              value={name}
              onChange={(e) => {
                setName(e.target.value);
                if (nameMsg) setNameMsg(validateName(e.target.value));
              }}
              onBlur={() => setNameMsg(validateName(name))}
            />
            <FieldMessage message={nameMsg} />
          </div>

          <div className="register__field">
            <Label htmlFor="register-nickname" required>
              닉네임
            </Label>
            <div className="register__row">
              <input
                id="register-nickname"
                className="register__input"
                maxLength={10}
                placeholder="한글·영문·숫자 2~10자"
                value={nickname}
                onChange={(e) => handleNicknameChange(e.target.value)}
              />
              <button
                type="button"
                className="register__button"
                disabled={checkingNickname}
                onClick={handleCheckNickname}
              >
                중복 확인
              </button>
            </div>
            <FieldMessage message={nicknameMsg} />
          </div>
        </section>

        {/* 3. 학력 및 관심 정보 */}
        <section className="register__section">
          <h2 className="register__section-title">3. 학력 및 관심 정보</h2>

          <div className="register__field">
            <span className="register__label">대학교</span>
            <div className="register__search">
              <SearchSelect
                key={`univ-${noSchool}`}
                placeholder="대학교 검색"
                disabled={noSchool}
                fetchOptions={fetchUniversities}
                getLabel={(item) => (item.campus_name ? `${item.name} (${item.campus_name})` : item.name)}
                onSelect={handleSelectUniversity}
              />
              <SearchIcon />
            </div>
            <label className="register__checkbox">
              <input type="checkbox" checked={noSchool} onChange={(e) => handleNoSchool(e.target.checked)} />
              대학교 다닌 적 없음 (체크하면 대학교 및 전공 정보를 입력하지 않아도 됩니다.)
            </label>
          </div>

          <div className="register__field">
            <span className="register__label">전공</span>
            <div className="register__search">
              <SearchSelect
                key={`major-${university?.id ?? 'none'}-${noSchool}`}
                placeholder={university ? '전공 검색' : '대학교를 먼저 선택해 주세요'}
                disabled={noSchool || !university}
                fetchOptions={fetchMajors}
                getLabel={(item) => item.label ?? item.name}
                onSelect={(item) => {
                  setMajor(item);
                  setSchoolMsg(null);
                }}
              />
              <SearchIcon />
            </div>
          </div>

          <div className="register__field">
            <label className="register__label" htmlFor="register-grade">
              학년
            </label>
            <div className="register__search">
              <select
                id="register-grade"
                className="register__input register__input--full register__select"
                value={grade}
                disabled={noSchool}
                onChange={(e) => {
                  setGrade(e.target.value);
                  setSchoolMsg(null);
                }}
              >
                <option value="">학년 선택</option>
                {GRADES.map((g) => (
                  <option key={g.value} value={g.value}>
                    {g.label}
                  </option>
                ))}
              </select>
              <ChevronIcon />
            </div>
            <FieldMessage message={schoolMsg} />
          </div>

          <div className="register__field">
            <span className="register__label">보유 자격증</span>
            <p className="register__hint">현재 보유하고 있는 자격증을 선택해 주세요.</p>
            {certs.length > 0 && (
              <ul className="register__chips">
                {certs.map((c) => (
                  <li key={c.jmcd} className="register__chip">
                    {c.jmfldnm}
                    <button type="button" aria-label={`${c.jmfldnm} 삭제`} onClick={() => removeCert(c.jmcd)}>
                      ×
                    </button>
                  </li>
                ))}
              </ul>
            )}
            <div className="register__search">
              <SearchSelect
                key={`cert-${certSearchKey}`}
                placeholder="자격증 검색"
                fetchOptions={fetchQualifications}
                getLabel={(item) => item.jmfldnm}
                onSelect={handleSelectCert}
              />
              <ChevronIcon />
            </div>
          </div>
        </section>

        <div className="register__submit">
          <button type="submit" className="register__primary" disabled={submitting}>
            {submitting ? '가입 처리 중…' : '회원가입 완료'}
          </button>
          <FieldMessage message={submitMsg} />
        </div>
      </form>

      {done && (
        <AlertModal
          message="회원가입이 완료되었습니다."
          buttonText="로그인하러 가기"
          onClose={() => navigate('/login')}
        />
      )}
    </div>
  );
}
