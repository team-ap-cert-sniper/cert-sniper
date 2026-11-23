import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { cancelWithdrawal, getMyProfile, requestWithdrawal } from '@/api/userApi';
import AlertModal from '@/components/common/AlertModal';
import ConfirmModal from '@/components/common/ConfirmModal';
import FieldError from '@/components/common/FieldError';
import Loading from '@/components/common/Loading';
import { errorMessage } from '@/components/passstory/utils';
import { authStore } from '@/store/authStore';
import { formatDate } from '@/utils/date';

const GRACE_DAYS = 7;

export default function WithdrawTab() {
  const navigate = useNavigate();
  const [profile, setProfile] = useState(null);
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [modal, setModal] = useState(null); // 'confirm' | 'requested' | 'canceled'

  const load = useCallback(
    () =>
      getMyProfile()
        .then(setProfile)
        .catch((e) => setError(errorMessage(e))),
    [],
  );

  useEffect(() => {
    load();
  }, [load]);

  const withdraw = async () => {
    setModal(null);
    try {
      await requestWithdrawal({ password });
      setModal('requested');
    } catch (e) {
      setError(errorMessage(e, { DUPLICATE: '이미 탈퇴 신청한 계정입니다.' }));
    }
  };

  const cancel = async () => {
    try {
      await cancelWithdrawal();
      setModal('canceled');
      load();
    } catch (e) {
      setError(errorMessage(e));
    }
  };

  // 탈퇴 신청 시 서버가 모든 토큰을 폐기하므로 이 기기도 로그아웃 후 메인으로
  const leave = () => {
    authStore.logout();
    navigate('/');
  };

  if (!profile && !error) return <Loading />;

  if (profile?.status === 'WITHDRAWAL_PENDING') {
    const deadline = new Date(profile.withdrawal_requested_at);
    deadline.setDate(deadline.getDate() + GRACE_DAYS);
    return (
      <section>
        <h2>회원 탈퇴</h2>
        <p>
          {formatDate(profile.withdrawal_requested_at)}에 탈퇴를 신청했습니다. {formatDate(deadline)} 이후 계정이
          삭제됩니다.
        </p>
        <button type="button" className="btn btn--primary" onClick={cancel}>
          탈퇴 철회
        </button>
        <FieldError message={error} />
      </section>
    );
  }

  return (
    <section>
      <h2>회원 탈퇴</h2>
      <ul>
        <li>탈퇴를 신청하면 {GRACE_DAYS}일 뒤 계정과 개인정보가 삭제되며, 그 전에는 로그인해 철회할 수 있습니다.</li>
        <li>찜한 자격증, 플래너 일정, 메모는 삭제되어 복구할 수 없습니다.</li>
        <li>작성한 합격수기와 문의는 삭제되지 않고 남습니다.</li>
      </ul>
      <form
        onSubmit={(e) => {
          e.preventDefault();
          setError('');
          setModal('confirm');
        }}
      >
        <label htmlFor="withdraw-password">현재 비밀번호</label>
        <input
          id="withdraw-password"
          type="password"
          autoComplete="current-password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <button type="submit" className="btn" disabled={!password}>
          탈퇴 신청
        </button>
      </form>
      <FieldError message={error} />

      {modal === 'confirm' && (
        <ConfirmModal
          message="정말 탈퇴하시겠습니까?"
          confirmText="탈퇴"
          onConfirm={withdraw}
          onCancel={() => setModal(null)}
        />
      )}
      {modal === 'requested' && (
        <AlertModal
          message={`탈퇴 신청이 완료되었습니다. ${GRACE_DAYS}일 안에 다시 로그인하면 철회할 수 있습니다.`}
          onClose={leave}
        />
      )}
      {modal === 'canceled' && <AlertModal message="탈퇴가 철회되었습니다." onClose={() => setModal(null)} />}
    </section>
  );
}
