import { useEffect, useState } from 'react';
import FieldError from '@/components/common/FieldError';
import { sendEmailCode, verifyEmailCode } from '@/api/authApi';

const LIMIT_SECONDS = 180;

export default function EmailVerifyField({ purpose, email, onEmailChange, onVerified }) {
  const [code, setCode] = useState('');
  const [remaining, setRemaining] = useState(0);
  const [verified, setVerified] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (remaining <= 0) return;
    const timer = setTimeout(() => setRemaining(remaining - 1), 1000);
    return () => clearTimeout(timer);
  }, [remaining]);

  const send = async () => {
    setError('');
    try {
      await sendEmailCode({ email, purpose });
      setVerified(false);
      setRemaining(LIMIT_SECONDS);
    } catch (e) {
      setError(e.response?.data?.message ?? '인증코드를 보내지 못했습니다.');
    }
  };

  const verify = async () => {
    setError('');
    try {
      await verifyEmailCode({ email, purpose, code });
      setVerified(true);
      setRemaining(0);
      onVerified?.();
    } catch (e) {
      setError(e.response?.data?.message ?? '인증코드를 확인해 주세요.');
    }
  };

  const mm = String(Math.floor(remaining / 60)).padStart(2, '0');
  const ss = String(remaining % 60).padStart(2, '0');

  return (
    <div>
      <input type="email" value={email} disabled={verified} onChange={(e) => onEmailChange(e.target.value)} />
      <button type="button" className="btn" disabled={!email || verified} onClick={send}>인증코드 받기</button>
      {remaining > 0 && (
        <>
          <input value={code} maxLength={6} onChange={(e) => setCode(e.target.value)} />
          <span>{mm}:{ss}</span>
          <button type="button" className="btn" disabled={code.length !== 6} onClick={verify}>확인</button>
        </>
      )}
      {verified && <p>인증되었습니다.</p>}
      <FieldError message={error} />
    </div>
  );
}
