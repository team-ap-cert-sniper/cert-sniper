import iconChevron from '@/pages/Support/assets/breadcrumb-chevron.svg';
import iconHome from '@/pages/Support/assets/breadcrumb-home.svg';

// 고객센터 화면 상단 경로 표시. 마지막 항목이 현재 위치
export function Breadcrumb({ items, home = false, slash = false }) {
  return (
    <nav className={slash ? 'support-crumb support-crumb--slash' : 'support-crumb'} aria-label="현재 위치">
      {home && <img src={iconHome} alt="" />}
      {items.map((item, i) => {
        const last = i === items.length - 1;
        return (
          <span key={item} className="support-crumb">
            {i > 0 && (slash ? <span className="support-crumb__sep">/</span> : <img src={iconChevron} alt="" />)}
            <span className={last ? 'support-crumb__current' : undefined} aria-current={last ? 'page' : undefined}>
              {item}
            </span>
          </span>
        );
      })}
    </nav>
  );
}

// 목록 화면 제목 영역: 경로, 제목(+ 보조 배지), 설명, 오른쪽 버튼
export function PageHead({ crumbs, title, pill, desc, large = false, action }) {
  return (
    <header className="support-head">
      <div className="support-head__text">
        <Breadcrumb items={crumbs} />
        <h2 className={large ? 'support-head__title support-head__title--lg' : 'support-head__title'}>
          {title}
          {pill && <span className="support-pill support-pill--pink">{pill}</span>}
        </h2>
        {desc && <p className="support-head__desc">{desc}</p>}
      </div>
      {action}
    </header>
  );
}
