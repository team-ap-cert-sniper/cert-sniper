import json, unittest
from pathlib import Path
from jeogyeokjeung import JeogyeokjeungService, NotFoundError, ValidationError
from jeogyeokjeung.eligibility import grade_exam_window

ROOT = Path(__file__).resolve().parents[1]
# 기준 프로필: 백석대학교 첨단IT학부 빅데이터전공 4학년 (전공 맞춤 추천은 학교·학과·학년만 받음)
EXAMPLE = {'school_id': '084d0e1ab9bcfde8', 'major_id': '298669b03b67514f',
           'grade': '4', 'owned_qualification_ids': []}


class Service(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.s = JeogyeokjeungService()

    def test_major_path_and_no_school_info(self):
        major = self.s.recommend(EXAMPLE, today='2026-11-18')                       # ① 전공 맞춤
        self.assertTrue(major['recommended']); self.assertFalse(major['needs_school_info'])
        none = self.s.recommend({}, today='2026-11-18')                             # 학교 정보 없음 → 안내
        self.assertTrue(none['needs_school_info']); self.assertEqual((none['recommended'], none['items']), ([], []))
        self.assertTrue(all(b['factor'] not in ('JOB_FIELD', 'INTEREST_FIELD') for x in major['recommended'] for b in x['score_breakdown']))
        card = major['recommended'][0]['card']                                       # 카드 값 = 상세 화면 기준과 같음
        d = self.s.qualification_detail(major['recommended'][0]['jmcd'], today='2026-11-18')
        self.assertEqual(card['pass_rate_latest']['exam_year'], d['pass_rate_latest']['exam_year'])
        self.assertEqual((card['next_registration'] or {}).get('schedule_id'), d['next_schedule_id'])
        self.assertEqual((card['nearest_exam'] or {}).get('schedule_id'), (d['nearest_exam'] or {}).get('schedule_id'))
        self.assertTrue(card['summary'])

    def test_detail_display_order(self):
        d = self.s.qualification_detail('7910', today='2026-11-18')      # 한식조리기능사
        st = [x['exam_status'] for x in d['schedules_2026']]
        self.assertEqual(st, sorted(st, key=lambda v: v == 'ENDED'))      # 진행 중·예정 먼저, 종료는 뒤
        ended = [x['exam_start_date'] for x in d['schedules_2026'] if x['exam_status'] == 'ENDED']
        self.assertEqual(ended, sorted(ended, reverse=True))               # 종료는 최근 회차부터
        self.assertEqual(d['next_registration']['schedule_id'], d['next_schedule_id'])
        self.assertNotEqual(d['nearest_exam']['exam_status'], 'ENDED')
        self.assertEqual(d['pass_rate_latest']['exam_year'], 2025)
        self.assertTrue(all(r['exam_year'] == 2025 for r in d['pass_rate_latest']['items']))
        late = self.s.qualification_detail('7910', today='2027-01-15')    # 다음 연도 일정 공고 전
        self.assertIsNone(late['next_registration']); self.assertIsNone(late['nearest_exam'])

    def test_qualification_texts(self):
        q = self.s.qualification_detail('1320')['qualification']
        self.assertIn('소프트웨어', q['overview'])
        self.assertIn('소프트웨어', q['career_outlook'])
        self.assertEqual(sum(1 for r in self.s.db.qualifications.values() if r['overview']), 391)
        rows = self.s.db.qualifications.values()
        self.assertTrue(all(r['summary'].endswith('이에요.') for r in rows))       # 613종 모두 한 줄 소개
        self.assertEqual(sum(1 for r in rows if r['summary_basis'] == 'QNET_TEXT'), 389)

    def test_example_profile(self):
        r = self.s.recommend(EXAMPLE)
        json.dumps(r, ensure_ascii=False)  # JSON 직렬화 가능
        codes = [x['jmcd'] for x in r['recommended']]
        self.assertEqual(codes, ['1320', '9521', '2290'])  # 학과 연결 점수순: 정보처리기사 → 사회조사분석사2급 → 동계열 정보처리산업기사
        self.assertEqual(r['recommended'][2]['basis'], 'same_subfield')
        self.assertEqual(r['major_discipline'], '빅데이터·AI')

    def test_output_contract_keys(self):
        r = self.s.recommend(EXAMPLE)
        self.assertEqual(set(r), {'schema_version', 'qualification_scope', 'profile', 'recommendation_scope', 'major_discipline', 'major_category', 'recommended',
                                  'owned_excluded_count', 'grade_guidance', 'exam_window_basis',
                                  'personal_eligibility_verified', 'needs_school_info', 'score_basis_date',
                                  'roadmap', 'next_levels', 'items', 'result_limit'})
        self.assertEqual(set(self.s.recommend({})), set(r))                        # 학교 정보 없음도 같은 키
        self.assertEqual(r['result_limit'], 5)
        self.assertEqual(len(r['items']), min(5, len(r['recommended'])))
        self.assertEqual([x['jmcd'] for x in r['items']], [x['jmcd'] for x in r['recommended'][:5]])
        self.assertEqual(set(r['recommended'][0]), {'jmcd', 'qualification_id', 'name', 'basis', 'exam_window',
                                                    'personal_eligibility_verified', 'score', 'score_breakdown', 'card'})

    def test_owned_qualifications_are_excluded(self):
        r = self.s.recommend(dict(EXAMPLE, owned_qualification_ids=['1320']))
        self.assertNotIn('1320', [x['jmcd'] for x in r['recommended']])
        self.assertNotIn('2290', [x['jmcd'] for x in r['recommended']])   # 하위 등급 정보처리산업기사도 제외
        self.assertEqual(r['owned_excluded_count'], 2)

    def test_roadmap_next_levels_and_eligibility(self):
        # 학년별 로드맵: 4년제 2학년 → 산업기사는 3학년, 기사는 4학년, 기술사는 졸업 후
        r = self.s.recommend(dict(EXAMPLE, grade='2'), today='2026-11-11')
        stage = {x['jmcd']: s['stage'] for s in r['roadmap'] for x in s['items']}
        self.assertEqual((stage.get('2290'), stage.get('1320')), ('3', '4'))
        self.assertEqual(sum(s['count'] for s in r['roadmap']), len(r['recommended']))
        self.assertEqual(self.s.recommend(dict(EXAMPLE, grade='GRADUATED'))['roadmap'], [])
        # 보유 자격 상위 등급: 전기기능사 → 전기산업기사·전기기사·전기기능장, 3급 청소년지도사 → 2급·1급
        nxt = self.s.recommend(dict(EXAMPLE, owned_qualification_ids=['7780']))['next_levels']
        self.assertEqual([x['name'] for x in nxt], ['전기산업기사', '전기기사', '전기기능장'])
        self.assertIn('실무 1년', nxt[0]['experience_route'])
        self.assertEqual([x['name'] for x in self.s.recommend(dict(EXAMPLE, owned_qualification_ids=['9752']))['next_levels']],
                         ['2급 청소년지도사', '1급 청소년지도사'])
        # 보유 기사 → 같은 직무분야(정보기술) 다른 기사·산업기사는 학년 무관 바로 응시
        from jeogyeokjeung.eligibility import grade_exam_window as gew
        Q = self.s.db.qualifications
        w = gew(Q['1998'], '대학교', '1', [Q['1320']])
        self.assertEqual((w['status'], w['via_owned_qualification']), ('NOW', '1320'))
        # 유사 직무분야([별표 11의2]): 전기기사 → 소방설비산업기사(전기분야, 안전관리)·건축기사 가능 / 한식조리산업기사 → 전기기사 불가
        from jeogyeokjeung.eligibility import owned_route
        Q = self.s.db.qualifications
        self.assertEqual(owned_route(Q['2910'], [Q['1150']]), '1150')
        self.assertEqual(owned_route(Q['1630'], [Q['1150']]), '1150')                      # 건축기사: 건설 ↔ 전기·전자 유사
        self.assertIsNone(owned_route(Q['1150'], [q for q in Q.values() if q['name'] == '한식조리산업기사']))
        # 응시자격: 국가전문 '제한 없음' / 서비스분야 2급·1급 / 괄호 붙은 산업기사
        from jeogyeokjeung.eligibility import grade_exam_window, level
        Q = self.s.db.qualifications
        self.assertEqual(grade_exam_window(Q['9630'], '대학교', '1')['status'], 'NO_RESTRICTION')       # 공인중개사
        self.assertEqual(grade_exam_window(Q['9738'], '대학교', '4')['status'], 'CHECK_QUALIFICATION')  # 사회복지사 1급
        self.assertEqual(grade_exam_window(Q['9510'], '대학교', '4')['status'], 'AFTER_GRADUATION_EXPERIENCE')
        self.assertEqual(level(Q['2900']), '산업기사')                                                  # 소방설비산업기사(기계분야)
        # 국가전문 직무분야 보완: 관심분야 필터에 잡힘(영업·판매 → 공인중개사)
        self.assertEqual(Q['9630']['field_source'], 'MAPPED')
        # 관련부처(Q-Net crf005): 613종 전부. 영문명·수수료·응시 조건
        self.assertEqual(self.s.qualification_detail('9630')['qualification']['related_ministry'], '국토교통부')
        self.assertEqual(sum(1 for q in Q.values() if q['related_ministry']), 613)
        self.assertEqual((Q['1320']['related_ministry'], Q['1320']['eng_name'], Q['1320']['fee_written']),
                         ('과학기술정보통신부', 'Engineer Information Processing', '19400'))
        self.assertEqual([Q[c]['eligibility_required'] for c in ('7910', '1320', '9630', '9738', '9511', '9510')],
                         ['N', 'Y', 'N', 'Y', 'N', 'Y'])                  # 한식조리기능사·정보처리기사·공인중개사·사회복지사1급·직업상담사2·1급
        self.assertEqual(self.s.trends('PROFESSIONAL', interest_field_id='10', limit=1, today='2026-11-11')['items'][0]['name'], '공인중개사')

    def test_golden_cases(self):
        g = json.loads((ROOT / 'tests/golden_recommendations.json').read_text(encoding='utf-8'))
        for c in g['cases']:
            r = self.s.recommend(c['profile'], today=g['today'])
            self.assertEqual([[x['jmcd'], x['basis'], x['exam_window']['status'], x['score']] for x in r['recommended']],
                             c['recommended'], c['profile']['major_id'])

    def test_not_found(self):
        for call in (lambda: self.s.qualification_detail('9999'), lambda: self.s.dday('1320:999')):
            with self.assertRaises(NotFoundError):
                call()

    def test_validation_errors(self):
        bad = [dict(EXAMPLE, grade='7'), dict(EXAMPLE, unknown=1), dict(EXAMPLE, major_id='nope'),
               dict(EXAMPLE, school_id=next(k for k in self.s.db.schools if k != EXAMPLE['school_id'])),
               dict(EXAMPLE, interest_field_ids=['21']), dict(EXAMPLE, job_field_ids=['211']),   # 직무·관심은 조건 추천에서만
               {'grade': '3'}, {'school_id': EXAMPLE['school_id'], 'major_id': EXAMPLE['major_id']},  # 대학교·전공·학년 묶음
               dict(EXAMPLE, owned_qualification_ids=['1320', '1320'])]
        for p in bad:
            with self.assertRaises(ValidationError, msg=str(p)):
                self.s.recommend(p)

    def test_merged_major_id_is_accepted(self):
        rep = next(r for r in self.s.db.majors.values() if r['merged_major_ids'])
        old = rep['merged_major_ids'].split(';')[0]
        r = self.s.recommend({'school_id': rep['school_id'], 'major_id': old, 'grade': '3'})
        self.assertEqual(r['profile']['major_id'], rep['major_id'])
        names = [i['name'] for i in self.s.options('majors', school_id=rep['school_id'], limit=100)['items']]
        self.assertLessEqual(names.count(rep['major_name']), len({(m['college_name'], m['parent_name']) for m in self.s.db.majors.values() if m['school_id'] == rep['school_id'] and m['major_name'] == rep['major_name']}))

    def test_same_subfield_bundle(self):
        # 같은 소계열 학과는 동계열 묶음 자격증을 모두 가진다
        import json
        from pathlib import Path
        sets = json.loads((ROOT / 'data/MANIFEST.json').read_text(encoding='utf-8'))['same_subfield']['sets']
        key = '공학계열/건설/토목공학'
        members = [m for m in self.s.db.majors.values()
                   if (m['category_large'], m['category_mid'], m['category_small']) == tuple(key.split('/'))]
        self.assertGreater(len(members), 5)
        for m in members[:50]:
            codes = {l['jmcd'] for l in self.s.db.major_links[m['major_id']]}
            self.assertTrue(set(sets[key]) <= codes, m['major_name'])
        self.assertNotIn('인문사회계열/사회과학/군사・국방・안보', sets)
        r = self.s.recommend({'school_id': members[0]['school_id'], 'major_id': members[0]['major_id'], 'grade': '3'})
        self.assertEqual(r['major_category']['small'], '토목공학')

    def test_faculty_name_major_aggregates_members(self):
        # 대학교 → 전공 흐름: 전공 목록의 학부 이름 항목(백석대 첨단IT학부)을 고르면 소속 전공 5개 합산
        items = self.s.options('majors', school_id=EXAMPLE['school_id'], query='첨단IT학부')['items']
        # 학부 이름으로 검색하면 학부 항목이 먼저, 그 아래 소속 전공(라벨 '전공 · 학부')
        self.assertEqual([(i['label'], i['scope']) for i in items],
                         [('첨단IT학부', 'FACULTY'), ('AR·VR전공 · 첨단IT학부', 'MAJOR'), ('IoT전공 · 첨단IT학부', 'MAJOR'),
                          ('빅데이터전공 · 첨단IT학부', 'MAJOR'), ('핀테크전공 · 첨단IT학부', 'MAJOR')])
        self.assertEqual([i['name'] for i in self.s.options('majors', school_id=EXAMPLE['school_id'], query='경상')['items']],
                         ['경상학부', '경영학', '회계학'])
        r = self.s.recommend({'school_id': EXAMPLE['school_id'], 'major_id': items[0]['id'], 'grade': '2'})
        self.assertEqual(r['recommendation_scope'], 'FACULTY')
        first = r['recommended'][0]
        self.assertEqual((first['basis'], first['faculty_share']), ('faculty_aggregate', {'majors': 5, 'of': 5}))
        scores = [x['score'] for x in r['recommended']]
        self.assertEqual(scores, sorted(scores, reverse=True))
        fs = self.s.major_faculty[items[0]['id']]
        union = {l['jmcd'] for m in self.s.db.faculty_members[fs] for l in self.s.db.major_links[m]}
        self.assertEqual({x['jmcd'] for x in r['recommended']}, union)
        self.assertEqual(self.s.recommend(EXAMPLE)['recommendation_scope'], 'MAJOR')   # 일반 전공은 전공 기준

    def test_major_options_cover_faculties_and_label_duplicates(self):
        # 학부 이름이 전공 목록에 없는 학부 24개 → '(학부 전체)' 항목으로 들어가 합산 추천
        self.assertEqual(len(self.s.faculty_items), 24)
        fs, f = next((k, v) for k, v in self.s.faculty_items.items() if v['faculty_name'] == '전기컴퓨터공학부')
        names = [i['name'] for i in self.s.options('majors', school_id=f['school_id'], query='전기컴퓨터', limit=100)['items']]
        self.assertIn('전기컴퓨터공학부 (학부 전체)', names)
        r = self.s.recommend({'school_id': f['school_id'], 'major_id': fs, 'grade': '1'})
        self.assertEqual(r['recommendation_scope'], 'FACULTY')
        self.assertTrue(r['recommended'])
        # 같은 학교 동명 전공은 소속 학부로 구분(금오공대 스마트모빌리티전공)
        sid = next(k for k, v in self.s.db.schools.items() if v['school_name'] == '국립금오공과대학교')
        labels = sorted(i['label'] for i in self.s.options('majors', school_id=sid, query='스마트모빌리티', limit=100)['items'])
        self.assertEqual(labels, ['스마트모빌리티전공 · 기계공학부', '스마트모빌리티전공 · 기계시스템공학부'])
        with self.assertRaises(ValidationError):   # 다른 학교 전공
            self.s.recommend({'school_id': sid, 'major_id': EXAMPLE['major_id'], 'grade': '1'})

    def test_score_ranking(self):
        r = self.s.recommend(EXAMPLE, today='2026-11-11')
        scores = [x['score'] for x in r['recommended']]
        self.assertEqual(scores, sorted(scores, reverse=True))
        for x in r['recommended']:
            self.assertEqual(x['score'], sum(b['points'] for b in x['score_breakdown']))
        top = r['recommended'][0]
        self.assertEqual(top['name'], '정보처리기사')
        self.assertTrue({b['factor'] for b in top['score_breakdown']} >= {'ACADEMIC_LINK', 'EXAM_WINDOW'})
        # 남은 시험 회차: 2026-11-11 기준 이후 시험이 있으면 +10, 연말이면 없음
        late = self.s.recommend(EXAMPLE, today='2026-12-31')
        self.assertFalse(any(b['factor'] == 'UPCOMING_2026' for x in late['recommended'] for b in x['score_breakdown']))
        self.assertEqual(r['score_basis_date'], '2026-11-11')
        # 재학생에게 기술사·기능장은 감점되어 같은 학과의 기사·산업기사보다 뒤
        for m in list(self.s.db.majors.values())[:3000]:
            rr = self.s.recommend({'school_id': m['school_id'], 'major_id': m['major_id'], 'grade': '3'}, today='2026-11-11')
            names = [(self.s.db.qualifications[x['jmcd']]['seriesnm'], x['score']) for x in rr['recommended']]
            pro = [sc for se, sc in names if se in ('기술사', '기능장')]
            eng = [sc for se, sc in names if se in ('기사', '기능사')]
            if pro and eng:
                self.assertLess(max(pro), max(eng), m['major_name'])

    def test_schedule_status_by_today(self):
        d = self.s.qualification_detail('1320', today='2026-11-11')
        st = [(x['exam_stage'], x['exam_status'], x['dday_label']) for x in d['schedules_2026']]
        self.assertEqual(st[0][1], 'IN_PROGRESS')                                   # 3회 실기 10-24~11-13 진행 중이 맨 앞
        self.assertTrue(all(x[1] == 'ENDED' and x[2] == '종료' for x in st[1:]))    # 지난 시험은 뒤에, 종료 표시
        self.assertIsNone(d['next_schedule_id'])                                    # 진행 중 회차는 접수 마감 → 새로 응시할 회차 없음
        self.assertIn('접수 가능한 남은 회차 없음', d['remaining_2026_note'])
        k = self.s.qualification_detail('7910', today='2026-11-11')                 # 한식조리기능사: 접수 중 회차가 다음 회차
        nxt = next(x for x in k['schedules_2026'] if x['schedule_id'] == k['next_schedule_id'])
        self.assertEqual(nxt['exam_status'], 'UPCOMING'); self.assertNotEqual(nxt['registration_status'], 'CLOSED')
        early = self.s.qualification_detail('1320', today='2026-01-01')
        self.assertEqual(early['schedules_2026'][0]['dday_label'], 'D-29')
        self.assertEqual(early['schedules_2026'][0]['registration_status'], 'NOT_OPEN')
        past = self.s.dday(d['schedules_2026'][-1]['schedule_id'], today='2026-11-11', exam_date='2026-01-30')   # 맨 뒤 = 가장 오래된 1회 필기
        self.assertEqual((past['label'], past['state']), ('종료', 'ENDED'))

    def test_trends_match_official_yearbook(self):
        # 2026 국가기술자격 통계연보(2025년 기준) 응시 상위 종목과 수치 일치(컴퓨터활용능력은 대한상의 시행이라 613 밖)
        r = self.s.trends('TECHNICAL', limit=5, today='2026-11-11')
        self.assertEqual(r['year'], 2025)
        self.assertEqual([(x['name'], x['examinees']) for x in r['items']],
                         [('지게차운전기능사', 110316), ('산업안전기사', 90265), ('전기기능사', 67010),
                          ('정보처리기사', 66308), ('전기기사', 64080)])
        self.assertEqual([x['name'] for x in self.s.trends('TECHNICAL', series='기사', limit=2, today='2026-11-11')['items']],
                         ['산업안전기사', '정보처리기사'])
        self.assertEqual([x['name'] for x in self.s.trends('TECHNICAL', series='산업기사', limit=2, today='2026-11-11')['items']],
                         ['산업안전산업기사', '전기산업기사'])
        p = self.s.trends('PROFESSIONAL', limit=3, today='2026-11-11')
        self.assertTrue(all(x['qualification_type'] == '국가전문자격' for x in p['items']))
        self.assertEqual(p['items'][0]['name'], '공인중개사')
        f = self.s.trends('TECHNICAL', interest_field_id='21', limit=50, today='2026-11-11')
        self.assertTrue(all(x['interest_field_id'] == '21' for x in f['items']))
        ex = [x['examinees'] for x in f['items']]
        self.assertEqual(ex, sorted(ex, reverse=True))
        top = r['items'][0]
        self.assertEqual([y['year'] for y in top['five_year']], [2021, 2022, 2023, 2024, 2025])
        self.assertIn(top['trend'], ('UP', 'DOWN', 'STEADY'))
        welding = self.s.qualification_detail('0011', today='2026-11-11')['trend']
        self.assertEqual(welding['five_year_change_percent'], -20.9)
        self.assertEqual(welding['trend'], 'DOWN')
        self.assertIn('최근 5개 연도 중 첫해', r['trend_rule'])
        d = self.s.qualification_detail('1320', today='2026-11-11')['trend']
        self.assertEqual((d['rank_in_type'], d['examinees'], d['trend']), (4, 66308, 'UP'))
        self.assertFalse(r['partial'])
        cur = self.s.trends('PROFESSIONAL', year=2026, today='2026-11-11')   # 올해 누적(참고용)
        self.assertTrue(cur['partial'])
        self.assertTrue(all(x['year'] == 2026 for x in cur['items']))
        with self.assertRaises(ValidationError):
            self.s.trends('OTHER')

    def test_needs_school_info_without_school(self):
        r = self.s.recommend({'owned_qualification_ids': ['1320']})
        self.assertTrue(r['needs_school_info'])
        self.assertEqual(r['recommended'], [])

    def test_major_without_links_returns_empty_items(self):
        db = self.s.db
        mid = next(m for m, row in sorted(db.majors.items())
                   if row['school_id'] and not db.major_links.get(m) and not self.s.major_faculty.get(m) and m not in self.s.faculty_items)
        r = self.s.recommend({'school_id': db.majors[mid]['school_id'], 'major_id': mid, 'grade': '2'})
        self.assertFalse(r['needs_school_info'])
        self.assertEqual((r['recommended'], r['items']), ([], []))

    def test_options(self):
        o = self.s.options('schools', query='백석대학교')
        self.assertTrue(any(i['id'] == EXAMPLE['school_id'] for i in o['items']))
        m = self.s.options('majors', school_id=EXAMPLE['school_id'], limit=100)
        self.assertIn(EXAMPLE['major_id'], [i['id'] for i in m['items']])
        self.assertEqual(self.s.options('qualifications', limit=1)['total'], 613)
        # 자격증 검색 분류: 국가기술 513 / 국가전문 100, 항목에 구분·등급 표시
        tech = self.s.options('qualifications', qualification_type='TECHNICAL', query='정보처리', limit=100)
        self.assertEqual(self.s.options('qualifications', qualification_type='TECHNICAL', limit=1)['total'], 513)
        self.assertEqual(self.s.options('qualifications', qualification_type='PROFESSIONAL', limit=1)['total'], 100)
        self.assertIn({'id': '1320', 'name': '정보처리기사', 'qualification_type': 'TECHNICAL',
                       'qualification_type_name': '국가기술자격', 'series': '기사'}, tech['items'])
        pro = self.s.options('qualifications', qualification_type='PROFESSIONAL', query='공인중개사')['items'][0]
        self.assertEqual((pro['qualification_type'], pro['series']), ('PROFESSIONAL', None))
        # 등급 필터(국가기술): 기사 109 / 산업기사 117, 정보처리 + 기사 → 정보처리기사만
        self.assertEqual(self.s.options('qualifications', series='기사', limit=1)['total'], 109)
        self.assertEqual(self.s.options('qualifications', qualification_type='TECHNICAL', series='산업기사', limit=1)['total'], 117)
        self.assertEqual([i['name'] for i in self.s.options('qualifications', series='기사', query='정보처리')['items']], ['정보처리기사'])
        for bad in [dict(qualification_type='OTHER'), dict(series='1급'), dict(qualification_type='PROFESSIONAL', series='기사')]:
            with self.assertRaises(ValidationError):
                self.s.options('qualifications', **bad)
        with self.assertRaises(ValidationError):
            self.s.options('majors')
        with self.assertRaises(ValidationError):
            self.s.options('schools', limit=101)

    def test_qualification_detail_and_dday(self):
        d = self.s.qualification_detail('QNET:1320')
        self.assertEqual(d['qualification']['name'], '정보처리기사')
        self.assertEqual(d['schedule_status'], 'AVAILABLE')
        self.assertTrue(all(r['examinees'] > 0 for r in d['pass_rates']))
        sid = d['schedules_2026'][0]['schedule_id']
        s = d['schedules_2026'][0]
        r = self.s.dday(sid, today='2026-01-01', exam_date=s['exam_start_date'])
        self.assertTrue(r['label'].startswith('D-'))
        with self.assertRaises(ValidationError):
            self.s.qualification_detail('0000')
        with self.assertRaises(ValidationError):
            self.s.dday('1320:9999')

    def test_exam_window_rules(self):
        q = lambda name, series, gb='국가기술자격': {'name': name, 'seriesnm': series, 'qualgbnm': gb}
        self.assertEqual(grade_exam_window(q('컴퓨터활용능력', '기능사'), '대학교', '1')['status'], 'NO_RESTRICTION')
        self.assertEqual(grade_exam_window(q('정보처리산업기사', '기사'), '대학교', '3'), {'status': 'NOW', 'from_grade': 3, 'related_major_required': True})
        self.assertEqual(grade_exam_window(q('정보처리산업기사', '기사'), '대학교', '2')['status'], 'FROM_GRADE')
        self.assertEqual(grade_exam_window(q('정보처리기사', '기사'), '대학교', '3'), {'status': 'FROM_GRADE', 'from_grade': 4, 'related_major_required': True})
        self.assertEqual(grade_exam_window(q('정보처리기사', '기사'), '전문대학', '2')['status'], 'AFTER_GRADUATION_EXPERIENCE')
        self.assertEqual(grade_exam_window(q('정보처리산업기사', '기사'), '전문대학', '2')['status'], 'NOW')
        self.assertEqual(grade_exam_window(q('정보관리기술사', '기술사'), '대학교', '4')['status'], 'AFTER_GRADUATION_EXPERIENCE')
        self.assertEqual(grade_exam_window(q('감정평가사', '', '국가전문자격'), '대학교', '4')['status'], 'CHECK_QUALIFICATION')
        # 관련학과가 '모든 학과 응시가능'이면 관련학과 조건 없음
        allq = dict(q('정보처리기사', '기사'), related_majors='모든 학과 응시가능')
        self.assertEqual(grade_exam_window(allq, '대학교', '4'), {'status': 'NOW', 'from_grade': 4, 'related_major_required': False})

    def test_any_major_flag_from_data(self):
        r = self.s.recommend(EXAMPLE)
        info = {x['jmcd']: x['exam_window'] for x in r['recommended']}
        self.assertFalse(info['1320']['related_major_required'])   # 정보처리기사: 모든 학과
        self.assertEqual(info['9521']['status'], 'NO_RESTRICTION')   # 사회조사분석사2급: 서비스분야 2급 제한 없음(Q-Net)

    def test_qualification_text_split(self):
        q = self.s.qualification_detail('1320')['qualification']
        self.assertEqual(q['conducting_agency'], '한국산업인력공단')
        self.assertEqual(q['related_majors'], '모든 학과 응시가능')
        self.assertIn('소프트웨어설계', q['exam_subjects'])
        self.assertIn('객관식', q['exam_method'])
        self.assertIn('60점 이상', q['pass_criteria'])
        q = self.s.qualification_detail('0011')['qualification']
        self.assertEqual(q['related_majors'], '대학의 기계공학, 금속공학 등 관련학과')
        q = self.s.qualification_detail('6012')['qualification']
        self.assertNotIn('실기시험 기본정보', q['pass_criteria'])
        self.assertFalse(any('&middot;' in v for v in q.values()))

    def test_pass_rate_stage_order(self):
        rates = self.s.qualification_detail('1320')['pass_rates']
        y2025 = [r['exam_stage'] for r in rates if r['exam_year'] == 2025]
        self.assertEqual(y2025, ['필기', '실기'])
        years = [r['exam_year'] for r in rates]
        self.assertEqual(years, sorted(years))
        y = [r['exam_stage'] for r in self.s.qualification_detail('9714')['pass_rates'] if r['exam_year'] == 2024]
        self.assertEqual(y, ['1차', '2차', '3차'])


    def test_condition_mode(self):
        # 조건 선택 추천: 관심 분야·희망 직무·자격구분만, 학교·전공·학년은 받지 않음, 학과 연결·응시 시점·학년 점수 없음, 최대 5개
        r = self.s.recommend_by_condition({'interest_field_ids': ['21'], 'job_field_ids': ['211']}, today='2026-11-18')
        self.assertEqual(r['mode'], 'CONDITION')
        self.assertEqual(len(r['items']), 5)
        factors = {b['factor'] for x in r['items'] for b in x['score_breakdown']}
        self.assertLessEqual(factors, {'JOB_FIELD', 'INTEREST_FIELD', 'UPCOMING_2026', 'NO_ELIGIBILITY_REQUIRED'})
        keys = [(-x['score'], -x['recent_examinees']['count'], x['jmcd']) for x in r['items']]
        self.assertEqual(keys, sorted(keys))
        for bad in ({'school_id': EXAMPLE['school_id'], 'interest_field_ids': ['21']}, {'grade': '4', 'interest_field_ids': ['21']}, {}):
            with self.assertRaises(ValidationError):
                self.s.recommend_by_condition(bad)
        pro = self.s.recommend_by_condition({'interest_field_ids': ['10'], 'qualification_type': 'PROFESSIONAL'}, today='2026-11-18')
        self.assertTrue(pro['items'] and all(self.s.db.qualifications[x['jmcd']]['qualgbcd'] == 'S' for x in pro['items']))
        own = self.s.recommend_by_condition({'interest_field_ids': ['21'], 'owned_qualification_ids': ['1320']}, today='2026-11-18')
        self.assertNotIn('1320', [x['jmcd'] for x in own['items']])

    def test_detail_new_fields(self):
        d = self.s.qualification_detail('1320', today='2026-01-24')
        self.assertTrue(d['eligibility']['required'])
        self.assertEqual(len(d['eligibility']['conditions']), 10)                       # 기사 법정 응시자격 10개
        self.assertFalse(self.s.qualification_detail('7910')['eligibility']['required'])   # 한식조리기능사
        first = d['schedules_2026'][0]
        self.assertEqual((first['registration_start_date'], first['registration_end_date'],
                          first['vacancy_registration_start_date'], first['registration_status']),
                         ('2026-01-12', '2026-01-15', '2026-01-24', 'VACANCY_OPEN'))
        y = [r for r in d['pass_rates'] if r['exam_year'] == 2025 and r['exam_stage'] == '필기'][0]
        self.assertTrue(y['applicants'] and y['attendance_rate_percent'])               # 2021~2025 국가기술: 접수자·응시율
        self.assertIn(2001, d['pass_rate_years'])
        sub = self.s.options('job_fields', interest_field_id='21', limit=100)['items']   # 정보통신 아래 중직무분야만, 종목 0개 분야는 없음
        self.assertTrue(sub and all(o['count'] > 0 and o['id'].startswith('21') for o in sub))
        tree = self.s.options('qualifications', qualification_type='TECHNICAL', interest_field_id='21', job_field_id='211', limit=100)
        self.assertIn('정보처리기사', [x['name'] for x in tree['items']])

    def test_detail_keeps_every_exam_stage(self):    # 트렌드만 1차 기준, 상세 합격률·일정은 모든 단계
        it = self.s.qualification_detail('1320', today='2026-11-20')
        self.assertEqual({r['exam_stage'] for r in it['pass_rates']}, {'필기', '실기'})
        self.assertEqual({r['exam_stage'] for r in it['schedules_2026']}, {'필기', '실기'})
        labor = self.s.qualification_detail('9500', today='2026-11-20')
        self.assertTrue({'1차', '2차', '3차'} <= {r['exam_stage'] for r in labor['pass_rates']})
        self.assertEqual({r['exam_stage'] for r in labor['schedules_2026']}, {'1차', '2차', '3차'})

    def test_daily_idiom_cycles_in_order(self):      # 1~50번을 하루 1개씩 순서대로, 50일 주기
        first = self.s.daily_idiom('2026-01-01')
        self.assertEqual((first['display_order'], first['idiom'], first['cycle_days']), (1, '우공이산', 50))
        self.assertEqual(self.s.daily_idiom('2026-01-02')['display_order'], 2)
        self.assertEqual(self.s.daily_idiom('2026-02-19')['display_order'], 50)
        self.assertEqual(self.s.daily_idiom('2026-02-20')['display_order'], 1)
        self.assertEqual(self.s.daily_idiom('2025-12-31')['display_order'], 50)
        self.assertEqual(self.s.daily_idiom('2026-11-20'), self.s.daily_idiom('2026-11-20'))

if __name__ == '__main__':
    unittest.main()
