"""추천·선택목록·자격증 상세·D-Day. 모든 반환값은 json.dumps 가능한 dict다."""
from __future__ import annotations
import json
from decimal import Decimal, ROUND_HALF_UP
from datetime import date
import re
from .eligibility import EXAM_WINDOW_SOURCE, start_level, EXPERIENCE_ROUTE, FOUR_YEAR_TYPES, LEVELS, base_name, grade_exam_window, level
from .store import DataStore, DEFAULT_DATA_DIR


def percent(part, whole, places=2):
    """part ÷ whole × 100을 소수 places자리에서 반올림(사사오입, MySQL ROUND와 같음). whole이 0이면 None."""
    if not whole:
        return None
    q = Decimal(1).scaleb(-places)
    return float((Decimal(part) * 100 / Decimal(whole)).quantize(q, rounding=ROUND_HALF_UP))


class ValidationError(ValueError):
    """잘못된 입력. 백엔드에서는 HTTP 400 VALIDATION_ERROR 로 매핑한다."""


class NotFoundError(ValidationError):
    """없는 종목코드·시험 회차. 백엔드에서는 HTTP 404 NOT_FOUND 로 매핑한다."""


IDIOM_CYCLE_START = date(2026, 1, 1)   # 사자성어 순환 기준일: 이날이 1번
GRADES = ('1', '2', '3', '4', '5', '6', 'GRADUATED', 'NOT_ENROLLED')
PROFILE_KEYS = {'school_id', 'major_id', 'grade', 'owned_qualification_ids'}   # 전공 맞춤 추천: 학교·학과·학년·보유 자격
TYPE_CODE = {'TECHNICAL': 'T', 'PROFESSIONAL': 'S'}   # 추천 조건의 자격구분 → qualgbcd
# 같은 해 안에서 단계 표시 순서 (필기→실기, 1차→2차→3차→면접)
STAGE_ORDER = {'필기': 0, '1차': 1, '2차': 2, '3차': 3, '실기': 4, '면접': 5, '시험': 6}
# 추천 순위 점수표. 항목별 점수와 이유를 score_breakdown 으로 함께 돌려준다.
SCORE = {
    'JOB_FIELD': 30, 'INTEREST_FIELD': 20,
    'existing_direct_links': 40, 'recommended': 35, 'same_subfield': 30, 'faculty_aggregate': 40,
    'NOW': 20, 'NO_RESTRICTION': 20, 'FROM_GRADE_NEXT': 10, 'FROM_GRADE_LATER': 5, 'CHECK_QUALIFICATION': 5,
    'AFTER_GRADUATION_EXPERIENCE': 0, 'UPCOMING_2026': 10, 'LEVEL_FIT': 5, 'LEVEL_UNFIT': -20,
    'NO_ELIGIBILITY_REQUIRED': 5,
}
RESULT_LIMIT = 5                                  # 추천 화면 최대 개수(후보가 적으면 그 수만큼)
CONDITION_KEYS = {'qualification_type', 'job_field_ids', 'interest_field_ids', 'owned_qualification_ids'}
BASIS_LABEL = {'existing_direct_links': '학과 직접 연결', 'recommended': '전공 연관', 'same_subfield': '동계열 공통',
               'faculty_aggregate': '학부 소속 학과 합산'}
# 트렌드: 최근 완결 연도의 1차(필기·1차·단일시험) 응시자 수 순위. 국가기술·국가전문 분리.
FIRST_STAGES = ('필기', '1차', '시험')
TREND_TYPES = {'TECHNICAL': '국가기술자격', 'PROFESSIONAL': '국가전문자격'}
TREND_YEARS, TREND_THRESHOLD = 5, 10.0      # 5년(기준연도 포함) 응시자 증감률 ±10% 넘으면 증가/감소
ROADMAP_PER_STAGE = 5                          # 학년별 로드맵: 단계마다 점수 상위 몇 개
GRADE_NUM = re.compile(r'\s*\d급\s*')             # 국가전문 급수(1급 청소년지도사, 정수시설운영관리사2급)
QUAL_SERIES = ('기능사', '산업기사', '기사', '기능장', '기술사', '서비스')   # 자격증 검색 등급 필터(국가기술)
OPTION_KINDS = ('schools', 'majors', 'grades', 'job_fields', 'interest_fields', 'qualifications')


class JeogyeokjeungService:
    def __init__(self, data_dir=DEFAULT_DATA_DIR, store: DataStore | None = None):
        self.db = store or DataStore(data_dir)
        q = self.db.qualifications.values()
        self.interest_fields = {r['obligfldcd']: r['obligfldnm'] for r in q if r['obligfldcd']}   # 관심분야 = Q-Net 직무분야(대)
        self.job_fields = {r['mdobligfldcd']: r['mdobligfldnm'] for r in q if r['mdobligfldcd']}   # 희망직무 = Q-Net 직무분야(중)
        # 입력 흐름: 대학교 → 전공. 학부는 별도 입력 단계가 아니다.
        #  - 전공 목록의 학부 이름 항목(예: 첨단IT학부)을 고르면 그 학부 소속 전공 합산 추천(major_faculty)
        #  - 학부 이름이 전공 목록에 없는 학부는 '(학부 전체)' 항목으로 전공 목록에 넣는다(faculty_scope_id 를 major_id 로 받음)
        by_name = {(m['school_id'], m['major_name']): mid for mid, m in sorted(self.db.majors.items())}
        self.major_faculty, self.faculty_items = {}, {}
        for fs, f in sorted(self.db.faculties.items()):
            members = self.db.faculty_members[fs]
            mid = by_name.get((f['school_id'], f['faculty_name']))
            if mid and members - {mid}:
                self.major_faculty.setdefault(mid, fs)
            elif not mid and members:
                self.faculty_items[fs] = f

    # ---------- 입력 검증 ----------
    def _check_codes(self, p: dict, keys) -> None:
        """선택 코드 목록 검증: 중복 없음, 목록에 있는 코드, 최대 개수(직무·관심 5개)."""
        catalogs = {'job_field_ids': (self.job_fields, 5), 'interest_field_ids': (self.interest_fields, 5),
                    'owned_qualification_ids': (self.db.qualifications, len(self.db.qualifications))}
        for key in keys:
            catalog, maximum = catalogs[key]
            values = p.get(key, [])
            if (not isinstance(values, list) or len(values) > maximum
                    or any(not isinstance(v, str) or v not in catalog for v in values)
                    or len(set(values)) != len(values)):
                raise ValidationError(key + ': 유효한 고유 선택 코드 필요')
            p[key] = values

    def validate(self, profile: dict) -> dict:
        """전공 맞춤 추천 입력. 대학교·전공·학년은 함께 입력하거나 모두 비운다(학교 정보 없음)."""
        if not isinstance(profile, dict) or set(profile) - PROFILE_KEYS:
            raise ValidationError('알 수 없는 추천 입력 필드')
        p = dict(profile)
        for key in ('school_id', 'major_id', 'grade'):
            p[key] = p.get(key) or None
        filled = [p[k] is not None for k in ('school_id', 'major_id', 'grade')]
        if any(filled) and not all(filled):
            raise ValidationError('대학교·전공·학년은 함께 입력하거나 모두 비워 둠')
        if p['grade'] is not None and (not isinstance(p['grade'], str) or p['grade'] not in GRADES):
            raise ValidationError('grade 선택값 필요')
        self._check_codes(p, ('owned_qualification_ids',))
        for key, catalog in [('school_id', self.db.schools), ('major_id', {**self.db.majors, **self.faculty_items})]:
            value = p[key]
            if value is not None and (not isinstance(value, str) or value not in catalog):
                raise ValidationError(key + ': 유효하지 않거나 서비스 제외된 선택')
        major = p['major_id']
        if major and (self.db.majors.get(major) or self.faculty_items.get(major))['school_id'] != p['school_id']:
            raise ValidationError('학교·캠퍼스와 전공 불일치')
        return p

    # ---------- 선택 목록(회원가입 드롭다운) ----------
    def options(self, kind, school_id=None, query='', offset=0, limit=50, qualification_type=None, series=None,
                interest_field_id=None, job_field_id=None) -> dict:
        if type(offset) is not int or type(limit) is not int or offset < 0 or not 1 <= limit <= 100:
            raise ValidationError('offset >= 0, 1 <= limit <= 100 필요')
        if not isinstance(query, str):
            raise ValidationError('검색어는 문자열')
        if school_id is not None and (not isinstance(school_id, str) or school_id not in self.db.schools):
            raise ValidationError('school_id: 유효하지 않거나 서비스 제외된 선택')
        if kind == 'schools':
            values = [{'id': s['school_id'], 'name': s['school_name'], 'campus': s['campus'],
                       'school_type': s['school_type']} for s in self.db.schools.values()]
        elif kind == 'majors':
            if not school_id:
                raise ValidationError('학교 선택 필요')
            # 전공 + 학부 전체 항목. scope: MAJOR(전공 기준) | FACULTY(소속 전공 합산).
            # label = '전공 · 소속 학부'. 검색은 전공 이름과 소속 학부 이름 둘 다(경상 → 경상학부·회계학·경영학).
            # 정렬은 학부별로 묶어 학부 항목 먼저, 그 아래 소속 전공 가나다순.
            values = [{'id': mid, 'name': m['major_name'], 'parent_name': m['parent_name'] or m['college_name'],
                       'scope': 'FACULTY' if mid in self.major_faculty else 'MAJOR'}
                      for mid, m in self.db.majors.items() if m['school_id'] == school_id]
            values += [{'id': fs, 'name': f['faculty_name'] + ' (학부 전체)', 'parent_name': '', 'scope': 'FACULTY',
                        'group': f['faculty_name']} for fs, f in self.faculty_items.items() if f['school_id'] == school_id]
            for v in values:
                v['label'] = f"{v['name']} · {v['parent_name']}" if v['parent_name'] else v['name']
                v.setdefault('group', v['parent_name'] or v['name'])
        elif kind in ('job_fields', 'interest_fields'):
            # 자격증 상세 탐색(자격구분 → 대직무분야 → 중직무분야 → 종목)에도 쓴다. count = 조건에 맞는 종목 수(0이면 제외)
            if qualification_type is not None and qualification_type not in TREND_TYPES:
                raise ValidationError('qualification_type 은 TECHNICAL 또는 PROFESSIONAL')
            pool = [q for q in self.db.qualifications.values()
                    if (qualification_type is None or q['qualgbnm'] == TREND_TYPES[qualification_type])
                    and (kind == 'interest_fields' or interest_field_id is None or q['obligfldcd'] == interest_field_id)]
            key = 'mdobligfldcd' if kind == 'job_fields' else 'obligfldcd'
            catalog = self.job_fields if kind == 'job_fields' else self.interest_fields
            counts = {}
            for q in pool:
                if q[key]:
                    counts[q[key]] = counts.get(q[key], 0) + 1
            values = [{'id': k, 'name': v, 'count': counts[k]} for k, v in catalog.items() if k in counts]
        elif kind == 'qualifications':
            # 자격증 검색: 국가기술(TECHNICAL) / 국가전문(PROFESSIONAL) 분류. 없으면 613종 전체
            if qualification_type is not None and qualification_type not in TREND_TYPES:
                raise ValidationError('qualification_type 은 TECHNICAL 또는 PROFESSIONAL')
            if series is not None and (series not in QUAL_SERIES or qualification_type == 'PROFESSIONAL'):
                raise ValidationError('series 는 국가기술 등급(' + ' / '.join(QUAL_SERIES) + ')')
            values = [{'id': k, 'name': q['name'],
                       'qualification_type': 'TECHNICAL' if q['qualgbnm'] == TREND_TYPES['TECHNICAL'] else 'PROFESSIONAL',
                       'qualification_type_name': q['qualgbnm'],
                       'series': level(q) if q['qualgbnm'] == TREND_TYPES['TECHNICAL'] else None}
                      for k, q in self.db.qualifications.items()
                      if (qualification_type is None or q['qualgbnm'] == TREND_TYPES[qualification_type])
                      and (series is None or (q['qualgbnm'] == TREND_TYPES['TECHNICAL'] and level(q) == series))
                      and (interest_field_id is None or q['obligfldcd'] == interest_field_id)
                      and (job_field_id is None or q['mdobligfldcd'] == job_field_id)]
        elif kind == 'grades':
            values = [{'id': g, 'name': g} for g in GRADES]
        else:
            raise ValidationError('알 수 없는 선택 목록: ' + str(kind))
        q = query.casefold()
        values = sorted((v for v in values if q in v['name'].casefold() or q in v.get('group', '').casefold()),
                        key=lambda v: (v.get('group', ''), v['name'] != v.get('group') and not v['id'].startswith('FS:'), v['name'],
                                       json.dumps(v, sort_keys=True, ensure_ascii=False)))
        for v in values:
            v.pop('group', None)
        return {'kind': kind, 'items': values[offset:offset + limit], 'total': len(values),
                'offset': offset, 'limit': limit}

    # ---------- 추천 ----------
    def recommend(self, profile: dict, today: str | None = None) -> dict:
        """전공 맞춤 추천: 대학교·전공·학년(+보유 자격)만 쓴다. 희망 직무·관심 분야는 받지 않는다.
        점수 = 학과 연결 + 응시 시점 + 올해 남은 시험 + 시작 등급 / 재학 중 응시 불가 등급 감점. 점수순 최대 5개(후보가 적으면 그 수만큼).
        학교 정보가 없으면 결과 없이 needs_school_info=True (화면은 관심 분야·희망 직무 선택 안내).
        학교 정보는 있는데 학과 연결 자격이 없으면 needs_school_info=False, items=[] (화면은 학과 연결 없음 안내).
        today: 'YYYY-MM-DD'(남은 시험 회차 계산 기준). 없으면 오늘 날짜."""
        p = self.validate(profile)
        current = date.fromisoformat(today) if today else date.today()
        if not p['major_id']:
            return {'schema_version': '3', 'qualification_scope': 'QNET_MASTER_613', 'profile': p,
                    'recommendation_scope': 'NONE',
                    'recommended': [], 'owned_excluded_count': 0, 'roadmap': [], 'next_levels': [],
                    'grade_guidance': None, 'exam_window_basis': EXAM_WINDOW_SOURCE,
                    'personal_eligibility_verified': False, 'score_basis_date': current.isoformat(),
                    'needs_school_info': True, 'items': [], 'result_limit': RESULT_LIMIT}
        owned = set(p['owned_qualification_ids'])
        owned_rows = [self.db.qualifications[c] for c in sorted(owned)]
        owned |= self._lower_levels(owned_rows)            # 보유 자격과 그 하위 등급은 추천 제외
        school_type = self.db.schools.get(p.get('school_id') or '', {}).get('school_type', '')
        start = start_level(school_type, p['grade'])

        def item(code, basis):
            q = self.db.qualifications[code]
            return {'jmcd': code, 'qualification_id': 'QNET:' + code, 'name': q['name'], 'basis': basis,
                    'exam_window': grade_exam_window(q, school_type, p['grade'], owned_rows),
                    'personal_eligibility_verified': False, 'card': self._card(code, current)}

        def scored(v, share=1.0):
            q, w, g = self.db.qualifications[v['jmcd']], v['exam_window'], p['grade']
            parts = []
            if v['basis'] in SCORE:
                pts = round(SCORE[v['basis']] * share)
                parts.append(('ACADEMIC_LINK', pts, BASIS_LABEL[v['basis']] + (f' {share:.0%}' if v['basis'] == 'faculty_aggregate' else '')))
            st = w['status']
            if st == 'FROM_GRADE':
                key = 'FROM_GRADE_NEXT' if g.isdigit() and w['from_grade'] - int(g) == 1 else 'FROM_GRADE_LATER'
                parts.append(('EXAM_WINDOW', SCORE[key], f"{w['from_grade']}학년부터 응시 가능"))
            elif w.get('via_owned_qualification'):
                parts.append(('EXAM_WINDOW', SCORE[st], f"보유 {self.db.qualifications[w['via_owned_qualification']]['name']}로 응시 가능"))
            else:
                parts.append(('EXAM_WINDOW', SCORE[st], {'NOW': '지금 학년 응시 가능', 'NO_RESTRICTION': '응시자격 제한 없음',
                              'CHECK_QUALIFICATION': '응시자격 개별 확인', 'AFTER_GRADUATION_EXPERIENCE': '졸업·경력 후 응시'}[st]))
            upcoming = [s for s in self.db.schedules.get(v['jmcd'], []) if s['exam_start_date'] >= current.isoformat()]
            if upcoming:
                parts.append(('UPCOMING_2026', SCORE['UPCOMING_2026'], f"남은 시험 {upcoming[0]['exam_start_date']} {upcoming[0]['exam_stage']}"))
            series = q['seriesnm']
            if q['qualgbnm'] == '국가기술자격' and level(q) == start:
                parts.append(('LEVEL_FIT', SCORE['LEVEL_FIT'], f'지금 학년에 딸 수 있는 시작 등급({start})'))
            if g.isdigit() and series in ('기술사', '기능장'):
                parts.append(('LEVEL_UNFIT', SCORE['LEVEL_UNFIT'], '재학 중에는 응시 불가한 등급(졸업·경력 후)'))
            v['score'] = sum(x[1] for x in parts)
            v['score_breakdown'] = [{'factor': f, 'points': pt, 'reason': r} for f, pt, r in parts if pt]
            return v

        base = {}
        fac = p.get('major_id') if p.get('major_id') in self.faculty_items else self.major_faculty.get(p.get('major_id'))
        if p.get('major_id') and not fac:          # 전공 기준
            scope = 'MAJOR'
            for link in self.db.major_links.get(p['major_id'], []):
                base.setdefault(link['jmcd'], item(link['jmcd'], link['basis']))
            order = {k: i for i, k in enumerate(base)}
            recommended = sorted((scored(v) for k, v in base.items() if k not in owned),
                                 key=lambda v: (-v['score'], order[v['jmcd']]))
        elif fac:                                  # 학부 이름 항목: 소속 전공 추천 합산, 많이 겹치는 순
            scope = 'FACULTY'
            members = sorted(self.db.faculty_members[fac])
            share, first = {}, {}
            for mid in members:
                for link in self.db.major_links.get(mid, []):
                    share[link['jmcd']] = share.get(link['jmcd'], 0) + 1
                    first.setdefault(link['jmcd'], (int(link['link_rank']), link['basis']))
            for code in share:
                base[code] = dict(item(code, 'faculty_aggregate'),
                                  faculty_share={'majors': share[code], 'of': len(members)})
            recommended = sorted((scored(v, share[v['jmcd']] / len(members)) for k, v in base.items() if k not in owned),
                                 key=lambda v: (-v['score'], -share[v['jmcd']], first[v['jmcd']][0], v['jmcd']))
        grade = p['grade']
        return {'schema_version': '3', 'qualification_scope': 'QNET_MASTER_613',
                'profile': p, 'recommendation_scope': scope,
                'recommended': recommended,
                'owned_excluded_count': len(set(base) & owned),
                'roadmap': self._roadmap(recommended, school_type, grade, owned_rows),
                'next_levels': self._next_levels(owned_rows, school_type, grade),
                'grade_guidance': '기초 학습과 시험 과목 탐색' if grade in ('1', '2') else '학습 일정 수립 및 공식 응시요건 별도 확인',
                'exam_window_basis': EXAM_WINDOW_SOURCE, 'personal_eligibility_verified': False,
                'score_basis_date': current.isoformat(),
                'needs_school_info': False,
                'items': recommended[:RESULT_LIMIT], 'result_limit': RESULT_LIMIT}

    def recommend_by_condition(self, conditions: dict, today: str | None = None) -> dict:
        """추천 화면에서 조건을 직접 고른 경우: 자격구분·관심 분야·희망 직무만 쓴다.
        대학교·전공·학년은 받지 않고, 학과 연결·응시 시점·학년 등급 점수(기존 합산)는 넣지 않는다.
        점수 = 희망직무 일치 +30, 관심분야 일치 +20, 올해 남은 시험 +10, 응시 조건 없음 +5.
        동점이면 최근 연도 1차 응시자 많은 순 → 종목코드 순. 보유 자격과 그 하위 등급은 제외(점수 아님)."""
        if not isinstance(conditions, dict) or set(conditions) - CONDITION_KEYS:
            raise ValidationError('조건 추천 입력은 qualification_type · interest_field_ids · job_field_ids 만 받는다')
        p = dict(conditions)
        if p.get('qualification_type') not in (None, *TYPE_CODE):
            raise ValidationError('qualification_type: TECHNICAL 또는 PROFESSIONAL')
        self._check_codes(p, ('job_field_ids', 'interest_field_ids', 'owned_qualification_ids'))
        if not p['job_field_ids'] and not p['interest_field_ids']:
            raise ValidationError('관심 분야 또는 희망 직무를 하나 이상 선택')
        current = date.fromisoformat(today) if today else date.today()
        owned_rows = [self.db.qualifications[c] for c in p['owned_qualification_ids']]
        owned = set(p['owned_qualification_ids']) | self._lower_levels(owned_rows)
        t = p.get('qualification_type')
        year = self._trend_year(current.isoformat())
        items = []
        for code, q in self.db.qualifications.items():
            if code in owned or (t and q['qualgbcd'] != TYPE_CODE[t]):
                continue
            parts = []
            if q['mdobligfldcd'] in p['job_field_ids']:
                parts.append(('JOB_FIELD', SCORE['JOB_FIELD'], '희망직무 일치'))
            if q['obligfldcd'] in p['interest_field_ids']:
                parts.append(('INTEREST_FIELD', SCORE['INTEREST_FIELD'], '관심분야 일치'))
            if not parts:
                continue
            upcoming = [s_ for s_ in self.db.schedules.get(code, []) if s_['exam_start_date'] >= current.isoformat()]
            if upcoming:
                parts.append(('UPCOMING_2026', SCORE['UPCOMING_2026'], f"남은 시험 {upcoming[0]['exam_start_date']} {upcoming[0]['exam_stage']}"))
            if q['eligibility_required'] == 'N':
                parts.append(('NO_ELIGIBILITY_REQUIRED', SCORE['NO_ELIGIBILITY_REQUIRED'], '응시 조건 없음(바로 응시 가능)'))
            popular = self._first_stage_counts(code).get(year, (0,))[0]
            items.append({'jmcd': code, 'qualification_id': 'QNET:' + code, 'name': q['name'],
                          'interest_matches': [{'type': f, 'id': q['mdobligfldcd'] if f == 'JOB_FIELD' else q['obligfldcd']}
                                               for f, _, _ in parts if f in ('JOB_FIELD', 'INTEREST_FIELD')],
                          'score': sum(x[1] for x in parts),
                          'score_breakdown': [{'factor': f, 'points': pt, 'reason': r} for f, pt, r in parts],
                          'recent_examinees': {'year': year, 'count': popular},
                          'eligibility_required': q['eligibility_required'] or None,
                          'card': self._card(code, current)})
        items.sort(key=lambda v: (-v['score'], -v['recent_examinees']['count'], v['jmcd']))
        return {'schema_version': '3', 'mode': 'CONDITION', 'conditions': p, 'items': items[:RESULT_LIMIT],
                'total_candidates': len(items), 'result_limit': RESULT_LIMIT, 'score_basis_date': current.isoformat(),
                'score_rule': '희망직무 +30, 관심분야 +20, 올해 남은 시험 +10, 응시 조건 없음 +5 / 동점: 최근 1차 응시자 많은 순'}

    def _roadmap(self, items, school_type, grade, owned_rows):
        """학년별 로드맵: 추천 종목을 '처음 응시 가능한 학년'에 배치(지금 학년~최종 학년, 이후 졸업 후 / 개별 확인)."""
        if not grade.isdigit():
            return []
        final = 4 if school_type in FOUR_YEAR_TYPES else max(2, int(grade))
        stages = {str(g): [] for g in range(int(grade), final + 1)}
        stages.update(GRADUATED=[], CHECK=[])
        for v in items:
            q = self.db.qualifications[v['jmcd']]
            if v['exam_window']['status'] == 'CHECK_QUALIFICATION':
                key = 'CHECK'
            else:
                key = next((g for g in stages if g.isdigit() and grade_exam_window(q, school_type, g, owned_rows)['status']
                            in ('NOW', 'NO_RESTRICTION')), 'GRADUATED')
            stages[key].append(v)
        label = {'GRADUATED': '졸업 후(실무경력 필요 포함)', 'CHECK': '응시자격 개별 확인'}
        return [{'stage': k, 'label': label.get(k, f'{k}학년'), 'count': len(vs),
                 'items': [{'jmcd': v['jmcd'], 'name': v['name'], 'score': v['score']} for v in vs[:ROADMAP_PER_STAGE]]}
                for k, vs in stages.items() if vs]

    def _ladder_key(self, q):
        """같은 종목 계열의 등급 사다리 (이름 줄기, 등급 순위). 국가기술: 기능사→산업기사→기사→기능장/기술사, 국가전문: 3급→1급."""
        m = re.search(r'(\d)급', q['name'])
        if m:                                               # 1·2·3급 (서비스분야·국가전문)
            return (GRADE_NUM.sub('', q['name']), -int(m.group(1)))
        name, lv = base_name(q), level(q)
        if q['qualgbnm'] == '국가기술자격' and lv in LEVELS and name.endswith(lv):
            return (name[:-len(lv)] + q['name'][len(name):], LEVELS.index(lv))   # 괄호 분야까지 같은 계열
        return None

    def _lower_levels(self, owned_rows):
        """보유 자격보다 낮은 같은 계열 등급(추천에서 제외)."""
        own = [k for k in map(self._ladder_key, owned_rows) if k]
        return {c for c, q in self.db.qualifications.items()
                for k in [self._ladder_key(q)] if k and any(k[0] == o[0] and k[1] < o[1] for o in own)}

    def _next_levels(self, owned_rows, school_type, grade):
        """보유 자격의 상위 등급(같은 이름 줄기). 응시 경로: 학과·보유 자격 기준 exam_window + 실무경력 경로 안내."""
        ladder = {}
        for c, q in self.db.qualifications.items():
            k = self._ladder_key(q)
            if k:
                ladder.setdefault(k[0], []).append((k[1], c))
        owned = {o['jmcd'] for o in owned_rows}
        out = []
        for o in owned_rows:
            k = self._ladder_key(o)
            for rank, c in sorted(ladder.get(k[0], []) if k else []):
                if rank > k[1] and c not in owned:
                    q = self.db.qualifications[c]
                    out.append({'from_jmcd': o['jmcd'], 'from_name': o['name'], 'jmcd': c, 'name': q['name'],
                                'exam_window': grade_exam_window(q, school_type, grade, owned_rows),
                                'experience_route': EXPERIENCE_ROUTE.get(level(q))})
        return out

    # ---------- 트렌드 ----------
    def _first_stage_counts(self, code):
        """연도별 1차 응시자 {year: (examinees, passed, stage)} — 필기 > 1차 > 시험 순으로 그 종목의 첫 단계."""
        by = {}
        for r in self.db.pass_rates.get(code, []):
            if r['exam_stage'] in FIRST_STAGES:
                y = int(r['exam_year']); rank = FIRST_STAGES.index(r['exam_stage'])
                if y not in by or rank < by[y][3]:
                    by[y] = (int(r['examinees']), int(r['passed']), r['exam_stage'], rank)
        return {y: v[:3] for y, v in by.items()}

    def _trend_year(self, today=None):
        current = date.fromisoformat(today) if today else date.today()
        years = {int(r['exam_year']) for rows in self.db.pass_rates.values() for r in rows}
        return max(y for y in years if y < current.year)       # 올해는 집계 중 → 최근 완결 연도

    def _trend_item(self, code, year):
        counts = self._first_stage_counts(code)
        if year not in counts:
            return None
        q = self.db.qualifications[code]
        series = [{'year': y, 'examinees': counts[y][0]} for y in range(year - TREND_YEARS + 1, year + 1) if y in counts]
        base = counts.get(year - TREND_YEARS + 1)
        change = percent(counts[year][0] - base[0], base[0], 1) if base else None
        label = ('NEW_OR_SHORT_HISTORY' if change is None else 'UP' if change > TREND_THRESHOLD
                 else 'DOWN' if change < -TREND_THRESHOLD else 'STEADY')
        n, k, stage = counts[year]
        return {'jmcd': code, 'name': q['name'], 'qualification_type': q['qualgbnm'], 'series': self._series_label(q),
                'interest_field_id': q['obligfldcd'], 'interest_field': q['obligfldnm'],
                'year': year, 'stage': stage, 'examinees': n, 'passed': k, 'pass_rate_percent': percent(k, n),
                'five_year': series, 'five_year_change_percent': change, 'trend': label}

    @staticmethod
    def _series_label(q):
        # 국가기술 등급(DB qualifications.grade_level과 같은 판정). 국가전문은 등급 없음
        return level(q) if q['qualgbnm'] == '국가기술자격' else None

    def trends(self, qualification_type='TECHNICAL', series=None, interest_field_id=None, year=None, limit=20, today=None):
        """최근 완결 연도 응시자 많은 순. qualification_type: TECHNICAL(국가기술) | PROFESSIONAL(국가전문)."""
        if qualification_type not in TREND_TYPES:
            raise ValidationError('qualification_type 은 TECHNICAL 또는 PROFESSIONAL')
        if series is not None and (series not in QUAL_SERIES or qualification_type == 'PROFESSIONAL'):
            raise ValidationError('series 는 국가기술 등급(' + ' / '.join(QUAL_SERIES) + ')')
        if interest_field_id is not None and interest_field_id not in self.interest_fields:
            raise ValidationError('interest_field_id: 유효한 관심분야 코드 필요')
        if type(limit) is not int or not 1 <= limit <= 100:
            raise ValidationError('1 <= limit <= 100 필요')
        year = int(year) if year else self._trend_year(today)
        items = []
        for code, q in self.db.qualifications.items():
            if q['qualgbnm'] != TREND_TYPES[qualification_type]:
                continue
            if series and self._series_label(q) != series:
                continue
            if interest_field_id and q['obligfldcd'] != interest_field_id:
                continue
            it = self._trend_item(code, year)
            if it:
                items.append(it)
        items.sort(key=lambda x: (-x['examinees'], x['jmcd']))
        for i, it in enumerate(items, 1):
            it['rank'] = i
        basis = f'{year}년 1차(필기·1차·단일시험) 실제 응시자 수'
        return {'year': year, 'qualification_type': qualification_type, 'series': series,
                'interest_field_id': interest_field_id, 'basis': basis,
                'total': len(items), 'items': items[:limit],
                'trend_rule': (f'기준연도를 포함한 최근 {TREND_YEARS}개 연도 중 첫해 대비 응시자 증감률이 '
                               f'±{TREND_THRESHOLD:g}% 범위를 벗어나면 UP/DOWN, 범위 안이면 STEADY. '
                               '첫해 자료가 없거나 응시자 수가 0이면 NEW_OR_SHORT_HISTORY')}

    # ---------- 자격증 상세 ----------
    def _card(self, code, current):
        """추천 카드 표시값: 한 줄 소개, 최근 연도 합격률, 다음 접수 회차, 가장 가까운 시험 (상세 화면과 같은 기준)."""
        q = self.db.qualifications[code]
        sch = [dict(r, schedule_id=f'{code}:{i}', **self.schedule_status(r, current))
               for i, r in enumerate(self.db.schedules.get(code, []))]
        active = sorted((s_ for s_ in sch if s_['exam_status'] != 'ENDED'), key=lambda s_: s_['exam_start_date'])
        reg = [s_ for s_ in active if s_['exam_status'] == 'UPCOMING' and s_['registration_status'] != 'CLOSED']
        rates = self.db.pass_rates.get(code, [])
        year = max((int(r['exam_year']) for r in rates), default=None)
        latest = sorted(({'exam_stage': r['exam_stage'], 'examinees': int(r['examinees']), 'passed': int(r['passed']),
                          'pass_rate_percent': percent(int(r['passed']), int(r['examinees']))}
                         for r in rates if int(r['exam_year']) == year), key=lambda r: STAGE_ORDER.get(r['exam_stage'], 99))
        return {'qualification_type_name': q['qualgbnm'], 'series': self._series_label(q),
                'field': q['mdobligfldnm'] or q['obligfldnm'], 'summary': q['summary'],
                'pass_rate_latest': {'exam_year': year, 'items': latest} if latest else None,
                'next_registration': reg[0] if reg else None, 'nearest_exam': active[0] if active else None}

    @staticmethod
    def schedule_status(r: dict, current: date) -> dict:
        """기준일 대비 일정 상태. 지난 시험은 응시 불가 → ENDED, D-Day는 예정 시험만."""
        today = current.isoformat()
        start, end = r['exam_start_date'], r['exam_end_date']
        if end < today:
            st, label = 'ENDED', '종료'
        elif start <= today:
            st, label = 'IN_PROGRESS', f'시험 기간 중(~{end})'
        else:
            st, label = 'UPCOMING', f"D-{(date.fromisoformat(start) - current).days}"
        rs, re_ = r.get('registration_start_date', ''), r.get('registration_end_date', '')
        vs, ve = r.get('vacancy_registration_start_date', ''), r.get('vacancy_registration_end_date', '')
        if not rs or not re_:
            reg = 'UNKNOWN'
        elif today < rs:
            reg = 'NOT_OPEN'
        elif today <= re_:
            reg = 'OPEN'
        elif vs and today < vs:
            reg = 'VACANCY_NOT_OPEN'        # 정기 접수 끝, 빈자리 접수 전
        elif vs and today <= ve:
            reg = 'VACANCY_OPEN'            # 빈자리 접수 중
        else:
            reg = 'CLOSED'
        return {'exam_status': st, 'dday_label': label, 'registration_status': reg}

    def qualification_detail(self, jmcd: str, today: str | None = None) -> dict:
        code = str(jmcd).strip().upper().removeprefix('QNET:').zfill(4)
        q = self.db.qualifications.get(code)
        if not q:
            raise NotFoundError('Q-Net 613종 원장에 없는 종목: ' + str(jmcd))
        current = date.fromisoformat(today) if today else date.today()
        schedules = [dict(r, schedule_id=f'{code}:{i}', **self.schedule_status(r, current))
                     for i, r in enumerate(self.db.schedules.get(code, []))]
        upcoming = [s_ for s_ in schedules if s_['exam_status'] in ('UPCOMING', 'IN_PROGRESS')]
        # 다음 회차 = 아직 접수할 수 있는(접수 전·접수 중) 가장 가까운 예정 시험
        applicable = sorted((s_ for s_ in schedules if s_['exam_status'] == 'UPCOMING' and s_['registration_status'] != 'CLOSED'),
                            key=lambda s_: s_['exam_start_date'])
        rates = []
        for r in self.db.pass_rates.get(code, []):
            n, k = int(r['examinees']), int(r['passed'])
            a = int(r['applicants']) if r.get('applicants') else None
            rates.append({'exam_year': int(r['exam_year']), 'exam_stage': r['exam_stage'], 'applicants': a, 'examinees': n, 'passed': k,
                          'attendance_rate_percent': percent(n, a, 1),
                          'pass_rate_percent': percent(k, n),
                          'source_type': r['source_type'], 'source_url': r['source_url']})
        rates.sort(key=lambda r: (r['exam_year'], STAGE_ORDER.get(r['exam_stage'], 99), r['exam_stage']))
        # 화면 순서: 진행 중·예정 회차를 시험일 가까운 순으로 먼저, 끝난 회차는 최근 것부터 뒤에
        active = sorted((s_ for s_ in schedules if s_['exam_status'] != 'ENDED'), key=lambda s_: s_['exam_start_date'])
        ended = sorted((s_ for s_ in schedules if s_['exam_status'] == 'ENDED'),
                       key=lambda s_: s_['exam_start_date'], reverse=True)
        schedules = active + ended
        by_id = {s_['schedule_id']: s_ for s_ in schedules}
        latest_year = max((r['exam_year'] for r in rates), default=None)
        rounds = {}                      # 화면용: 같은 회차의 필기·실기(1차·2차…)를 한 줄로
        for s_ in sorted(schedules, key=lambda s_: (s_['exam_start_date'], STAGE_ORDER.get(s_['exam_stage'], 99))):
            rounds.setdefault(s_['exam_round'], []).append(s_['schedule_id'])
        rules = self.db.eligibility_rules.get(q['eligibility_rule_key'], [])
        eligibility = {'required': {'Y': True, 'N': False}.get(q['eligibility_required']), 'summary': q['eligibility_summary'],
                       'conditions': [{'type': r['condition_type'], 'text': r['condition_text']} for r in rules],
                       'note': ('조건 중 하나만 충족하면 응시 가능. 관련학과·실무경력 인정 범위는 큐넷 응시자격 자가진단으로 확인'
                                if rules else ''),
                       'source_url': 'https://www.q-net.or.kr/crf006.do?id=crf00603',
                       'personal_eligibility_verified': False}
        return {'jmcd': code, 'qualification_id': 'QNET:' + code, 'qualification': q, 'series': self._series_label(q),
                'eligibility': eligibility,
                'schedule_rounds': [{'exam_round': k, 'schedule_ids': v} for k, v in rounds.items()],
                'pass_rate_years': sorted({r['exam_year'] for r in rates}, reverse=True),
                'schedules_2026': schedules, 'schedule_status': 'AVAILABLE' if schedules else 'NOT_ACQUIRED',
                'schedule_basis_date': current.isoformat(),
                'next_schedule_id': applicable[0]['schedule_id'] if applicable else None,
                'next_registration': by_id[applicable[0]['schedule_id']] if applicable else None,
                'nearest_exam': active[0] if active else None,
                'remaining_2026_count': len(upcoming),
                'applicable_2026_count': len(applicable),
                'remaining_2026_note': ('' if applicable or not schedules else
                                        '2026년 접수 가능한 남은 회차 없음(다음 연도 일정 공고 전)'),
                'pass_rate_latest': ({'exam_year': latest_year,
                                      'items': [r for r in rates if r['exam_year'] == latest_year]} if rates else None),
                'pass_rates': rates, 'pass_rate_status': 'AVAILABLE' if rates else 'NOT_ACQUIRED',
                'trend': self._detail_trend(code, q, current),
                'pass_rate_definition': '합격자수 / 실제 응시자수 × 100 (접수자 아님)'}

    def _detail_trend(self, code, q, current):
        """자격증 상세용: 이 종목의 최근 5년 흐름 + 같은 유형(국가기술/국가전문) 안 순위."""
        year = self._trend_year(current.isoformat())
        it = self._trend_item(code, year)
        if not it:
            return None
        t_ = 'TECHNICAL' if q['qualgbnm'] == TREND_TYPES['TECHNICAL'] else 'PROFESSIONAL'
        ranked = self.trends(t_, year=year, limit=100)
        same = [c for c, qq in self.db.qualifications.items() if qq['qualgbnm'] == q['qualgbnm']]
        rank = 1 + sum(1 for c in same if (self._trend_item(c, year) or {}).get('examinees', -1) > it['examinees'])
        return dict(it, rank_in_type=rank, type_total=ranked['total'])

    # ---------- 오늘의 사자성어 ----------
    def daily_idiom(self, today: str | None = None) -> dict | None:
        """한국시간 오늘 날짜의 사자성어. 기준일(IDIOM_CYCLE_START)에 1번, 그다음 날 2번 … 순서대로 하루 1개씩,
        목록 길이(50)를 주기로 돈다. 로그인·회원·화면과 관계없이 같은 날짜면 같은 결과. 목록이 비면 None.
        today: 'YYYY-MM-DD'(한국시간). 없으면 오늘 날짜."""
        items = self.db.daily_idioms
        if not items:
            return None
        current = date.fromisoformat(today) if today else date.today()
        index = (current - IDIOM_CYCLE_START).days % len(items)
        r = items[index]
        return {'date': current.isoformat(), 'display_order': int(r['display_order']), 'idiom': r['idiom'],
                'hanja': r['hanja'], 'meaning': r['meaning'], 'cycle_days': len(items)}

    # ---------- D-Day ----------
    def dday(self, schedule_id: str, today: str | None = None, exam_date: str | None = None) -> dict:
        code, _, idx = str(schedule_id).partition(':')
        rows = self.db.schedules.get(code, [])
        if not idx.isdigit() or int(idx) >= len(rows):
            raise NotFoundError('schedule_id 는 qualification_detail 의 schedules_2026[].schedule_id 값이어야 함')
        r = rows[int(idx)]
        start, end = date.fromisoformat(r['exam_start_date']), date.fromisoformat(r['exam_end_date'])
        if not exam_date and start != end:
            raise ValidationError('시험 기간이 여러 날이면 실제 응시일(exam_date)을 지정해야 함')
        chosen = date.fromisoformat(exam_date) if exam_date else start
        if not start <= chosen <= end:
            raise ValidationError('응시일이 시험 기간 밖')
        current = date.fromisoformat(today) if today else date.today()
        days = (chosen - current).days
        return {'schedule_id': schedule_id, 'jmcd': code, 'exam_stage': r['exam_stage'], 'exam_date': chosen.isoformat(),
                'today': current.isoformat(), 'days_remaining': days,
                'label': f'D-{days}' if days > 0 else 'D-Day' if days == 0 else '종료',
                'state': 'UPCOMING' if days > 0 else 'TODAY' if days == 0 else 'ENDED'}
