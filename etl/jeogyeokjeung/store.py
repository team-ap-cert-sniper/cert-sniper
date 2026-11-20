"""data/*.csv 를 메모리에 올린다. DB를 쓸 때는 이 모듈만 DB 조회로 바꾼다."""
from __future__ import annotations
import csv
from collections import defaultdict
from pathlib import Path

DEFAULT_DATA_DIR = Path(__file__).resolve().parents[1] / 'data'
TABLES = ('schools', 'majors', 'faculties', 'faculty_majors', 'qualifications',
          'major_qualifications', 'exam_schedules', 'pass_rates', 'eligibility_rules')


def _read(path: Path):
    with path.open(encoding='utf-8-sig', newline='') as f:
        return list(csv.DictReader(f))


class DataStore:
    def __init__(self, data_dir: Path | str = DEFAULT_DATA_DIR):
        d = Path(data_dir)
        missing = [t for t in TABLES if not (d / f'{t}.csv').is_file()]
        if missing:
            raise FileNotFoundError(f'데이터 테이블 누락: {missing} (위치: {d})')
        self.schools = {r['school_id']: r for r in _read(d / 'schools.csv')}
        self.majors = {r['major_id']: r for r in _read(d / 'majors.csv')}
        # 합쳐진 동명 학과의 예전 ID → 대표 ID
        self.major_alias = {old: mid for mid, r in self.majors.items() for old in r.get('merged_major_ids', '').split(';') if old}
        self.faculties = {r['faculty_scope_id']: r for r in _read(d / 'faculties.csv')}
        self.faculty_members = defaultdict(set)
        for r in _read(d / 'faculty_majors.csv'):
            self.faculty_members[r['faculty_scope_id']].add(r['major_id'])
        self.qualifications = {r['jmcd']: r for r in _read(d / 'qualifications.csv')}
        self.major_links = defaultdict(list)
        for r in _read(d / 'major_qualifications.csv'):
            self.major_links[r['major_id']].append(r)
        for rows in self.major_links.values():
            rows.sort(key=lambda r: int(r['link_rank']))
        self.schedules = defaultdict(list)
        for r in _read(d / 'exam_schedules.csv'):
            self.schedules[r['jmcd']].append(r)
        self.pass_rates = defaultdict(list)
        for r in _read(d / 'pass_rates.csv'):
            self.pass_rates[r['jmcd']].append(r)
        idioms = d / 'daily_idioms.csv'          # 사자성어 후보(팀 제공). 없으면 빈 목록
        self.daily_idioms = sorted(_read(idioms), key=lambda r: int(r['display_order'])) if idioms.is_file() else []
        self.eligibility_rules = defaultdict(list)       # rule_key(GRADE:등급 | Q:종목코드) → 응시 조건 목록
        for r in _read(d / 'eligibility_rules.csv'):
            self.eligibility_rules[r['rule_key']].append(r)
        for rows in self.eligibility_rules.values():
            rows.sort(key=lambda r: int(r['seq']))
