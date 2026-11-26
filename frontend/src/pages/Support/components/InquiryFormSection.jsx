import { useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { createInquiry } from '@/api/supportApi';
import AlertModal from '@/components/common/AlertModal';
import FieldError from '@/components/common/FieldError';
import { INQUIRY_TYPES } from '@/constants/inquiry';
import { PageHead } from '@/pages/Support/components/SupportParts';
import { errorMessage } from '@/pages/Support/supportShared';
import iconGuide from '@/pages/Support/assets/form-guide.svg';
import iconChevron from '@/pages/Support/assets/select-chevron.svg';
import iconSend from '@/pages/Support/assets/send.svg';

// inquiries.title·content 컬럼 크기 (시안의 60자·2,000자가 아니라 DB 기준)
const TITLE_MAX = 50;
const CONTENT_MAX = 1000;
const EMPTY_FORM = { inquiryType: '', title: '', content: '' };

function validate(form) {
  const errors = {};
  if (!form.inquiryType) errors.inquiryType = '문의 유형을 선택해 주세요.';
  if (!form.title.trim()) errors.title = '제목을 입력해 주세요.';
  if (!form.content.trim()) errors.content = '내용을 입력해 주세요.';
  return errors;
}

function FieldLabel({ htmlFor, label, hint }) {
  return (
    <div className="support-field__label">
      <label htmlFor={htmlFor}>
        {label} <span className="support-field__required">*</span>
      </label>
      <span className="support-field__hint">{hint}</span>
    </div>
  );
}

// CS-04 1:1 문의 작성 (회원). 접수 후 공지사항 목록으로 이동
export default function InquiryFormSection() {
  const [, setParams] = useSearchParams();
  const [form, setForm] = useState(EMPTY_FORM);
  const [errors, setErrors] = useState({});
  const [submitError, setSubmitError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [done, setDone] = useState(false);

  const change = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
    setErrors((prev) => ({ ...prev, [name]: undefined }));
  };

  const submit = async (e) => {
    e.preventDefault();
    const nextErrors = validate(form);
    setErrors(nextErrors);
    if (Object.keys(nextErrors).length > 0) return;

    setSubmitting(true);
    setSubmitError('');
    try {
      await createInquiry({ inquiry_type: form.inquiryType, title: form.title.trim(), content: form.content.trim() });
      setForm(EMPTY_FORM);
      setDone(true);
    } catch (err) {
      setSubmitError(errorMessage(err, '문의를 접수하지 못했습니다. 잠시 후 다시 시도해 주세요.'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <PageHead
        crumbs={['고객센터', '1:1 문의하기']}
        title="1:1 문의하기"
        desc="수험 준비 중 발생한 불편과 시스템 제안을 세심하게 경청합니다."
      />

      <div className="support-card support-form-card">
        <div className="support-guide">
          <img src={iconGuide} alt="" />
          <div>
            <p className="support-guide__title">정확한 해결을 위한 가이드</p>
            <p className="support-guide__text">
              자격증 시험일정 오기재, 응시요건 분석 오류, 플래너 기록 동기화 문제는 발생한 화면과 상황을 자세히 적어
              주시면 담당자가 확인 후 답변해 드립니다.
            </p>
          </div>
        </div>

        <form className="support-form" onSubmit={submit} noValidate>
          <div className="support-field">
            <FieldLabel htmlFor="inquiry-type" label="문의 유형" hint="필수 선택" />
            <div className="support-select">
              <select
                id="inquiry-type"
                name="inquiryType"
                className="support-input"
                value={form.inquiryType}
                onChange={change}
                aria-invalid={Boolean(errors.inquiryType)}
              >
                <option value="">유형을 선택해 주세요</option>
                {INQUIRY_TYPES.map((t) => (
                  <option key={t.value} value={t.value}>
                    {t.label}
                  </option>
                ))}
              </select>
              <img src={iconChevron} alt="" />
            </div>
            <FieldError message={errors.inquiryType} />
          </div>

          <div className="support-field">
            <FieldLabel htmlFor="inquiry-title" label="문의 제목" hint={`${form.title.length} / ${TITLE_MAX}자`} />
            <input
              id="inquiry-title"
              name="title"
              className="support-input"
              value={form.title}
              maxLength={TITLE_MAX}
              onChange={change}
              placeholder="제목을 입력해 주세요."
              aria-invalid={Boolean(errors.title)}
            />
            <FieldError message={errors.title} />
          </div>

          <div className="support-field">
            <FieldLabel
              htmlFor="inquiry-content"
              label="문의 내용"
              hint={`${form.content.length.toLocaleString()} / ${CONTENT_MAX.toLocaleString()}자`}
            />
            <textarea
              id="inquiry-content"
              name="content"
              className="support-input support-input--textarea"
              value={form.content}
              maxLength={CONTENT_MAX}
              rows={8}
              onChange={change}
              placeholder="문의 내용을 상세히 작성해 주시면 보다 빠르고 정확한 답변이 가능합니다."
              aria-invalid={Boolean(errors.content)}
            />
            <FieldError message={errors.content} />
          </div>

          {submitError && (
            <p className="support-error" role="alert">
              {submitError}
            </p>
          )}
          <div className="support-form__actions">
            <button
              type="button"
              className="support-btn support-btn--secondary"
              onClick={() => setParams({ menu: 'notices' })}
            >
              취소
            </button>
            <button type="submit" className="support-btn support-btn--primary" disabled={submitting}>
              <img src={iconSend} alt="" />
              {submitting ? '등록 중...' : '문의 등록'}
            </button>
          </div>
        </form>
      </div>

      {done && (
        <AlertModal
          message="문의가 접수되었습니다. 답변은 나의 문의 내역에서 확인할 수 있습니다."
          onClose={() => setParams({ menu: 'notices' })}
        />
      )}
    </>
  );
}
