import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { deletePassStory, getPassStory } from '@/api/passStoryApi';
import { reportPassStory } from '@/api/reportApi';
import AlertModal from '@/components/common/AlertModal';
import ConfirmModal from '@/components/common/ConfirmModal';
import Loading from '@/components/common/Loading';
import HiddenNotice from '@/components/passstory/HiddenNotice';
import RankBadge from '@/components/passstory/RankBadge';
import { ATTEMPT_LABELS, EXAMINEE_BASE_LABELS, PREP_PERIOD_LABELS } from '@/components/passstory/labels';
import { errorMessage } from '@/components/passstory/utils';
import ReportModal from '@/pages/PassStory/components/ReportModal';
import { formatDate } from '@/utils/date';

const REPORT_ERRORS = {
  DUPLICATE: '이미 신고한 글입니다.',
  FORBIDDEN: '본인 글은 신고할 수 없습니다.',
};

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
      <section>
        <p className="field-error">{error}</p>
        <Link to="/pass-stories">목록으로</Link>
      </section>
    );
  }
  if (!story) return <Loading />;

  return (
    <section>
      <h1>{story.jmfldnm} 합격수기</h1>
      <p>
        {story.nickname} <RankBadge rank={story.current_rank} /> · {formatDate(story.created_at)}
      </p>

      <HiddenNotice status={story.status} hiddenReason={story.hidden_reason} />

      <dl>
        {story.examinee_base && (
          <>
            <dt>응시자 베이스</dt>
            <dd>{EXAMINEE_BASE_LABELS[story.examinee_base]}</dd>
          </>
        )}
        {story.prep_period && (
          <>
            <dt>준비 기간</dt>
            <dd>{PREP_PERIOD_LABELS[story.prep_period]}</dd>
          </>
        )}
        {story.attempt_band && (
          <>
            <dt>응시 횟수</dt>
            <dd>{ATTEMPT_LABELS[story.attempt_band]}</dd>
          </>
        )}
        {story.textbook_name && (
          <>
            <dt>사용 교재</dt>
            <dd>{story.textbook_name}</dd>
          </>
        )}
        {story.pass_score != null && (
          <>
            <dt>합격 점수</dt>
            <dd>{story.pass_score}</dd>
          </>
        )}
        {story.exam_date && (
          <>
            <dt>응시일</dt>
            <dd>{formatDate(story.exam_date)}</dd>
          </>
        )}
      </dl>

      {/* body는 서버에서 허용 태그만 남긴 HTML (HtmlSanitizer). 타인의 숨김 글이면 null */}
      {story.body != null && <div className="pass-story-body" dangerouslySetInnerHTML={{ __html: story.body }} />}

      <div>
        <Link to="/pass-stories" className="btn">
          목록으로
        </Link>
        {story.mine && (
          <>
            <Link to={`/pass-stories/${id}/edit`} className="btn">
              수정
            </Link>
            <button type="button" className="btn" onClick={() => setModal('delete')}>
              삭제
            </button>
          </>
        )}
        {!story.mine && story.status === 'PUBLISHED' && (
          <button type="button" className="btn" onClick={() => setModal('report')}>
            신고하기
          </button>
        )}
      </div>

      {modal === 'delete' && (
        <ConfirmModal
          message="삭제한 글은 되돌릴 수 없습니다. 삭제할까요?"
          confirmText="삭제"
          onConfirm={remove}
          onCancel={() => setModal(null)}
        />
      )}
      {modal === 'report' && <ReportModal onSubmit={report} onCancel={() => setModal(null)} />}
      {modal?.alert && <AlertModal message={modal.alert} onClose={() => setModal(null)} />}
    </section>
  );
}
