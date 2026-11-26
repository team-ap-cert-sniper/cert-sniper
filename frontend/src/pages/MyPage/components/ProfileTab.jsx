import { useCallback, useEffect, useState } from 'react';
import { getOptions } from '@/api/optionApi';
import { getMyProfile, updateMyProfile } from '@/api/userApi';
import FieldError from '@/components/common/FieldError';
import Loading from '@/components/common/Loading';
import SearchSelect from '@/components/common/SearchSelect';
import RankBadge from '@/components/passstory/RankBadge';
import { errorMessage } from '@/components/passstory/utils';
import { GRADES } from '@/constants/grades';

const gradeLabel = (value) => GRADES.find((g) => g.value === value)?.label ?? '';
const optionLabel = (item) => item.label ?? item.name;
const fetchUniversities = (query) => getOptions('universities', { query, limit: 20 });
const fetchQualifications = (query) => getOptions('qualifications', { query, limit: 20 });

// shortcut: 학부 전체(FACULTY) 항목은 college_id, 전공(MAJOR) 항목은 major_id로 저장 — 팀 규칙 확정 전 기본값 (kiwan.md Q12)
const schoolIds = (item) =>
  item.scope === 'FACULTY'
    ? { college_id: Number(item.id), major_id: null }
    : { college_id: null, major_id: Number(item.id) };

function toForm(p) {
  return {
    nickname: p.nickname,
    noSchool: p.university_id == null,
    university_id: p.university_id,
    university_name: p.university_name ?? '',
    college_id: p.college_id,
    major_id: p.major_id,
    major_name: p.major_name ?? '',
    grade: p.grade ?? '',
    owned: p.owned_certificates.map((c) => ({ jmcd: c.jmcd, name: c.jmfldnm ?? c.cert_name })),
    certInput: '',
  };
}

function validate(f) {
  if (f.nickname.trim().length < 2 || f.nickname.trim().length > 10) return '닉네임은 2~10자로 입력해 주세요.';
  if (!f.noSchool && (!f.university_id || (!f.college_id && !f.major_id) || !f.grade)) {
    return '대학교·전공·학년을 모두 선택하거나 "학교 정보 없음"을 선택해 주세요.';
  }
  return '';
}

export default function ProfileTab() {
  const [profile, setProfile] = useState(null);
  const [form, setForm] = useState(null); // null이면 보기 모드
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  useEffect(() => {
    getMyProfile()
      .then(setProfile)
      .catch((e) => setError(errorMessage(e)));
  }, []);

  const universityId = form?.university_id;
  const fetchMajors = useCallback(
    (query) => getOptions('majors', { university_id: universityId, query, limit: 50 }),
    [universityId],
  );

  const update = (patch) => setForm((f) => ({ ...f, ...patch }));

  const addOwned = (entry) =>
    setForm((f) =>
      f.owned.some((o) => o.jmcd === entry.jmcd && o.name === entry.name) ? f : { ...f, owned: [...f.owned, entry] },
    );

  const save = async (e) => {
    e.preventDefault();
    const message = validate(form);
    if (message) {
      setError(message);
      return;
    }
    const request = {
      nickname: form.nickname.trim(),
      university_id: form.noSchool ? null : form.university_id,
      college_id: form.noSchool ? null : form.college_id,
      major_id: form.noSchool ? null : form.major_id,
      grade: form.noSchool ? null : form.grade,
      owned_jmcds: form.owned.filter((o) => o.jmcd).map((o) => o.jmcd),
      owned_cert_names: form.owned.filter((o) => !o.jmcd).map((o) => o.name),
    };
    try {
      setProfile(await updateMyProfile(request));
      setForm(null);
      setError('');
      setNotice('회원정보가 저장되었습니다.');
    } catch (err) {
      setError(errorMessage(err, { DUPLICATE: '이미 사용 중인 닉네임입니다.' }));
    }
  };

  if (!profile) return error ? <p className="field-error">{error}</p> : <Loading />;

  if (!form) {
    return (
      <section className="mypage-card">
        <h2 className="mypage-title">회원정보</h2>
        {notice && (
          <p role="status" className="mypage-ok">
            ✓ {notice}
          </p>
        )}
        <dl className="mypage-dl">
          <dt>이메일</dt>
          <dd>{profile.email}</dd>
          <dt>이름</dt>
          <dd>{profile.name}</dd>
          <dt>닉네임</dt>
          <dd>{profile.nickname}</dd>
          <dt>저격 등급</dt>
          <dd>
            <RankBadge rank={profile.current_rank} />
          </dd>
          <dt>학력</dt>
          <dd>
            {profile.university_id == null
              ? '학교 정보 없음'
              : `${profile.university_name ?? ''} ${profile.major_name ?? ''} ${gradeLabel(profile.grade)}`}
          </dd>
          <dt>보유 자격증</dt>
          <dd>
            {profile.owned_certificates.length === 0 ? (
              '없음'
            ) : (
              <span className="mypage-tags">
                {profile.owned_certificates.map((c) => (
                  <span key={`${c.jmcd ?? ''}:${c.jmfldnm ?? c.cert_name}`} className="mypage-tag">
                    {c.jmfldnm ?? c.cert_name}
                  </span>
                ))}
              </span>
            )}
          </dd>
        </dl>
        <div className="mypage-actions">
          <button
            type="button"
            className="btn btn--primary"
            onClick={() => {
              setForm(toForm(profile));
              setNotice('');
              setError('');
            }}
          >
            정보 수정
          </button>
        </div>
      </section>
    );
  }

  return (
    <section className="mypage-card">
      <h2 className="mypage-title">회원정보 수정</h2>
      <form onSubmit={save} noValidate>
        <div className="mypage-block">
          <p className="mypage-field">
            <span className="mypage-label">이메일</span>
            <span>{profile.email} (변경 불가)</span>
          </p>
          <p className="mypage-field mypage-field--half">
            <label htmlFor="profile-nickname" className="mypage-label ps-required">
              닉네임
            </label>
            <input
              id="profile-nickname"
              maxLength={10}
              value={form.nickname}
              onChange={(e) => update({ nickname: e.target.value })}
            />
          </p>
        </div>

        <fieldset className="mypage-block">
          <legend className="mypage-section">학력 및 관심 정보</legend>
          <label className="mypage-check">
            <input type="checkbox" checked={form.noSchool} onChange={(e) => update({ noSchool: e.target.checked })} />
            대학교 다닌 적 없음 (체크하면 대학교 및 전공 정보를 입력하지 않아도 됩니다.)
          </label>
          {!form.noSchool && (
            <>
              <div className="mypage-field mypage-field--half">
                <span className="mypage-label">대학교</span>
                <SearchSelect
                  placeholder="대학교 검색"
                  fetchOptions={fetchUniversities}
                  getLabel={optionLabel}
                  onSelect={(item) =>
                    update({
                      university_id: Number(item.id),
                      university_name: optionLabel(item),
                      college_id: null,
                      major_id: null,
                      major_name: '',
                    })
                  }
                />
                {form.university_name && <p className="mypage-picked">선택: {form.university_name}</p>}
              </div>
              <div className="mypage-row">
                <div className="mypage-field">
                  <span className="mypage-label">전공</span>
                  {/* 대학교가 바뀌면 검색 입력을 비우기 위해 key로 다시 마운트 */}
                  <SearchSelect
                    key={form.university_id ?? 'none'}
                    placeholder="전공 검색"
                    disabled={!form.university_id}
                    fetchOptions={fetchMajors}
                    getLabel={optionLabel}
                    onSelect={(item) => update({ ...schoolIds(item), major_name: optionLabel(item) })}
                  />
                  {form.major_name && <p className="mypage-picked">선택: {form.major_name}</p>}
                </div>
                <p className="mypage-field">
                  <label htmlFor="profile-grade" className="mypage-label">
                    학년
                  </label>
                  <select id="profile-grade" value={form.grade} onChange={(e) => update({ grade: e.target.value })}>
                    <option value="">선택</option>
                    {GRADES.map((g) => (
                      <option key={g.value} value={g.value}>
                        {g.label}
                      </option>
                    ))}
                  </select>
                </p>
              </div>
            </>
          )}
        </fieldset>

        <fieldset className="mypage-block">
          <legend className="mypage-section">보유 자격증</legend>
          <ul className="mypage-tags mypage-tags--box">
            {form.owned.length === 0 && <li className="mypage-muted">아래에서 자격증을 검색해 추가하세요.</li>}
            {form.owned.map((o) => (
              <li key={`${o.jmcd ?? ''}:${o.name}`} className="mypage-tag">
                {o.name}
                <button
                  type="button"
                  aria-label={`${o.name} 삭제`}
                  onClick={() => update({ owned: form.owned.filter((x) => x !== o) })}
                >
                  ×
                </button>
              </li>
            ))}
          </ul>
          <div className="mypage-field mypage-field--half">
            <span className="mypage-label">자격증 검색으로 추가</span>
            <SearchSelect
              placeholder="자격증 명칭 검색 (예: 빅데이터분석기사)"
              fetchOptions={fetchQualifications}
              getLabel={(item) => item.jmfldnm}
              onSelect={(item) => addOwned({ jmcd: item.jmcd, name: item.jmfldnm })}
            />
          </div>
          <div className="mypage-field mypage-field--half">
            <label htmlFor="profile-cert-name" className="mypage-label">
              목록에 없는 자격증 직접 입력
            </label>
            <div className="mypage-inline">
              <input
                id="profile-cert-name"
                maxLength={100}
                value={form.certInput}
                onChange={(e) => update({ certInput: e.target.value })}
              />
              <button
                type="button"
                className="btn btn--primary"
                disabled={!form.certInput.trim()}
                onClick={() => {
                  addOwned({ jmcd: null, name: form.certInput.trim() });
                  update({ certInput: '' });
                }}
              >
                추가
              </button>
            </div>
          </div>
        </fieldset>

        <FieldError message={error} />
        <div className="mypage-actions">
          <button type="button" className="btn" onClick={() => setForm(null)}>
            취소
          </button>
          <button type="submit" className="btn btn--primary">
            수정 완료
          </button>
        </div>
      </form>
    </section>
  );
}
