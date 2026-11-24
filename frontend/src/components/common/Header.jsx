import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import ConfirmModal from '@/components/common/ConfirmModal';
import { logout } from '@/api/authApi';
import useAuth from '@/hooks/useAuth';
import useHideOnScroll from '@/hooks/useHideOnScroll';
import { authStore } from '@/store/authStore';

const MENUS = [
  { to: '/qualifications', label: '자격증 상세' },
  { to: '/recommend', label: '자격증 추천' },
  { to: '/planner', label: '플래너 다이어리' },
  { to: '/pass-stories', label: '저격왕' },
];

export default function Header() {
  const navigate = useNavigate();
  const { isLoggedIn, isAdmin } = useAuth();
  const [confirmLogout, setConfirmLogout] = useState(false);
  const hidden = useHideOnScroll();

  const handleLogout = async () => {
    const { refreshToken } = authStore.getState();
    try {
      if (refreshToken) await logout({ refresh_token: refreshToken });
    } finally {
      authStore.logout();
      setConfirmLogout(false);
      navigate('/');
    }
  };

  return (
    <header className={hidden ? 'header header--hidden' : 'header'}>
      <div className="header__inner">
        <Link to="/" className="header__logo">
          저격증
        </Link>
        <nav className="header__menu">
          {MENUS.map((menu) => (
            <Link key={menu.to} to={menu.to}>
              {menu.label}
            </Link>
          ))}
        </nav>
        <div className="header__user">
          {isLoggedIn ? (
            <>
              <Link to="/mypage">마이페이지</Link>
              {isAdmin && <Link to="/admin">관리자 페이지</Link>}
              <button type="button" className="btn--link" onClick={() => setConfirmLogout(true)}>
                로그아웃
              </button>
            </>
          ) : (
            <>
              <Link to="/login">로그인</Link>
              <Link to="/register">회원가입</Link>
            </>
          )}
        </div>
      </div>
      {confirmLogout && (
        <ConfirmModal
          message="로그아웃 하시겠습니까?"
          onConfirm={handleLogout}
          onCancel={() => setConfirmLogout(false)}
        />
      )}
    </header>
  );
}
