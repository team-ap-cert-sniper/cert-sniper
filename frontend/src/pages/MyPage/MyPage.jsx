import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { getMyProfile } from '@/api/userApi';
import ProfileTab from '@/pages/MyPage/components/ProfileTab';
import PasswordTab from '@/pages/MyPage/components/PasswordTab';
import FavoriteTab from '@/pages/MyPage/components/FavoriteTab';
import MyPostTab from '@/pages/MyPage/components/MyPostTab';
import MyReportTab from '@/pages/MyPage/components/MyReportTab';
import WithdrawTab from '@/pages/MyPage/components/WithdrawTab';
import '@/styles/passstory.css';
import '@/styles/mypage.css';

const GROUPS = [
  {
    label: '회원정보 관리',
    tabs: [
      { key: 'profile', label: '회원정보 조회/수정', Component: ProfileTab },
      { key: 'password', label: '비밀번호 관리', Component: PasswordTab },
    ],
  },
  { label: '자격증 관리', tabs: [{ key: 'favorites', label: '관심 자격증', Component: FavoriteTab }] },
  {
    label: '활동 관리',
    tabs: [
      { key: 'posts', label: '내 작성글', Component: MyPostTab },
      { key: 'reports', label: '신고내역', Component: MyReportTab },
    ],
  },
  { label: '계정 관리', tabs: [{ key: 'withdraw', label: '회원 탈퇴', Component: WithdrawTab }] },
];
const TABS = GROUPS.flatMap((g) => g.tabs.map((t) => ({ ...t, group: g.label })));

export default function MyPage() {
  const [params, setParams] = useSearchParams();
  const current = TABS.find((t) => t.key === params.get('tab')) ?? TABS[0];
  const { Component } = current;
  const [me, setMe] = useState(null);

  // 사이드바 표시용. 실패하면 이름 영역만 비움 (각 탭이 오류를 따로 보여 줌)
  useEffect(() => {
    getMyProfile()
      .then(setMe)
      .catch(() => {});
  }, []);

  return (
    <section className="mypage">
      <h1 className="sr-only">마이페이지</h1>
      <aside className="mypage__side">
        {me && (
          <div className="mypage__me">
            <span className="mypage__avatar" aria-hidden="true">
              {me.nickname.slice(0, 1)}
            </span>
            <div>
              <strong>{me.nickname}</strong>
              <span>{me.email}</span>
            </div>
          </div>
        )}
        <nav aria-label="마이페이지 메뉴">
          {GROUPS.map((g) => (
            <div key={g.label} className="mypage__group">
              <p>{g.label}</p>
              {g.tabs.map((t) => (
                <button
                  key={t.key}
                  type="button"
                  aria-current={t.key === current.key ? 'page' : undefined}
                  onClick={() => setParams({ tab: t.key })}
                >
                  {t.label}
                </button>
              ))}
            </div>
          ))}
        </nav>
      </aside>

      <div className="mypage__content">
        <p className="mypage__crumb">
          마이페이지 › {current.group} › <strong>{current.label}</strong>
        </p>
        <Component />
      </div>
    </section>
  );
}
