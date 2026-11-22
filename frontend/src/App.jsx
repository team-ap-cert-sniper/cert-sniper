import { Outlet, useNavigate } from 'react-router-dom';
import Footer from '@/components/common/Footer';
import Header from '@/components/common/Header';
import LoginRequiredModal from '@/components/common/LoginRequiredModal';
import useAuth from '@/hooks/useAuth';
import { authStore } from '@/store/authStore';

export default function App() {
  const navigate = useNavigate();
  const { loginRequired } = useAuth();

  const goLogin = () => {
    authStore.closeLoginRequired();
    navigate('/login');
  };

  return (
    <>
      <Header />
      <main className="container">
        <Outlet />
      </main>
      <Footer />
      {loginRequired && <LoginRequiredModal onConfirm={goLogin} onCancel={authStore.closeLoginRequired} />}
    </>
  );
}
