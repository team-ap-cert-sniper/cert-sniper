import { useState } from 'react';
import { Link, Outlet, useNavigate } from 'react-router-dom';
import ConfirmModal from '@/components/common/ConfirmModal';
import { logout } from '@/api/authApi';
import { authStore } from '@/store/authStore';

const MENUS = [
  { to: '/admin', label: '대시보드' },
  { to: '/admin/users', label: '회원 관리' },
  { to: '/admin/notices', label: '공지사항 관리' },
  { to: '/admin/inquiries', label: '1:1 문의 관리' },
  { to: '/admin/reports', label: '신고 관리' },
  { to: '/admin/pass-stories', label: '저격왕 전체 글' },
];

export default function AdminLayout() {
  const navigate = useNavigate();
  const [confirmLogout, setConfirmLogout] = useState(false);

  const handleLogout = async () => {
    const { refreshToken } = authStore.getState();
    try {
      if (refreshToken) await logout({ refresh_token: refreshToken });
    } finally {
      authStore.logout();
      navigate('/');
    }
  };

  return (
    <div className="admin">
      <aside className="admin__menu">
        {MENUS.map((menu) => (
          <Link key={menu.to} to={menu.to}>
            {menu.label}
          </Link>
        ))}
        <Link to="/">사이트로 이동</Link>
        <button type="button" className="btn--link" onClick={() => setConfirmLogout(true)}>
          로그아웃
        </button>
      </aside>
      <main className="admin__content">
        <Outlet />
      </main>
      {confirmLogout && (
        <ConfirmModal
          message="로그아웃 하시겠습니까?"
          onConfirm={handleLogout}
          onCancel={() => setConfirmLogout(false)}
        />
      )}
    </div>
  );
}
