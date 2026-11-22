import ProfileTab from '@/pages/MyPage/components/ProfileTab';
import PasswordTab from '@/pages/MyPage/components/PasswordTab';
import FavoriteTab from '@/pages/MyPage/components/FavoriteTab';
import MyPostTab from '@/pages/MyPage/components/MyPostTab';
import MyReportTab from '@/pages/MyPage/components/MyReportTab';
import WithdrawTab from '@/pages/MyPage/components/WithdrawTab';

export default function MyPage() {
  return (
    <section>
      <h1>마이페이지</h1>
      <ProfileTab />
      <PasswordTab />
      <FavoriteTab />
      <MyPostTab />
      <MyReportTab />
      <WithdrawTab />
    </section>
  );
}
