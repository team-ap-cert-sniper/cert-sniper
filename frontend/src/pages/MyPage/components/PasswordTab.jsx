import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { changePassword } from '@/api/authApi';
import { getMyProfile } from '@/api/userApi';
import AlertModal from '@/components/common/AlertModal';
import EmailVerifyField from '@/components/common/EmailVerifyField';
import FieldError from '@/components/common/FieldError';
import Loading from '@/components/common/Loading';
import { errorMessage } from '@/components/passstory/utils';
import { authStore } from '@/store/authStore';

export default function PasswordTab() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [verified, setVerified] = useState(false);
  const [form, setForm] = useState({ current: '', next: '', confirm: '' });
  const [error, setError] = useState('');
  const [done, setDone] = useState(false);

  useEffect(() => {
    getMyProfile()
      .then((p) => setEmail(p.email))
      .catch((e) => setError(errorMessage(e)));
  }, []);

  const set = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }));

  const submit = async (e) => {
    e.preventDefault();
    if (form.next !== form.confirm) {
      setError('새 비밀번호와 확인이 일치하지 않습니다.');
      return;
    }
    try {
      await changePassword({ current_password: form.current, new_password: form.next });
      setDone(true);
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  // 비밀번호 변경 시 서버가 모든 토큰을 폐기하므로 이 기기도 로그아웃 후 로그인 화면으로
  const finish = () => {
    authStore.logout();
    navigate('/login');
  };

  if (!email && !error) return <Loading />;

  return (
    <section>
      <h2>비밀번호 관리</h2>
      <p>가입한 이메일로 인증한 뒤 비밀번호를 바꿀 수 있습니다. 변경하면 모든 기기에서 로그아웃됩니다.</p>
      {/* 인증 이메일은 가입 이메일로 고정 (변경 입력은 무시) */}
      <EmailVerifyField
        purpose="PASSWORD_CHANGE"
        email={email}
        onEmailChange={() => {}}
        onVerified={() => setVerified(true)}
      />

      {verified && (
        <form onSubmit={submit}>
          <p>
            <label htmlFor="pw-current">현재 비밀번호</label>
            <input
              id="pw-current"
              type="password"
              autoComplete="current-password"
              value={form.current}
              onChange={set('current')}
              required
            />
          </p>
          <p>
            <label htmlFor="pw-next">새 비밀번호</label>
            <input
              id="pw-next"
              type="password"
              autoComplete="new-password"
              value={form.next}
              onChange={set('next')}
              required
            />
          </p>
          <p>
            <label htmlFor="pw-confirm">새 비밀번호 확인</label>
            <input
              id="pw-confirm"
              type="password"
              autoComplete="new-password"
              value={form.confirm}
              onChange={set('confirm')}
              required
            />
          </p>
          <button type="submit" className="btn btn--primary">
            변경
          </button>
        </form>
      )}
      <FieldError message={error} />

      {done && (
        <AlertModal message="비밀번호가 변경되었습니다. 다시 로그인해 주세요." buttonText="로그인" onClose={finish} />
      )}
    </section>
  );
}
