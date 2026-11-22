import { useNavigate } from 'react-router-dom';
import useAuth from '@/hooks/useAuth';
import { authStore } from '@/store/authStore';

export default function Footer() {
  const navigate = useNavigate();
  const { isLoggedIn } = useAuth();

  const go = (path) => {
    if (!isLoggedIn) {
      authStore.openLoginRequired();
      return;
    }
    navigate(path);
  };

  return (
    <footer className="footer">
      <div className="footer__inner">
        <span>© TEAM AP 저격증</span>
        <div>
          <button type="button" className="btn--link" onClick={() => go('/support')}>
            고객센터 바로가기
          </button>
          {' · '}
          <button type="button" className="btn--link" onClick={() => go('/support?menu=inquiry')}>
            1:1 문의하기
          </button>
        </div>
      </div>
    </footer>
  );
}
