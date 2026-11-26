import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { getOptions } from '@/api/optionApi';
import { createPassStory, getPassStory, updatePassStory } from '@/api/passStoryApi';
import ConfirmModal from '@/components/common/ConfirmModal';
import FieldError from '@/components/common/FieldError';
import Loading from '@/components/common/Loading';
import SearchSelect from '@/components/common/SearchSelect';
import HiddenNotice from '@/components/passstory/HiddenNotice';
import { ATTEMPT_LABELS, EXAMINEE_BASE_LABELS, PREP_PERIOD_LABELS } from '@/components/passstory/labels';
import { errorMessage, htmlToText, textToHtml } from '@/components/passstory/utils';
import '@/styles/passstory.css';

const MAX_BODY = 1500;
const MAX_SCORE = 1500;
const EMPTY_FORM = {
  jmcd: '',
  jmfldnm: '',
  text: '',
  examinee_base: '',
  prep_period: '',
  attempt_band: '',
  textbook_name: '',
  pass_score: '',
  exam_date: '',
};

// SearchSelect가 fetchOptions 변경마다 다시 조회하므로 모듈 상수로 고정
const fetchQualifications = (query) => getOptions('qualifications', { query, limit: 20 });

function CodeSelect({ id, label, value, labels, onChange }) {
  return (
    <p className="ps-field">
      <label htmlFor={id} className="ps-field__label">
        {label}
      </label>
      <select id={id} value={value} onChange={(e) => onChange(e.target.value)}>
        <option value="">선택 안 함</option>
        {Object.entries(labels).map(([code, text]) => (
          <option key={code} value={code}>
            {text}
          </option>
        ))}
      </select>
    </p>
  );
}

// 선택 버튼 묶음. 선택한 버튼을 다시 누르면 선택 해제 (선택 입력 항목)
function ChoiceGroup({ label, value, labels, onChange }) {
  return (
    <div className="ps-field" role="group" aria-label={label}>
      <span className="ps-field__label">{label}</span>
      <div className="ps-choices">
        {Object.entries(labels).map(([code, text]) => (
          <button
            key={code}
            type="button"
            className="btn"
            aria-pressed={value === code}
            onClick={() => onChange(value === code ? '' : code)}
          >
            {value === code && '✓ '}
            {text}
          </button>
        ))}
      </div>
    </div>
  );
}

export default function PassStoryFormPage() {
  const { id } = useParams();
  const isEdit = Boolean(id);
  const navigate = useNavigate();

  const [form, setForm] = useState(EMPTY_FORM);
  const [status, setStatus] = useState('PUBLISHED');
  const [loaded, setLoaded] = useState(!isEdit);
  const [loadError, setLoadError] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [submitError, setSubmitError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [confirmCancel, setConfirmCancel] = useState(false);

  useEffect(() => {
    if (!isEdit) return;
    getPassStory(id)
      .then((s) => {
        if (!s.mine) {
          setLoadError('본인 글만 수정할 수 있습니다.');
          return;
        }
        setForm({
          jmcd: s.jmcd,
          jmfldnm: s.jmfldnm,
          text: htmlToText(s.body),
          examinee_base: s.examinee_base ?? '',
          prep_period: s.prep_period ?? '',
          attempt_band: s.attempt_band ?? '',
          textbook_name: s.textbook_name ?? '',
          pass_score: s.pass_score ?? '',
          exam_date: s.exam_date ?? '',
        });
        setStatus(s.status);
        setLoaded(true);
      })
      .catch((e) => setLoadError(errorMessage(e)));
  }, [id, isEdit]);

  const set = (key) => (value) => setForm((f) => ({ ...f, [key]: value }));
  const bodyHtml = textToHtml(form.text);

  const submit = async (e) => {
    e.preventDefault();
    const errors = {};
    if (!form.jmcd) errors.jmcd = '자격증을 선택해 주세요.';
    if (!form.text.trim()) errors.text = '상세 후기를 입력해 주세요.';
    if (bodyHtml.length > MAX_BODY) errors.text = `상세 후기는 ${MAX_BODY}자 이내로 입력해 주세요.`;
    if (form.pass_score !== '') {
      const score = Number(form.pass_score);
      // 서버 @Digits(fraction = 2)와 맞춤: 소수점 둘째 자리까지
      if (!(score >= 0 && score <= MAX_SCORE) || !/^\d+(\.\d{1,2})?$/.test(String(form.pass_score))) {
        errors.pass_score = `합격 점수는 0~${MAX_SCORE} 사이, 소수점 둘째 자리까지 입력해 주세요.`;
      }
    }
    setFieldErrors(errors);
    if (Object.keys(errors).length) return;

    const request = {
      jmcd: form.jmcd,
      body: bodyHtml,
      examinee_base: form.examinee_base || null,
      prep_period: form.prep_period || null,
      attempt_band: form.attempt_band || null,
      textbook_name: form.textbook_name.trim() || null,
      pass_score: form.pass_score === '' ? null : Number(form.pass_score),
      exam_date: form.exam_date || null,
    };

    setSubmitting(true);
    setSubmitError('');
    try {
      if (isEdit) {
        await updatePassStory(id, request);
        navigate(`/pass-stories/${id}`);
      } else {
        const result = await createPassStory(request);
        navigate('/pass-stories', { state: result.ranked_up ? { rankUp: result } : null });
      }
    } catch (err) {
      setSubmitError(errorMessage(err, { NOT_FOUND: '선택한 자격증을 찾을 수 없습니다.' }));
      setSubmitting(false);
    }
  };

  if (loadError) {
    return (
      <section className="ps">
        <p className="field-error">{loadError}</p>
      </section>
    );
  }
  if (!loaded) return <Loading />;

  const title = isEdit ? '합격 후기 수정' : '합격 후기 등록';

  return (
    <section className="ps">
      <div className="ps-form">
        <nav className="ps-crumb" aria-label="현재 위치">
          <Link to="/pass-stories">시험 합격 후기</Link> › <strong>{isEdit ? '후기 수정' : '후기 등록'}</strong>
        </nav>
        <h1>{title}</h1>
        <p className="ps-form__lead">
          다른 수험생들에게 실질적인 도움이 될 수 있도록 생생한 합격 팁과 솔직한 학습 전략을 나눠 주세요.
        </p>

        <form onSubmit={submit} noValidate>
          <div className="ps-form__card ps-box">
            {status !== 'PUBLISHED' && (
              <>
                <HiddenNotice status={status} />
                <p className="ps-form__caution">수정하면 관리자 재검토를 거친 뒤 다시 공개됩니다.</p>
              </>
            )}

            <div className="ps-field">
              {/* SearchSelect(이은아)가 input id를 받지 않아 label 연결 불가 → 문구로 표시 */}
              <span className="ps-field__label">
                <span className="ps-required">자격증 종목</span>
                <small>선택한 자격증 기준으로 게시됩니다</small>
              </span>
              <SearchSelect
                placeholder="자격증 이름 검색"
                fetchOptions={fetchQualifications}
                getLabel={(item) => item.jmfldnm}
                onSelect={(item) => setForm((f) => ({ ...f, jmcd: item.jmcd, jmfldnm: item.jmfldnm }))}
              />
              {form.jmfldnm && <p className="ps-field__selected">선택한 자격증: {form.jmfldnm}</p>}
              <FieldError message={fieldErrors.jmcd} />
            </div>

            <ChoiceGroup
              label="응시자 베이스"
              value={form.examinee_base}
              labels={EXAMINEE_BASE_LABELS}
              onChange={set('examinee_base')}
            />

            <div className="ps-row">
              <CodeSelect
                id="ps-period"
                label="시험 준비 기간"
                value={form.prep_period}
                labels={PREP_PERIOD_LABELS}
                onChange={set('prep_period')}
              />
              <ChoiceGroup
                label="시험 응시 횟수"
                value={form.attempt_band}
                labels={ATTEMPT_LABELS}
                onChange={set('attempt_band')}
              />
            </div>

            <p className="ps-field">
              <label htmlFor="ps-textbook" className="ps-field__label">
                사용한 교재 / 학습 수단
                <small>{form.textbook_name.length} / 50자</small>
              </label>
              <input
                id="ps-textbook"
                maxLength={50}
                value={form.textbook_name}
                onChange={(e) => set('textbook_name')(e.target.value)}
              />
            </p>

            <div className="ps-row">
              <div className="ps-field">
                <label htmlFor="ps-score" className="ps-field__label">
                  취득 점수
                </label>
                <div className="ps-suffix">
                  <input
                    id="ps-score"
                    type="number"
                    min="0"
                    max={MAX_SCORE}
                    step="0.01"
                    value={form.pass_score}
                    onChange={(e) => set('pass_score')(e.target.value)}
                  />
                  <span aria-hidden="true">점</span>
                </div>
                <FieldError message={fieldErrors.pass_score} />
              </div>
              <p className="ps-field">
                <label htmlFor="ps-date" className="ps-field__label">
                  시험 응시일
                </label>
                <input
                  id="ps-date"
                  type="date"
                  value={form.exam_date}
                  onChange={(e) => set('exam_date')(e.target.value)}
                />
              </p>
            </div>

            <div className="ps-field">
              <label htmlFor="ps-body" className="ps-field__label">
                <span className="ps-required">생생한 합격 후기 내용</span>
                <small>
                  {bodyHtml.length} / {MAX_BODY}자
                </small>
              </label>
              <textarea id="ps-body" rows={10} value={form.text} onChange={(e) => set('text')(e.target.value)} />
              <FieldError message={fieldErrors.text} />
            </div>

            <p className="ps-form__caution">
              • 부적절한 홍보, 욕설, 타인을 비방하는 글은 사전 고지 없이 숨김 처리될 수 있습니다.
            </p>
          </div>

          {submitError && <p className="field-error">{submitError}</p>}
          <div className="ps-form__actions">
            <button type="button" className="btn" onClick={() => setConfirmCancel(true)}>
              취소
            </button>
            <button type="submit" className="btn btn--primary" disabled={submitting}>
              ✓ {isEdit ? '수정하기' : '등록하기'}
            </button>
          </div>
        </form>
      </div>

      {confirmCancel && (
        <ConfirmModal
          message="작성 중인 글은 저장되지 않습니다. 나갈까요?"
          confirmText="나가기"
          onConfirm={() => navigate(-1)}
          onCancel={() => setConfirmCancel(false)}
        />
      )}
    </section>
  );
}
