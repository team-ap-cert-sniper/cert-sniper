"""학년·학제·보유 자격으로 본 응시 시점 안내 (Q-Net 응시자격별 안내 기준).

개인 응시자격 확정이 아니다. 산업기사·기사는 '관련학과'(동일·유사 직무분야) 조건이 붙는다.
보유 자격 경로: 산업기사는 '동일·유사 직무분야 다른 종목 산업기사 등급 이상', 기사는 '… 기사 등급 이상',
기술사는 '… 다른 종목 기술사' 취득자면 실무경력 없이 응시 가능.
동일 직무분야 = 직무분야(대분류) 같음, 유사 직무분야 = 국가기술자격법 시행규칙 [별표 11의2]
(2026-12-31까지 시행본) → SIMILAR_FIELDS.
서비스분야(직업상담사·사회조사분석사 등, 종목코드 9xxx): 2급·단일등급 4종은 제한 없음, 1급은 2급+실무, 그 외 개별 확인.
국가전문자격은 응시자격 원문에 '제한 없음'이 있으면 NO_RESTRICTION, 아니면 개별 확인.
상태값: NO_RESTRICTION / NOW / FROM_GRADE / AFTER_GRADUATION_EXPERIENCE / CHECK_QUALIFICATION
"""
import re
EXAM_WINDOW_SOURCE = 'https://www.q-net.or.kr/crf006.do?id=crf00603&gSite=Q'
FOUR_YEAR_TYPES = {'대학교', '산업대학', '교육대학', '사이버대학(대학)', '기술대학', '방송통신대학', '각종학교(대학)'}
JUNIOR_TYPES = {'전문대학', '기능대학', '사이버대학(전문)', '각종학교(전문)'}


LEVELS = ('기능사', '산업기사', '기사', '기능장', '기술사')          # 낮은 등급 → 높은 등급
OWNED_ROUTE_MIN = {'산업기사': '산업기사', '기사': '기사', '기술사': '기술사'}
EXPERIENCE_ROUTE = {'산업기사': '기능사 취득 후 동일·유사 직무분야 실무 1년',
                    '기사': '산업기사 취득 후 실무 1년 또는 기능사 취득 후 실무 3년',
                    '기술사': '기사 취득 후 실무 4년 · 산업기사 5년 · 기능사 7년',
                    '기능장': '기능장 응시자격(실무경력) Q-Net 확인',
                    '서비스': '2급 취득 후 해당 실무 2~3년(종목별 상이, Q-Net 확인)'}
SERVICE_OPEN = {'텔레마케팅관리사', '멀티미디어콘텐츠제작전문가', '스포츠경영관리사', '이러닝운영관리사'}
OPEN_RE = re.compile(r'제한\s*없|응시자격\s*:\s*없음')


# [별표 11의2] 유사 직무분야: 응시하려는 종목의 중직무분야(없으면 직무분야) → 유사로 보는 직무분야(2자리)·중직무분야(3자리). '*' = 모든 직무분야
SIMILAR_FIELDS = {
    '024': '14 15 16 17 18 19 20 21 22 23 24 25 26', '082': '141 19 232', '083': '024 20 21', '091': '146 24',
    '131': '22', '132': '22', '14': '024 082 15 16 17 18 20 24 25 26', '15': '024 14 164 17 18 24 25 26',
    '16': '024 14 17 18 20 212 213 25 26', '17': '024 151 16 18 20 25 262', '18': '024 151 16 17 19 25 26',
    '19': '024 082 18', '20': '024 083 14 16 17 21 25 26', '211': '*', '212': '024 165 20', '213': '024 165 20',
    '221': '024 13', '222': '13', '231': '082', '232': '082 141', '24': '024 14 15 25 261',
    '251': '024 14 15 16 17 18 19 20 21 22 23 24 26', '252': '024 151 16 17 18 201',
    '261': '024 14 15 16 17 18 202 24 25', '262': '024 142 16 17 201'}
SIMILAR_SPECIAL = [  # 별표의 직종 한정 행(261-2, 262-2, 262-3)
    ('261', re.compile('자연환경관리|자연생태복원|생물분류|토양환경'), '142 143 144 24'),
    ('262', re.compile('기상'), '024 14 16 17 18 19 20 21 22 23 24'),
    ('262', re.compile('신재생에너지발전설비'), '024 141 142 16 17 20')]


def same_or_similar_field(target: dict, owned: dict) -> bool:
    """owned 종목이 target 종목의 '동일 및 유사 직무분야'에 속하는가."""
    big, mid = target.get('obligfldcd', ''), target.get('mdobligfldcd', '')
    if not big or not owned.get('obligfldcd'):
        return False
    if owned['obligfldcd'] == big:
        return True
    spec = next((s for m, rx, s in SIMILAR_SPECIAL if m == mid and rx.search(target.get('name', ''))), None)
    spec = (spec or SIMILAR_FIELDS.get(mid) or SIMILAR_FIELDS.get(big, '')).split()
    return '*' in spec or owned['obligfldcd'] in spec or owned.get('mdobligfldcd') in spec


def base_name(q: dict) -> str:
    """'소방설비산업기사(기계분야)' → '소방설비산업기사'"""
    return re.sub(r'\(.*\)$', '', q.get('name', ''))


def level(q: dict) -> str:
    """Q-Net 원장은 산업기사·서비스분야도 seriesnm='기사'로 저장되어 있어 이름·종목코드로 구분한다."""
    b = base_name(q)
    if b.endswith('산업기사'):
        return '산업기사'
    if q.get('qualgbnm') == '국가기술자격' and q.get('jmcd', '').startswith('9') and not b.endswith(('기능사', '기사', '기능장', '기술사')):
        return '서비스'
    return q.get('seriesnm', '')


def owned_route(q: dict, owned: list) -> str | None:
    """보유 자격만으로 바로 응시 가능하면 그 보유 종목코드."""
    need = OWNED_ROUTE_MIN.get(level(q))
    if q.get('qualgbnm') != '국가기술자격' or not need:
        return None
    for o in owned:
        lv = level(o)
        if (o['jmcd'] != q['jmcd'] and o.get('qualgbnm') == '국가기술자격' and same_or_similar_field(q, o)
                and lv in LEVELS and LEVELS.index(lv) >= LEVELS.index(need) and (need != '기술사' or lv == '기술사')):
            return o['jmcd']
    return None


def any_major_allowed(q: dict) -> bool:
    """Q-Net 시험정보의 관련학과가 '모든 학과 응시가능'인 종목(예: 정보처리기사)."""
    text = (q.get('related_majors') or '').replace(' ', '')
    return '모든학과' in text


def grade_exam_window(q: dict, school_type: str, grade: str, owned: list = ()) -> dict:
    """q: qualifications.csv 한 행. owned: 보유 자격 행 목록."""
    via = owned_route(q, owned)
    if via:
        return {'status': 'NOW', 'from_grade': None, 'related_major_required': False, 'via_owned_qualification': via}
    result = _window(q, school_type, grade)
    if result['related_major_required'] and any_major_allowed(q):
        result = dict(result, related_major_required=False)
    return result


def start_level(school_type: str, grade: str) -> str:
    """학교 유형·학년 기준으로 지금 응시 가능한 가장 높은 국가기술자격 등급 (기사 → 산업기사 → 기능사 순으로 확인)."""
    for lv in ('기사', '산업기사'):
        if _window({'qualgbnm': '국가기술자격', 'seriesnm': '기사', 'name': lv, 'jmcd': '1'}, school_type, grade)['status'] == 'NOW':
            return lv
    return '기능사'


def _window(q: dict, school_type: str, grade: str) -> dict:
    name = q.get('name', '')
    series = q.get('seriesnm', '')
    if q.get('qualgbnm') != '국가기술자격':
        open_ = bool(OPEN_RE.search((q.get('eligibility') or '')[:120]))
        return {'status': 'NO_RESTRICTION' if open_ else 'CHECK_QUALIFICATION', 'from_grade': None,
                'related_major_required': False if open_ else None}
    if level(q) == '서비스':
        name = q.get('name', '')
        if (name.endswith('2급') and not name.startswith('임상심리사')) or name in SERVICE_OPEN:
            return {'status': 'NO_RESTRICTION', 'from_grade': None, 'related_major_required': False}
        status = 'AFTER_GRADUATION_EXPERIENCE' if name.endswith('1급') else 'CHECK_QUALIFICATION'
        return {'status': status, 'from_grade': None, 'related_major_required': None}
    if series == '기능사':
        return {'status': 'NO_RESTRICTION', 'from_grade': None, 'related_major_required': False}
    if series in ('기술사', '기능장'):
        return {'status': 'AFTER_GRADUATION_EXPERIENCE', 'from_grade': None, 'related_major_required': True}
    lv = level(q)
    four = school_type in FOUR_YEAR_TYPES
    junior = school_type in JUNIOR_TYPES
    if grade == 'GRADUATED':
        status = 'NOW' if (four or lv == '산업기사') else 'AFTER_GRADUATION_EXPERIENCE'
        return {'status': status, 'from_grade': None, 'related_major_required': True}
    if not (four or junior) or not grade.isdigit():
        return {'status': 'CHECK_QUALIFICATION', 'from_grade': None, 'related_major_required': True}
    g = int(grade)
    if lv == '산업기사':
        start = 3 if four else 2   # 4년제: 과정 1/2 이수, 전문대: 최종학년
    elif four:
        start = 4                  # 4년제: 최종학년(졸업예정자)
    else:
        return {'status': 'AFTER_GRADUATION_EXPERIENCE', 'from_grade': None, 'related_major_required': True}
    return {'status': 'NOW' if g >= start else 'FROM_GRADE', 'from_grade': start, 'related_major_required': True}
