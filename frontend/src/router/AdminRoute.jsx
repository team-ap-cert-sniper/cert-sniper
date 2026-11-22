import { useNavigate } from 'react-router-dom';
import AlertModal from '@/components/common/AlertModal';
import LoginRequiredModal from '@/components/common/LoginRequiredModal';
import useAuth from '@/hooks/useAuth';
import AdminLayout from '@/pages/Admin/AdminLayout';

export default function AdminRoute() {
  const navigate = useNavigate();
  const { isLoggedIn, isAdmin } = useAuth();

  if (!isLoggedIn) {
    return (
      <LoginRequiredModal
        onConfirm={() => navigate('/login', { replace: true })}
        onCancel={() => navigate('/', { replace: true })}
      />
    );
  }
  if (!isAdmin) {
    return <AlertModal message="접근 권한이 없습니다." onClose={() => navigate('/', { replace: true })} />;
  }
  return <AdminLayout />;
}
