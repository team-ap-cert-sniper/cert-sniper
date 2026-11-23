import { useSearchParams } from 'react-router-dom';
import ProfileTab from '@/pages/MyPage/components/ProfileTab';
import PasswordTab from '@/pages/MyPage/components/PasswordTab';
import FavoriteTab from '@/pages/MyPage/components/FavoriteTab';
import MyPostTab from '@/pages/MyPage/components/MyPostTab';
import MyReportTab from '@/pages/MyPage/components/MyReportTab';
import WithdrawTab from '@/pages/MyPage/components/WithdrawTab';

const TABS = [
  { key: 'profile', label: '회원정보', Component: ProfileTab },
  { key: 'password', label: '비밀번호 관리', Component: PasswordTab },
  { key: 'favorites', label: '관심 자격증', Component: FavoriteTab },
  { key: 'posts', label: '내 작성글', Component: MyPostTab },
  { key: 'reports', label: '신고내역', Component: MyReportTab },
  { key: 'withdraw', label: '회원 탈퇴', Component: WithdrawTab },
];

export default function MyPage() {
  const [params, setParams] = useSearchParams();
  const current = TABS.find((t) => t.key === params.get('tab')) ?? TABS[0];
  const { Component } = current;

  return (
    <section>
      <h1>마이페이지</h1>
      <nav aria-label="마이페이지 메뉴">
        {TABS.map((t) => (
          <button
            key={t.key}
            type="button"
            className="btn"
            aria-current={t.key === current.key ? 'page' : undefined}
            onClick={() => setParams({ tab: t.key })}
          >
            {t.label}
          </button>
        ))}
      </nav>
      <Component />
    </section>
  );
}
