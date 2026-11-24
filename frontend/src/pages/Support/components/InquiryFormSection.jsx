import { useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { createInquiry } from '@/api/supportApi';
import AlertModal from '@/components/common/AlertModal';
import FieldError from '@/components/common/FieldError';
import { INQUIRY_TYPES } from '@/constants/inquiry';
import { errorMessage } from '@/pages/Support/supportShared';

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
    <div>
      <h2>1:1 문의하기</h2>
      <form onSubmit={submit} noValidate>
        <div>
          <label htmlFor="inquiry-type">문의 유형</label>
          <select id="inquiry-type" name="inquiryType" value={form.inquiryType} onChange={change}>
            <option value="">선택해 주세요</option>
            {INQUIRY_TYPES.map((t) => (
              <option key={t.value} value={t.value}>
                {t.label}
              </option>
            ))}
          </select>
          <FieldError message={errors.inquiryType} />
        </div>

        <div>
          <label htmlFor="inquiry-title">제목</label>
          <input
            id="inquiry-title"
            name="title"
            value={form.title}
            maxLength={TITLE_MAX}
            onChange={change}
            placeholder={`${TITLE_MAX}자 이내`}
          />
          <span>
            {form.title.length}/{TITLE_MAX}
          </span>
          <FieldError message={errors.title} />
        </div>

        <div>
          <label htmlFor="inquiry-content">내용</label>
          <textarea
            id="inquiry-content"
            name="content"
            value={form.content}
            maxLength={CONTENT_MAX}
            rows={8}
            onChange={change}
            placeholder={`${CONTENT_MAX}자 이내`}
          />
          <span>
            {form.content.length}/{CONTENT_MAX}
          </span>
          <FieldError message={errors.content} />
        </div>

        {submitError && <p role="alert">{submitError}</p>}
        <button type="submit" className="btn btn--primary" disabled={submitting}>
          {submitting ? '접수 중...' : '문의 접수'}
        </button>
      </form>

      {done && (
        <AlertModal
          message="문의가 접수되었습니다. 답변은 나의 문의 내역에서 확인할 수 있습니다."
          onClose={() => setParams({ menu: 'notices' })}
        />
      )}
    </div>
  );
}
