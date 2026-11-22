import { createBrowserRouter, Navigate } from 'react-router-dom';
import App from '@/App';
import AdminRoute from '@/router/AdminRoute';
import ProtectedRoute from '@/router/ProtectedRoute';
import AdminDashboardPage from '@/pages/Admin/AdminDashboardPage';
import AdminInquiryPage from '@/pages/Admin/AdminInquiryPage';
import AdminNoticePage from '@/pages/Admin/AdminNoticePage';
import AdminPassStoryPage from '@/pages/Admin/AdminPassStoryPage';
import AdminReportPage from '@/pages/Admin/AdminReportPage';
import AdminUserPage from '@/pages/Admin/AdminUserPage';
import AiPlannerPage from '@/pages/AiPlanner/AiPlannerPage';
import LoginPage from '@/pages/Auth/LoginPage';
import PasswordResetPage from '@/pages/Auth/PasswordResetPage';
import RegisterPage from '@/pages/Auth/RegisterPage';
import MainPage from '@/pages/Main/MainPage';
import MyPage from '@/pages/MyPage/MyPage';
import PassStoryDetailPage from '@/pages/PassStory/PassStoryDetailPage';
import PassStoryFormPage from '@/pages/PassStory/PassStoryFormPage';
import PassStoryListPage from '@/pages/PassStory/PassStoryListPage';
import PlannerPage from '@/pages/Planner/PlannerPage';
import QualificationDetailPage from '@/pages/QualificationDetail/QualificationDetailPage';
import RecommendPage from '@/pages/Recommend/RecommendPage';
import SupportPage from '@/pages/Support/SupportPage';

const router = createBrowserRouter([
  {
    element: <App />,
    children: [
      { path: '/', element: <MainPage /> },
      { path: '/register', element: <RegisterPage /> },
      { path: '/login', element: <LoginPage /> },
      { path: '/password-reset', element: <PasswordResetPage /> },
      {
        element: <ProtectedRoute />,
        children: [
          { path: '/recommend', element: <RecommendPage /> },
          { path: '/qualifications', element: <QualificationDetailPage /> },
          { path: '/qualifications/:jmcd', element: <QualificationDetailPage /> },
          { path: '/planner', element: <PlannerPage /> },
          { path: '/ai-planner', element: <AiPlannerPage /> },
          { path: '/pass-stories', element: <PassStoryListPage /> },
          { path: '/pass-stories/new', element: <PassStoryFormPage /> },
          { path: '/pass-stories/:id', element: <PassStoryDetailPage /> },
          { path: '/pass-stories/:id/edit', element: <PassStoryFormPage /> },
          { path: '/mypage', element: <MyPage /> },
          { path: '/support', element: <SupportPage /> },
        ],
      },
    ],
  },
  {
    path: '/admin',
    element: <AdminRoute />,
    children: [
      { index: true, element: <AdminDashboardPage /> },
      { path: 'users', element: <AdminUserPage /> },
      { path: 'notices', element: <AdminNoticePage /> },
      { path: 'inquiries', element: <AdminInquiryPage /> },
      { path: 'reports', element: <AdminReportPage /> },
      { path: 'pass-stories', element: <AdminPassStoryPage /> },
    ],
  },
  { path: '*', element: <Navigate to="/" replace /> },
]);

export default router;
