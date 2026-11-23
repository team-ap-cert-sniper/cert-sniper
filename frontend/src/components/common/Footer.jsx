import { useNavigate } from 'react-router-dom';
import useAuth from '@/hooks/useAuth';
import { authStore } from '@/store/authStore';

export default function Footer() {
  const navigate = useNavigate();
  const { isLoggedIn } = useAuth();

  // 고객센터는 회원 전용: 비회원이면 로그인 필요 팝업 (CS-01)
  const go = (path) => {
    if (!isLoggedIn) {
      authStore.openLoginRequired();
      return;
    }
    navigate(path);
  };

  return (
    <footer className="footer">
      <div className="footer__top">
        <button type="button" className="footer__top-link" onClick={() => go('/support?menu=inquiry')}>
          1:1 문의하기
        </button>
        <span className="footer__divider" aria-hidden="true">
          |
        </span>
        <button type="button" className="footer__top-link" onClick={() => go('/support')}>
          고객센터
        </button>
      </div>

      <div className="footer__body">
        <div className="footer__brand">
          {/* TODO 로고 이미지 받으면 frontend/public/images/logo-footer.png 로 넣기 */}
          <img src="/images/logo-footer.png" alt="" className="footer__logo" />
          <span className="footer__wordmark">저격증</span>
        </div>
        <div className="footer__info">
          <p>(주) APcompany</p>
          <p>
            문의 제보: <a href="mailto:ap2026@gmail.com">ap2026@gmail.com</a>
          </p>
        </div>
      </div>
    </footer>
  );
}