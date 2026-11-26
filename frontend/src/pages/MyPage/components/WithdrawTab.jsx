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
  const [agreed, setAgreed] = useState(false);
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
        <h2 className="mypage-title">회원 탈퇴</h2>
        <div className="mypage-card">
          <div className="mypage-notice">
            <strong>탈퇴 대기 중입니다</strong>
            <p>
              {formatDate(profile.withdrawal_requested_at)}에 탈퇴를 신청했습니다. {formatDate(deadline)} 이후 계정이
              삭제됩니다.
            </p>
          </div>
          <div className="mypage-actions mypage-actions--end">
            <button type="button" className="btn btn--primary" onClick={cancel}>
              탈퇴 철회
            </button>
          </div>
          <FieldError message={error} />
        </div>
      </section>
    );
  }

  return (
    <section>
      <h2 className="mypage-title">회원 탈퇴</h2>
      <div className="mypage-card">
        <div className="mypage-notice mypage-notice--warn">
          <strong>⚠ 회원 탈퇴 전 꼭 확인해 주세요!</strong>
          <p>아래 내용을 모두 확인한 뒤 진행해 주세요. 삭제된 데이터는 복구할 수 없습니다.</p>
        </div>
        <ol className="withdraw-steps">
          <li>
            <strong>계정·개인정보 삭제</strong>
            <span>신청 {GRACE_DAYS}일 뒤 계정과 개인정보가 삭제됩니다. 그 전에는 로그인해 철회할 수 있습니다.</span>
          </li>
          <li>
            <strong>찜·플래너·메모 삭제</strong>
            <span>찜한 자격증, 플래너 일정, 메모는 삭제되어 복구할 수 없습니다.</span>
          </li>
          <li>
            <strong>작성 글은 유지</strong>
            <span>작성한 합격수기와 문의는 삭제되지 않고 남습니다.</span>
          </li>
        </ol>
        <label className="mypage-check mypage-check--box">
          <input type="checkbox" checked={agreed} onChange={(e) => setAgreed(e.target.checked)} />
          [필수] 유의사항을 모두 확인하였으며, 데이터 삭제에 동의합니다.
        </label>
      </div>

      <form
        onSubmit={(e) => {
          e.preventDefault();
          setError('');
          setModal('confirm');
        }}
      >
        <div className="mypage-card">
          <p className="mypage-section">본인 확인</p>
          <p className="mypage-field mypage-field--half">
            <label htmlFor="withdraw-password" className="mypage-label">
              현재 비밀번호 입력
            </label>
            <input
              id="withdraw-password"
              type="password"
              autoComplete="current-password"
              placeholder="현재 사용 중인 비밀번호를 입력해 주세요"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </p>
          <FieldError message={error} />
        </div>
        <div className="mypage-actions mypage-actions--end">
          <button type="submit" className="btn btn--dark" disabled={!password || !agreed}>
            회원 탈퇴
          </button>
        </div>
      </form>

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
