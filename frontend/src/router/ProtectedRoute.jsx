import { Outlet, useNavigate } from 'react-router-dom';
import LoginRequiredModal from '@/components/common/LoginRequiredModal';
import useAuth from '@/hooks/useAuth';

export default function ProtectedRoute() {
  const navigate = useNavigate();
  const { isLoggedIn } = useAuth();

  if (!isLoggedIn) {
    return (
      <LoginRequiredModal
        onConfirm={() => navigate('/login', { replace: true })}
        onCancel={() => (window.history.state?.idx > 0 ? navigate(-1) : navigate('/', { replace: true }))}
      />
    );
  }
  return <Outlet />;
}
