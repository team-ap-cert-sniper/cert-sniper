#!/usr/bin/env python3
"""저격증 서비스 로직 실행 도구 (Python 3.9+, 외부 라이브러리 없음).

  python run.py check                                   # 데이터 무결성 + 전체 테스트 실행
  python run.py recommend profile.json --today 2026-11-11  # 전공 맞춤 추천(JSON). profile.json 형식은 docs/API_CONTRACT.md
  python run.py recommend conditions.json --mode condition  # 조건 선택 추천(관심 분야·희망 직무·자격구분만)
  python run.py options schools --query 백석             # 선택 목록(대학교·전공·학년·관심분야·희망직무·자격증)
  python run.py options majors --school-id 084d0e1ab9bcfde8 --query 데이터
  python run.py options qualifications --type TECHNICAL --series 기사 --query 정보   # 자격증 검색(국가기술/국가전문, 등급)
  python run.py options job_fields --type TECHNICAL --interest 21                   # 상세 탐색: 자격구분 → 직무분야 → 종목
  python run.py qualification 1320                      # 자격증 상세(2026 일정·합격률)
  python run.py trends --type TECHNICAL --limit 10       # 트렌드: 2025 응시자 순위(국가기술). --type PROFESSIONAL, --series 기사, --interest 21
  python run.py dday 1320:5 --today 2026-11-10 --exam-date 2026-11-13  # D-Day
"""
import argparse, json, sys, unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parent
sys.path.insert(0, str(ROOT))
try:
    sys.stdout.reconfigure(encoding='utf-8')
except Exception:
    pass


def out(obj):
    print(json.dumps(obj, ensure_ascii=False, indent=2))


def main():
    ap = argparse.ArgumentParser(description='저격증 v2')
    sub = ap.add_subparsers(dest='cmd', required=True)
    sub.add_parser('check', help='데이터 무결성 + 테스트')
    r = sub.add_parser('recommend', help='프로필 JSON 파일로 추천'); r.add_argument('profile_json'); r.add_argument('--today')
    r.add_argument('--mode', default='profile', choices=['profile', 'condition'])
    o = sub.add_parser('options', help='선택 목록')
    o.add_argument('kind', choices=['schools', 'majors', 'grades', 'job_fields', 'interest_fields', 'qualifications'])
    o.add_argument('--school-id'); o.add_argument('--query', default=''); o.add_argument('--type', choices=['TECHNICAL', 'PROFESSIONAL']); o.add_argument('--series')
    o.add_argument('--interest'); o.add_argument('--job')
    o.add_argument('--offset', type=int, default=0); o.add_argument('--limit', type=int, default=50)
    q = sub.add_parser('qualification', help='자격증 상세'); q.add_argument('jmcd'); q.add_argument('--today')
    tr = sub.add_parser('trends', help='트렌드: 최근 완결 연도 응시자 순위')
    tr.add_argument('--type', default='TECHNICAL', choices=['TECHNICAL', 'PROFESSIONAL'])
    tr.add_argument('--series'); tr.add_argument('--interest'); tr.add_argument('--year', type=int)
    tr.add_argument('--limit', type=int, default=20); tr.add_argument('--today')
    d = sub.add_parser('dday', help='D-Day'); d.add_argument('schedule_id'); d.add_argument('--today'); d.add_argument('--exam-date')
    a = ap.parse_args()

    if a.cmd == 'check':
        suite = unittest.defaultTestLoader.discover(str(ROOT / 'tests'), top_level_dir=str(ROOT))
        res = unittest.TextTestRunner(verbosity=1).run(suite)
        print('RESULT:', 'PASS' if res.wasSuccessful() else 'FAIL', f'({res.testsRun} tests)')
        sys.exit(0 if res.wasSuccessful() else 1)

    from jeogyeokjeung import JeogyeokjeungService, NotFoundError, ValidationError
    svc = JeogyeokjeungService()
    try:
        if a.cmd == 'recommend':
            body = json.loads(Path(a.profile_json).read_text(encoding='utf-8-sig'))
            out(svc.recommend_by_condition(body, today=a.today) if a.mode == 'condition' else svc.recommend(body, today=a.today))
        elif a.cmd == 'options':
            out(svc.options(a.kind, school_id=a.school_id, query=a.query, offset=a.offset, limit=a.limit, qualification_type=a.type, series=a.series,
                            interest_field_id=a.interest, job_field_id=a.job))
        elif a.cmd == 'qualification':
            out(svc.qualification_detail(a.jmcd, today=a.today))
        elif a.cmd == 'trends':
            out(svc.trends(a.type, series=a.series, interest_field_id=a.interest, year=a.year, limit=a.limit, today=a.today))
        elif a.cmd == 'dday':
            out(svc.dday(a.schedule_id, today=a.today, exam_date=a.exam_date))
    except ValidationError as e:
        out({'code': 'NOT_FOUND' if isinstance(e, NotFoundError) else 'VALIDATION_ERROR', 'message': str(e)}); sys.exit(2)


if __name__ == '__main__':
    main()
