import csv, hashlib, json, unittest
from pathlib import Path

DATA = Path(__file__).resolve().parents[1] / 'data'


def rows(name):
    with (DATA / name).open(encoding='utf-8-sig', newline='') as f:
        return list(csv.DictReader(f))


class DataIntegrity(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.manifest = json.loads((DATA / 'MANIFEST.json').read_text(encoding='utf-8'))
        cls.t = {n: rows(n) for n in cls.manifest['tables']}

    def test_manifest_counts_and_hashes(self):
        for name, meta in self.manifest['tables'].items():
            self.assertEqual(len(self.t[name]), meta['rows'], name)
            self.assertEqual(hashlib.sha256((DATA / name).read_bytes()).hexdigest(), meta['sha256'], name)

    def test_qnet_scope_is_613(self):
        self.assertEqual(len(self.t['qualifications.csv']), 613)

    def test_primary_keys_unique(self):
        for name, key in [('schools.csv', 'school_id'), ('majors.csv', 'major_id'),
                          ('faculties.csv', 'faculty_scope_id'), ('qualifications.csv', 'jmcd')]:
            ids = [r[key] for r in self.t[name]]
            self.assertEqual(len(ids), len(set(ids)), name)
        links = [(r['major_id'], r['jmcd']) for r in self.t['major_qualifications.csv']]
        self.assertEqual(len(links), len(set(links)))
        rates = [(r['jmcd'], r['exam_year'], r['exam_stage']) for r in self.t['pass_rates.csv']]
        self.assertEqual(len(rates), len(set(rates)))

    def test_foreign_keys(self):
        schools = {r['school_id'] for r in self.t['schools.csv']}
        majors = {r['major_id'] for r in self.t['majors.csv']}
        facs = {r['faculty_scope_id'] for r in self.t['faculties.csv']}
        quals = {r['jmcd'] for r in self.t['qualifications.csv']}
        self.assertTrue(all(r['school_id'] in schools for r in self.t['majors.csv']))
        self.assertTrue(all(r['school_id'] in schools for r in self.t['faculties.csv']))
        self.assertTrue(all(r['faculty_scope_id'] in facs and r['major_id'] in majors for r in self.t['faculty_majors.csv']))
        self.assertTrue(all(r['major_id'] in majors and r['jmcd'] in quals for r in self.t['major_qualifications.csv']))
        self.assertTrue(all(r['jmcd'] in quals for r in self.t['exam_schedules.csv']))
        self.assertTrue(all(r['jmcd'] in quals for r in self.t['pass_rates.csv']))

    def test_undergraduate_scope_only(self):
        types = {r['school_id']: r['school_type'] for r in self.t['schools.csv']}
        self.assertFalse([t for t in types.values() if '대학원' in t])
        self.assertEqual(self.manifest['service_scope']['kept_majors'], len(self.t['majors.csv']))
        # 학교 목록에는 학부 학과가 있는 학교만
        self.assertEqual({r['school_id'] for r in self.t['majors.csv']}, set(types))

    def test_same_name_majors_merged(self):
        keys = [(r['school_id'], r['college_name'], r['parent_name'], r['major_name']) for r in self.t['majors.csv']]
        self.assertEqual(len(keys), len(set(keys)))
        self.assertEqual(self.manifest['major_merge']['after'], len(self.t['majors.csv']))

    def test_name_rule_supplement(self):
        self.assertGreater(self.manifest['name_rule_supplement']['majors_added'], 0)
        linked = {r['major_id'] for r in self.t['major_qualifications.csv']}
        self.assertLessEqual(self.manifest['name_rule_supplement']['majors_added'], len(linked))

    def test_value_ranges(self):
        for r in self.t['pass_rates.csv']:   # 2001년~최근 완결 연도
            self.assertTrue(0 <= int(r['passed']) <= int(r['examinees']) and int(r['examinees']) > 0, r)
            self.assertTrue(2001 <= int(r['exam_year']) <= 2025, r)
        self.assertTrue(all(r['source_type'] in ('QNET_PAGE', 'QNET_OFFICIAL', 'QNET_STATS_API') for r in self.t['pass_rates.csv']))
        for r in self.t['pass_rates.csv']:     # 접수자는 통계연보가 있는 행만, 있으면 응시자 이상
            self.assertTrue(r['applicants'] == '' or int(r['applicants']) >= int(r['examinees']), r)
        keys = {r['rule_key'] for r in self.t['eligibility_rules.csv']}
        for q in self.t['qualifications.csv']:  # 응시 조건 필요(Y)인데 조건 목록이 없는 건 국가유산수리기술자뿐
            self.assertIn(q['eligibility_required'], ('Y', 'N', ''))
            self.assertTrue(not q['eligibility_rule_key'] or q['eligibility_rule_key'] in keys, q['jmcd'])
            if q['eligibility_required'] == 'Y' and not q['eligibility_rule_key']:
                self.assertTrue(q['name'].startswith('국가유산수리기술자'), q['name'])
        for r in self.t['exam_schedules.csv']:  # 빈자리 접수는 정규 접수 이후, 시험 시작 전
            if r['vacancy_registration_start_date']:
                self.assertTrue(r['registration_end_date'] <= r['vacancy_registration_start_date'] <= r['vacancy_registration_end_date'] <= r['exam_start_date'], r)
        for r in self.t['exam_schedules.csv']:
            self.assertEqual(r['exam_year'], '2026')
            self.assertLessEqual(r['exam_start_date'], r['exam_end_date'], r)
        for r in self.t['major_qualifications.csv']:
            self.assertIn(r['basis'], ('existing_direct_links', 'recommended', 'same_subfield'))

    def test_daily_idioms(self):              # 사자성어 50개: 팀 제공 목록, MANIFEST 대상 아님(직접 관리)
        idioms = rows('daily_idioms.csv')
        self.assertEqual(len(idioms), 50)
        self.assertEqual(len({r['idiom'] for r in idioms}), 50)
        self.assertEqual(sorted(int(r['display_order']) for r in idioms), list(range(1, 51)))
        for r in idioms:
            self.assertTrue(1 <= len(r['idiom']) <= 10 and len(r['hanja']) <= 10 and 1 <= len(r['meaning']) <= 200, r)


if __name__ == '__main__':
    unittest.main()
