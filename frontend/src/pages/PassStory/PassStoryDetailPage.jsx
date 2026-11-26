import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { deletePassStory, getPassStory } from '@/api/passStoryApi';
import { reportPassStory } from '@/api/reportApi';
import AlertModal from '@/components/common/AlertModal';
import Loading from '@/components/common/Loading';
import HiddenNotice from '@/components/passstory/HiddenNotice';
import RankBadge from '@/components/passstory/RankBadge';
import { ATTEMPT_LABELS, EXAMINEE_BASE_LABELS, PREP_PERIOD_LABELS } from '@/components/passstory/labels';
import { errorMessage } from '@/components/passstory/utils';
import DeleteModal from '@/pages/PassStory/components/DeleteModal';
import ReportModal from '@/pages/PassStory/components/ReportModal';
import { formatDate } from '@/utils/date';
import '@/styles/passstory.css';

const REPORT_ERRORS = {
  DUPLICATE: '이미 신고한 글입니다.',
  FORBIDDEN: '본인 글은 신고할 수 없습니다.',
};

// 선택 입력 항목: 값이 없으면 줄을 그리지 않음
function Info({ label, value }) {
  if (!value) return null;
  return (
    <div>
      <dt>{label}</dt>
      <dd>{value}</dd>
    </div>
  );
}

export default function PassStoryDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [story, setStory] = useState(null);
  const [error, setError] = useState('');
  const [modal, setModal] = useState(null); // 'delete' | 'report' | { alert: string }

  const load = useCallback(() => {
    return getPassStory(id)
      .then((data) => {
        setStory(data);
        setError('');
      })
      .catch((e) => setError(errorMessage(e, { NOT_FOUND: '삭제되었거나 없는 글입니다.' })));
  }, [id]);

  useEffect(() => {
    load();
  }, [load]);

  const remove = () =>
    deletePassStory(id)
      .then(() => navigate('/pass-stories'))
      .catch((e) => setModal({ alert: errorMessage(e) }));

  const report = (reportType) =>
    reportPassStory(id, { report_type: reportType })
      .then(() => {
        setModal({ alert: '신고가 접수되었습니다. 글은 관리자 검토 전까지 숨김 처리됩니다.' });
        load();
      })
      .catch((e) => setModal({ alert: errorMessage(e, REPORT_ERRORS) }));

  if (error) {
    return (
      <section className="ps">
        <div className="ps-detail ps-box">
          <p className="field-error">{error}</p>
          <Link to="/pass-stories" className="btn">
            ← 목록으로
          </Link>
        </div>
      </section>
    );
  }
  if (!story) return <Loading />;

  return (
    <section className="ps">
      <div className="ps-detail">
        <article className="ps-detail__card ps-box">
          <h1 className="ps-detail__cert">{story.jmfldnm}</h1>
          <p className="ps-detail__meta">
            {story.nickname} <RankBadge rank={story.current_rank} /> | {formatDate(story.created_at)}
          </p>

          <HiddenNotice status={story.status} hiddenReason={story.hidden_reason} />

          <div className="ps-detail__panel">
            <dl>
              <Info label="응시자 베이스" value={EXAMINEE_BASE_LABELS[story.examinee_base]} />
              <Info label="시험 준비 기간" value={PREP_PERIOD_LABELS[story.prep_period]} />
              <Info label="시험 응시 횟수" value={ATTEMPT_LABELS[story.attempt_band]} />
              <Info label="사용한 교재 / 학습 수단" value={story.textbook_name} />
              <Info label="합격 점수" value={story.pass_score != null && `${story.pass_score}점`} />
              <Info label="시험 응시일" value={story.exam_date && formatDate(story.exam_date)} />
            </dl>

            {/* body는 서버에서 허용 태그만 남긴 HTML (HtmlSanitizer). 타인의 숨김 글이면 null */}
            {story.body != null && (
              <>
                <h2>생생한 합격 후기</h2>
                <div className="pass-story-body" dangerouslySetInnerHTML={{ __html: story.body }} />
              </>
            )}
          </div>

          <div className="ps-detail__actions">
            {story.mine && (
              <>
                <Link to={`/pass-stories/${id}/edit`} className="btn">
                  ✎ 수정하기
                </Link>
                <button type="button" className="btn btn--danger" onClick={() => setModal('delete')}>
                  삭제
                </button>
              </>
            )}
            {!story.mine && story.status === 'PUBLISHED' && (
              <button type="button" className="btn btn--danger" onClick={() => setModal('report')}>
                신고하기
              </button>
            )}
            <Link to="/pass-stories" className="btn btn--primary">
              ← 목록으로
            </Link>
          </div>
        </article>
      </div>

      {modal === 'delete' && <DeleteModal onConfirm={remove} onCancel={() => setModal(null)} />}
      {modal === 'report' && <ReportModal onSubmit={report} onCancel={() => setModal(null)} />}
      {modal?.alert && <AlertModal message={modal.alert} onClose={() => setModal(null)} />}
    </section>
  );
}
