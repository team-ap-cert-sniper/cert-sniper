#!/usr/bin/env python3
"""공공데이터 수집 파이프라인의 결과물에서 서비스용 테이블(data/*.csv)을 만든다.

수집 파이프라인(Q-Net·대학 공공데이터 API 수집, 학과 검증)은 별도 저장소에서 실행하고,
이 스크립트는 그 결과를 정규화·보완해 data/ 에 쓴다:
    python tools/build_service_data.py --source <수집 파이프라인 경로>
"""
from __future__ import annotations
import argparse, csv, hashlib, html, io, json, os, re, sys, time
from collections import Counter, defaultdict
from pathlib import Path

V2 = Path(__file__).resolve().parents[1]
OUT = V2 / 'data'


def write_csv(name, fields, rows):
    buf = io.StringIO(newline='')
    w = csv.DictWriter(buf, fieldnames=fields, lineterminator='\n', extrasaction='ignore')
    w.writeheader(); w.writerows(rows)
    path = OUT / name
    tmp = path.with_suffix('.csv.tmp')
    tmp.write_bytes(buf.getvalue().encode('utf-8-sig'))   # BOM: 엑셀에서 한글 깨짐 방지
    os.replace(tmp, path)
    return {'rows': len(rows), 'sha256': hashlib.sha256(path.read_bytes()).hexdigest()}


LABEL = r'(시\s*행\s*처|관\s*련\s*학\s*과|시\s*험\s*과\s*목|검\s*정\s*방\s*법|합\s*격\s*기\s*준|응\s*시\s*자\s*격)'
# 항목 이름으로 인정: 앞에 ①~⑳ 번호, 또는 띄어쓴 단어로서 뒤에 ':'/'-'/내용이 이어지는 경우. 바로 뒤가 다음 번호(③)인 '… 등 관련학과' 는 제외
SECTION_RE = re.compile(r'(?:[①-⑳]\s*' + LABEL + r'\s*[:：]?|(?<!\S)' + LABEL + r'(?=\s*[:：-]|\s+(?![①-⑳]|$)))\s*[:：-]?\s*')
TAIL_RE = re.compile(r'\s*(작업형\s*)?실기시험\s*기본정보.*$', re.S)


def split_acquisition_method(text):
    """'① 시 행 처 : … ② 관련학과 : … ③ 시험과목 …' 형식을 항목별로 나눈다. 처음 나온 항목만 사용."""
    text = html.unescape(text or '').strip()
    marks = list(SECTION_RE.finditer(text))
    out = {}
    for i, m in enumerate(marks):
        label = re.sub(r'\s+', '', m.group(1) or m.group(2))
        end = marks[i + 1].start() if i + 1 < len(marks) else len(text)
        value = TAIL_RE.sub('', text[m.end():end])
        value = re.sub(r'\s*([①-⑳]|\d{1,2}\.)\s*$', '', value).strip(' -·,')   # 다음 항목 번호(③, 2.) 꼬리 제거
        if value and label not in out:
            out[label] = value
    return out


def is_undergraduate(major, school_type):
    """서비스 범위 = 학부·전문대 과정. 대학원(일반·특수·전문대학원) 소속이거나 석사·박사 과정이면 제외.
    degree_course 가 빈 학과는 대학·전문대 등 학부 기관 소속이므로 학부로 본다."""
    if '대학원' in (school_type or ''):
        return False
    course = major.get('degree_course', '') or ''
    return '석사' not in course and '박사' not in course


SKIP_RULE = re.compile(r'기타|자유전공|자율전공|무전공|교양|교육(과|학과|전공)|초등|의예|의학|치의|한의|수의|약학|간호|응급구조')


def apply_name_rules(majors, links, manifest):
    rules = read_csv(V2 / 'tools/major_name_rules.csv')
    linked = defaultdict(list)
    for l in links:
        linked[l['major_id']].append(l)
    known = [m for m in majors if m['discipline'] and linked[m['major_id']]]
    # 계열별 대표 자격증 묶음 = 그 계열 학과들에서 가장 흔한 '전공연관 추천' 묶음(순서는 첫 등장 학과 기준)
    sets = defaultdict(Counter)
    for m in known:
        codes = tuple(l['jmcd'] for l in linked[m['major_id']] if l['basis'] == 'recommended')
        if codes:
            sets[m['discipline']][codes] += 1
    canon = {d: sorted(c.items(), key=lambda x: (-x[1], x[0]))[0][0] for d, c in sets.items()}   # 동률은 사전순(결정적)
    usable, report = [], []
    for r in rules:
        assert r['discipline'] in canon, r
        hits = Counter(m['discipline'] for m in known if r['keyword'] in m['major_name'])
        n = sum(hits.values()); share = hits[r['discipline']] / n if n else 0
        ok = r['mode'] == 'MANUAL' or (n >= 5 and share >= 0.75)
        report.append({'keyword': r['keyword'], 'discipline': r['discipline'], 'mode': r['mode'], 'known_n': n,
                       'share': round(share, 2), 'applied': ok})
        if ok:
            usable.append(r)
    usable.sort(key=lambda r: -len(r['keyword']))   # 긴(구체적) 핵심어 먼저
    added = Counter()
    for m in majors:
        if m['discipline'] or linked[m['major_id']] or SKIP_RULE.search(m['major_name']):
            continue
        rule = next((r for r in usable if r['keyword'] in m['major_name']
                     and not (r['exclude'] and re.search(r['exclude'], m['major_name']))), None)
        if not rule:
            continue
        m['discipline'] = rule['discipline']
        for rank, code in enumerate(canon[rule['discipline']], 1):
            links.append({'major_id': m['major_id'], 'jmcd': code, 'link_rank': rank, 'basis': 'recommended',
                          'link_basis': f"학과명 핵심어 '{rule['keyword']}' 보완 규칙",
                          'recommendation_type': '전공 연관 진로 추천'})
        added[rule['discipline']] += 1
    manifest['name_rule_supplement'] = {'majors_added': sum(added.values()), 'by_discipline': dict(added.most_common()),
                                        'rules': report}
    return links, added


CATEGORY_FILE = V2.parent / 'Planner-DataSchool-team-handoff' / 'claude_계열분류' / '학과_계열매핑.csv'
GROUP_MIN_MAJORS, GROUP_SHARE, GROUP_MAX_ITEMS = 5, 0.5, 5
# 묶음에서 뺄 소계열: 기존 연결에 남은 오분류(군사학 → 준학예사) 때문에 공통 자격이 왜곡되는 계열
GROUP_EXCLUDE = {'군사・국방・안보'}


def apply_same_subfield(majors, links, manifest, series):
    """동계열 묶음: 같은 소계열(대·중·소계열) 학과의 절반 이상이 가진 자격증(기술사·기능장 제외, 최대 5개)을
    그 소계열의 묶음으로 정하고, 묶음 중 빠진 자격증을 같은 소계열 학과에 basis='same_subfield' 로 붙인다.
    계열 분류 = 학과_계열매핑.csv (대학알리미 공식 계열 중심)."""
    cat = {r['major_id']: r for r in read_csv(CATEGORY_FILE)}
    for m in majors:
        c = cat.get(m['major_id'], {})
        m['category_large'], m['category_mid'], m['category_small'] = c.get('대계열', ''), c.get('중계열', ''), c.get('소계열', '')
    have = defaultdict(list)
    for l in links:
        have[l['major_id']].append(l)
    groups = defaultdict(list)
    for m in majors:
        if m['category_small'] and m['category_small'] not in ('미분류',) and not m['category_small'].startswith('분류 제외'):
            groups[(m['category_large'], m['category_mid'], m['category_small'])].append(m['major_id'])
    sets, added_rows, majors_gain = {}, 0, 0
    for key, ids in groups.items():
        if len(ids) < GROUP_MIN_MAJORS or key[2] in GROUP_EXCLUDE:
            continue
        freq = Counter(code for i in ids for code in {l['jmcd'] for l in have[i]})
        chosen = [c for c, n in sorted(freq.items(), key=lambda x: (-x[1], x[0])) if n >= len(ids) * GROUP_SHARE   # 같은 겹침 수는 종목코드 순(재실행해도 동일)
                  and series.get(c) not in ('기술사', '기능장')][:GROUP_MAX_ITEMS]
        if not chosen:
            continue
        sets['/'.join(key)] = chosen
        for i in ids:
            own = {l['jmcd'] for l in have[i]}
            rank = len(own)
            gained = False
            for c in chosen:
                if c not in own:
                    rank += 1; gained = True; added_rows += 1
                    links.append({'major_id': i, 'jmcd': c, 'link_rank': rank, 'basis': 'same_subfield',
                                  'link_basis': f"동계열 묶음: {key[2]} 학과 {freq[c]}/{len(ids)} 공통",
                                  'recommendation_type': '동계열 공통 자격'})
            majors_gain += gained
    manifest['same_subfield'] = {'rule': f'소계열 학과 {GROUP_MIN_MAJORS}개 이상, {int(GROUP_SHARE*100)}% 이상 공통, 기술사·기능장 제외, 최대 {GROUP_MAX_ITEMS}개',
                                 'groups_with_set': len(sets), 'majors_gaining': majors_gain, 'rows_added': added_rows, 'sets': sets}
    return links


QNET_PAGES = 'data/source/qnet/qnet_detail_pages_2026-11-18.json'   # Q-Net 종목 상세 페이지(crf00503·s01·s02) 브라우저 수집 원본
YEARBOOK = 'data/reference/yearbook_2026/national_technical_qualification_exam_statistics_2026.xlsx'
DATE_RE = re.compile(r'(\d{4})\.\s*(\d{1,2})\.\s*(\d{1,2})')
# 응시자격: 국가기술은 등급별 법정 기준(GRADE:등급), 서비스 분야·국가전문은 종목별(Q:종목코드). 조건 목록은 tools/eligibility_rules.csv
SERVICE_FREE = {'9511', '9521', '9536', '9538', '9541', '9545', '9546', '9547', '9777'}
PRO_DISQUALIFY_ONLY = {'9500', '9641', '9737', '9745', '9747', '9748', '9749'}   # 결격사유만 규정(학력·경력 요건 없음)
PRO_SIBLING = {'9502': '9501', '9696': '9698', '9697': '9698', '9699': '9698', '9700': '9698', '9728': '9698', '9758': '9729', '9759': '9729'}
HERITAGE_TECH = {'9666', '9667', '9668', '9669', '9670', '9671'}                # 국가유산수리기술자: 응시요건 있음, 세부는 큐넷 확인
FREE_RE = re.compile(r'제한\s*없음|제한없음|:\s*없음')


def qdates(text):
    return [f'{y}-{int(m):02d}-{int(d):02d}' for y, m, d in DATE_RE.findall(text or '')]


def split_vacancy(cell):
    """'2026.01.12 ~ 2026.01.15 [빈자리접수 : 2026.01.24 ~ 2026.01.25]' → (정규 날짜, 빈자리 날짜)"""
    head, _, tail = (cell or '').partition('빈자리')
    return qdates(head), qdates(tail)


def won(text):
    digits = re.sub(r'[^\d]', '', text or '')
    return int(digits) if digits else ''


def tech_grade(q):
    if q['seriescd'] in ('01', '02', '04'):
        return {'01': '기술사', '02': '기능장', '04': '기능사'}[q['seriescd']]
    name = re.sub(r'\(.*\)$', '', q['name'])
    return '산업기사' if name.endswith('산업기사') else '기사' if name.endswith('기사') else '서비스'


def eligibility(q, page, pages, rule_keys, sib_eligibility):
    """→ (eligibility_required Y/N/'', eligibility_summary, eligibility_rule_key)"""
    code = q['jmcd']
    if q['qualgbcd'] == 'T':
        g = tech_grade(q)
        if g == '기능사' or code in SERVICE_FREE:
            return 'N', '응시 조건 없음', ''
        key = 'GRADE:' + g if g != '서비스' else 'Q:' + code
        return 'Y', ('관련학과 졸업(예정)·실무경력·보유 자격 중 하나 필요' if g in ('산업기사', '기사') else
                     '실무경력 또는 보유 자격 필요' if g in ('기능장', '기술사') else '종목별 응시 조건 필요'), key
    if 'Q:' + code in rule_keys:
        return 'Y', '종목별 응시 조건 필요', 'Q:' + code
    if code in HERITAGE_TECH:
        return 'Y', '응시 요건 있음 (큐넷 국가유산수리기술자 응시자격 확인)', ''
    sib = PRO_SIBLING.get(code, '')
    text = (page.get('texts', {}).get('응시자격', '') or q['eligibility']
            or pages.get(sib, {}).get('texts', {}).get('응시자격', '') or sib_eligibility.get(sib, ''))
    if code in PRO_DISQUALIFY_ONLY or FREE_RE.search(text):
        return 'N', '응시 조건 없음 (결격사유 해당자는 응시 불가)', ''
    return '', '응시 조건 확인 필요', ''


def read_csv(path):
    with open(path, encoding='utf-8-sig', newline='') as f:
        return list(csv.DictReader(f))


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--source', default=str(V2.parent / 'Planner-DataSchool-verified'), help='수집 파이프라인 경로')
    a = ap.parse_args()
    src = Path(a.source).resolve()
    sys.path.insert(0, str(src))
    os.chdir(src)
    from plannerschool_service_bridge import RecommendationProfileService
    from tools.faculty_scope import build_faculty_scopes
    t0 = time.time()
    svc = RecommendationProfileService(src)
    OUT.mkdir(parents=True, exist_ok=True)
    manifest = {'built_at_unix': int(t0), 'built_from': 'collection_pipeline', 'tables': {}}

    all_majors = read_csv(src / 'data/serving/majors.csv')
    # 1) schools 후보(전체 majors.csv). 실제 파일은 학부 학과가 1개 이상 남는 학교만 아래에서 기록
    schools = {}
    for m in all_majors:
        schools.setdefault(m['school_id'], {'school_id': m['school_id'], 'school_name': m['school_name'],
                                            'campus': m['campus'], 'school_type': m.get('school_type', '')})

    # 2) majors: 서비스 선택 가능 학과만(공식 검토에서 폐지·개칭·개편·과거편제·미등재 판정 제외)
    major_fields = ['major_id', 'school_id', 'major_name', 'degree_course', 'college_name', 'parent_name',
                    'academic_category', 'unit_type', 'discipline', 'merged_major_ids',
                    'category_large', 'category_mid', 'category_small']
    majors = []
    links = []
    excluded_graduate = 0
    for mid, m in svc.majors.items():
        if not is_undergraduate(m, schools[m['school_id']]['school_type']):
            excluded_graduate += 1
            continue
        legacy = svc._legacy_result(mid)
        row = {k: m.get(k, '') for k in major_fields}
        row['discipline'] = legacy.get('discipline', '') or ''
        majors.append(row)
        rank = 0
        seen = set()
        for group in ('existing_direct_links', 'recommended'):
            for q in legacy.get(group, []):
                code = str(q.get('jmcd', ''))
                ident = q.get('qualification_id', '')
                if code in svc.master and (not ident or ident == 'QNET:' + code) and code not in seen:
                    seen.add(code); rank += 1
                    links.append({'major_id': mid, 'jmcd': code, 'link_rank': rank, 'basis': group,
                                  'link_basis': q.get('link_basis', '') or q.get('evidence_type', '') or '',
                                  'recommendation_type': q.get('recommendation_type', '') or ''})
    # 같은 학교·같은 소속(단과대학·학부)·같은 이름 학과는 한 줄로 합친다(6단계 R1: 동명 코드는 구분 근거 없음).
    # 대표 = 연결 자격증이 가장 많은 코드(같으면 major_id 순). 연결은 합집합, 나머지 ID는 merged_major_ids 로 남긴다.
    by_major = defaultdict(list)
    for l in links:
        by_major[l['major_id']].append(l)
    groups = defaultdict(list)
    for r in majors:
        groups[(r['school_id'], r['college_name'], r['parent_name'], r['major_name'])].append(r)
    merged_majors, merged_links = [], []
    for grp in groups.values():
        grp.sort(key=lambda r: (-len(by_major[r['major_id']]), r['major_id']))
        rep = dict(grp[0], merged_major_ids=';'.join(sorted(r['major_id'] for r in grp[1:])))
        merged_majors.append(rep)
        seen = set()
        for r in grp:
            for l in by_major[r['major_id']]:
                if l['jmcd'] not in seen:
                    seen.add(l['jmcd'])
                    merged_links.append(dict(l, major_id=rep['major_id'], link_rank=len(seen)))
    manifest['major_merge'] = {'before': len(majors), 'after': len(merged_majors)}
    majors, links = merged_majors, merged_links
    alias = {m: r['major_id'] for r in majors for m in r['merged_major_ids'].split(';') if m}
    # 학과명 핵심어 보완 규칙: 원천에서 계열 미판정 + 추천 0개인 학과에 한해
    # tools/major_name_rules.csv 의 핵심어로 '기존 계열'을 정하고, 그 계열의 기존 대표 자격증 묶음을 붙인다.
    # DATA 규칙은 이미 판정된 학과 중 그 핵심어 포함 학과가 5개 이상·같은 계열 75% 이상일 때만 적용.
    # (동명 코드 합친 뒤 적용: 합친 학과에 기존 연결이 하나라도 있으면 규칙을 붙이지 않음)
    links, majors_rule_stats = apply_name_rules(majors, links, manifest)
    links = apply_same_subfield(majors, links, manifest, {k: q['seriesnm'] for k, q in svc.master.items()})
    majors.sort(key=lambda r: (r['school_id'], r['major_name'], r['major_id']))
    kept = {r['major_id'] for r in majors}
    used_schools = {r['school_id'] for r in majors}
    manifest['service_scope'] = {'rule': '학부·전문대 과정만(대학원 소속·석사·박사 과정 제외)',
                                 'v1_selectable_majors': len(svc.majors), 'excluded_graduate_majors': excluded_graduate,
                                 'kept_majors': len(majors), 'kept_schools': len(used_schools)}
    manifest['tables']['schools.csv'] = write_csv('schools.csv', ['school_id', 'school_name', 'campus', 'school_type'],
                                                  sorted((v for k, v in schools.items() if k in used_schools),
                                                         key=lambda r: (r['school_name'], r['campus'], r['school_id'])))
    manifest['tables']['majors.csv'] = write_csv('majors.csv', major_fields, majors)
    manifest['tables']['major_qualifications.csv'] = write_csv(
        'major_qualifications.csv', ['major_id', 'jmcd', 'link_rank', 'basis', 'link_basis', 'recommendation_type'], links)

    # 3) faculties(학부 범위) + 학부-학과 연결(선택 가능 학과만)
    scopes, children, _ = build_faculty_scopes(src)
    fac = [{'faculty_scope_id': s['faculty_scope_id'], 'school_id': s['school_id'], 'faculty_name': s['faculty_name'],
            'resolution': s.get('resolution', '')} for s in scopes if s['school_id'] in used_schools]
    manifest['tables']['faculties.csv'] = write_csv('faculties.csv', ['faculty_scope_id', 'school_id', 'faculty_name', 'resolution'],
                                                    sorted(fac, key=lambda r: (r['school_id'], r['faculty_name'], r['faculty_scope_id'])))
    fm = set()
    for s in scopes:
        for mid in s['catalog_parent_major_ids']:
            fm.add((s['faculty_scope_id'], mid))
    for c in children:
        fm.add((c['faculty_scope_id'], c['child_major_id']))
    fm = {(f, alias.get(m, m)) for f, m in fm}
    fm_rows = [{'faculty_scope_id': f, 'major_id': m} for f, m in sorted(fm) if m in kept]
    manifest['tables']['faculty_majors.csv'] = write_csv('faculty_majors.csv', ['faculty_scope_id', 'major_id'], fm_rows)

    # 4) qualifications: Q-Net 원장 613종 + 상세 설명.
    #    acquisition_method(원문 한 덩어리)를 항목 이름(시행처·관련학과·시험과목·검정방법·합격기준·응시자격) 기준으로 나눠 별도 칸에 담는다.
    #    원문은 acquisition_method 에 그대로 둔다. 원천에 exam_subjects·eligibility 값이 이미 있으면 그 값을 우선한다.
    profile = {r['jmcd']: r for r in read_csv(src / 'data/curated/qualification_profile.csv')}
    qf = ['jmcd', 'name', 'qualgbcd', 'qualgbnm', 'seriescd', 'seriesnm', 'obligfldcd', 'obligfldnm',
          'mdobligfldcd', 'mdobligfldnm', 'conducting_agency', 'related_majors', 'exam_subjects', 'exam_method',
          'pass_criteria', 'eligibility', 'acquisition_method', 'exam_trend']
    qrows = []
    for code, q in svc.master.items():
        p = profile.get(code, {})
        parts = split_acquisition_method(p.get('acquisition_method', ''))
        qrows.append({**{k: q.get(k, '') for k in qf}, 'name': q['jmfldnm'],
                      'conducting_agency': parts.get('시행처', ''), 'related_majors': parts.get('관련학과', ''),
                      'exam_subjects': p.get('exam_subjects', '').strip() or parts.get('시험과목', ''),
                      'exam_method': parts.get('검정방법', ''), 'pass_criteria': parts.get('합격기준', ''),
                      'eligibility': p.get('eligibility', '').strip() or parts.get('응시자격', ''),
                      'acquisition_method': p.get('acquisition_method', ''), 'exam_trend': p.get('exam_trend', '')})
    for r in qrows:  # 원문에 남은 HTML 기호(&middot; 등) 정리
        for k in qf[10:]:
            r[k] = html.unescape(r[k])
    # 국가전문자격은 Q-Net 원장에 직무분야 코드가 없다 → tools/professional_fields.csv(자격 계열별 매핑표)로 보완.
    #   국가기술자격에 쓰이는 코드만 사용, 맞는 중분류가 없으면 대분류만. field_source: QNET | MAPPED | ''(미보완)
    names = {r['obligfldcd']: r['obligfldnm'] for r in qrows if r['obligfldcd']}
    mnames = {r['mdobligfldcd']: r['mdobligfldnm'] for r in qrows if r['mdobligfldcd']}
    pmap = {r['seriesnm']: r for r in read_csv(V2 / 'tools/professional_fields.csv')}
    #   related_ministry: 국가전문자격 관련부처(Q-Net 국가자격 종목별 상세정보 crf005 '관련부처' 분류).
    for r in qrows:
        r['field_source'] = 'QNET' if r['obligfldcd'] else ''
        m = pmap.get(r['seriesnm']) if r['qualgbnm'] == '국가전문자격' else None
        r['related_ministry'] = m['ministry'] if m else ''
        if m and not r['obligfldcd'] and m['obligfldcd']:
            r.update(obligfldcd=m['obligfldcd'], obligfldnm=names[m['obligfldcd']], mdobligfldcd=m['mdobligfldcd'],
                     mdobligfldnm=mnames.get(m['mdobligfldcd'], ''), field_source='MAPPED')
    qf += ['field_source', 'related_ministry']
    # 종목 소개: 공공데이터포털「한국산업인력공단_국가기술자격 종목별 시험정보」(3038404, 2025-12-31 기준)
    #   의 개요·수행직무·진로 및 전망. 국가기술 일부와 국가전문은 원천에 없음 → 빈칸.
    #   summary: 화면용 한 줄 소개(서비스 작성 문구). summary_basis: QNET_TEXT(공단 수행직무 근거) / EXAM_INFO(수행직무 없음 → 시험과목·직무분야 근거)
    texts = {r['jmcd']: r for r in read_csv(V2 / 'tools/qualification_texts.csv')}
    tf = ['summary', 'summary_basis', 'overview', 'job_description', 'career_outlook']
    for r in qrows:
        r.update({k: texts.get(r['jmcd'], {}).get(k, '') for k in tf})
    qf += tf
    # Q-Net 종목 상세 페이지: 영문명·관련부처(국가기술 포함)·시행기관·수수료·시험 형태(정기/상시)·응시 조건
    pages = {x['jmcd']: x for x in json.loads((src / QNET_PAGES).read_text(encoding='utf-8'))['items']}
    rules = read_csv(V2 / 'tools/eligibility_rules.csv')
    rule_keys = {r['rule_key'] for r in rules}
    for r in qrows:
        pg = pages.get(r['jmcd'], {})
        info = pg.get('info', {})
        r['eng_name'] = pg.get('eng', '')
        ministry = ', '.join(dict.fromkeys(m.strip() for m in info.get('관련부처', '').split(',') if m.strip()))
        r['related_ministry'] = ministry or r['related_ministry']
        r['conducting_agency'] = r['conducting_agency'] or info.get('시행기관', '')
        fee = next((t for t in pg.get('tables', []) if '수수료' in t['cap']), None)
        heads = fee['heads'][0] if fee and fee['heads'] else []
        vals = dict(zip(heads, fee['rows'][0])) if fee and fee['rows'] else {}
        r['fee_written'], r['fee_practical'] = won(vals.get('필기')), won(vals.get('실기'))
        note = pg.get('texts', {}).get('응시수수료', '')
        r['fee_note'] = re.sub(r'\s+', ' ', note.replace('□ 응시수수료', '')).strip()
        sched_text = ' '.join(c for t in pg.get('tables', []) if '수수료' not in t['cap'] for row in t['rows'] for c in row)
        r['schedule_type'] = '상시' if '상시' in sched_text and not qdates(sched_text) else '정기'
        r['eligibility_required'], r['eligibility_summary'], r['eligibility_rule_key'] = eligibility(r, pg, pages, rule_keys, {x['jmcd']: x['eligibility'] for x in qrows})
    qf += ['eng_name', 'fee_written', 'fee_practical', 'fee_note', 'schedule_type',
           'eligibility_required', 'eligibility_summary', 'eligibility_rule_key']
    used = {r['eligibility_rule_key'] for r in qrows}
    manifest['tables']['eligibility_rules.csv'] = write_csv('eligibility_rules.csv', ['rule_key', 'seq', 'condition_type', 'condition_text'],
                                                            [r for r in rules if r['rule_key'] in used])
    manifest['eligibility_required_counts'] = dict(Counter(r['eligibility_required'] or '확인 필요' for r in qrows))
    qrows.sort(key=lambda r: r['jmcd'])
    manifest['tables']['qualifications.csv'] = write_csv('qualifications.csv', qf, qrows)
    manifest['professional_field_mapping'] = {k: sum(1 for r in qrows if r['field_source'] == k) for k in ('QNET', 'MAPPED', '')}
    manifest['professional_ministry_filled'] = sum(1 for r in qrows if r['related_ministry'])
    manifest['qualification_text_counts'] = {k: sum(1 for r in qrows if r[k]) for k in tf}
    manifest['qualification_text_split_counts'] = {k: sum(1 for r in qrows if r[k]) for k in
        ('conducting_agency', 'related_majors', 'exam_subjects', 'exam_method', 'pass_criteria', 'eligibility')}

    # 5) exam_schedules: 2026 일정. 공식 회차표 + Q-Net 일정 API 파이프라인을 한 테이블로, 같은 단계·같은 날짜는 1행
    sf = ['jmcd', 'exam_year', 'exam_round', 'exam_stage', 'exam_start_date', 'exam_end_date',
          'registration_start_date', 'registration_end_date', 'pass_announcement_date', 'source_type', 'source_url', 'source_note']
    sched, seen = [], set()
    for r in read_csv(src / 'data/curated/exam_schedules/official_exam_rounds.csv'):
        code = r['qualification_id'].split(':', 1)[1]
        if r['exam_year'] != '2026' or code not in svc.master:
            continue
        key = (code, r['exam_stage'], r['exam_start_date'], r['exam_end_date'])
        if key in seen: continue
        seen.add(key)
        sched.append({'jmcd': code, 'exam_year': r['exam_year'], 'exam_round': r['exam_round'], 'exam_stage': r['exam_stage'],
                      'exam_start_date': r['exam_start_date'], 'exam_end_date': r['exam_end_date'],
                      'registration_start_date': r['registration_start_date'], 'registration_end_date': r['registration_end_date'],
                      'pass_announcement_date': '', 'source_type': 'OFFICIAL_ROUNDS', 'source_url': r['source_url'],
                      'source_note': r['verification_status']})
    for r in read_csv(src / 'data/curated/qualification_exam_schedules_2026.csv'):
        code = r['jmcd'].zfill(4)
        if r.get('impl_year') != '2026' or code not in svc.master:
            continue
        m = re.search(r'\d+회\s*(\S+)\)', r.get('description', ''))
        for prefix, default_stage in (('doc', '필기'), ('prac', '실기')):
            start, end = r.get(prefix + '_exam_start_date', ''), r.get(prefix + '_exam_end_date', '')
            if not start or not end:
                continue
            assert start <= end, (code, start, end)
            stage = m.group(1) if (r.get('qualgb_cd') == 'S' and m and prefix == 'doc') else default_stage
            key = (code, stage, start, end)
            if key in seen: continue
            seen.add(key)
            sched.append({'jmcd': code, 'exam_year': '2026', 'exam_round': r['impl_seq'] + '회', 'exam_stage': stage,
                          'exam_start_date': start, 'exam_end_date': end,
                          'registration_start_date': r.get(prefix + '_reg_start_date', ''),
                          'registration_end_date': r.get(prefix + '_reg_end_date', ''),
                          'pass_announcement_date': r.get(prefix + '_pass_date', ''), 'source_type': 'QNET_SCHEDULE_API',
                          'source_url': 'https://www.data.go.kr/data/15074408/openapi.do', 'source_note': r.get('description', '')})
    # 빈자리 접수 기간: Q-Net 종목 상세 시험일정 표(정기 접수 칸의 [빈자리접수 : …]). 같은 종목·단계·시험 시작일 행에 붙인다.
    vacancy = {}
    for code, pg in pages.items():
        for t in pg.get('tables', []):
            if '수수료' in t['cap'] or not t['heads']:
                continue
            h = t['heads'][0]
            for row in t['rows']:
                cells = dict(zip(h, row))
                if '필기시험' in h:        # 국가기술: 필기·실기 한 줄
                    pairs = [('필기', cells.get(h[1], ''), cells.get('필기시험', '')), ('실기', cells.get(h[4], ''), cells.get('실기시험', ''))]
                else:                      # 국가전문: 'N회1차' 단계
                    m = re.search(r'(\d차|면접)', cells.get('구분', ''))
                    pairs = [(m.group(1) if m else '시험', cells.get('접수기간', ''), cells.get('시험일정', ''))]
                for stage, reg, exam in pairs:
                    regular, vac = split_vacancy(reg)
                    ex = qdates(exam)
                    if len(vac) >= 2 and ex:
                        vacancy[(code, stage, ex[0])] = (vac[0], vac[1], regular[:2])
    fixed = 0
    for r in sched:     # 정규 접수 칸에 빈자리 기간이 들어간 행은 Q-Net 표의 정규 접수 기간으로 바로잡는다
        vs, ve, regular = vacancy.get((r['jmcd'], r['exam_stage'], r['exam_start_date']), ('', '', []))
        r['vacancy_registration_start_date'], r['vacancy_registration_end_date'] = vs, ve
        if len(regular) == 2 and (r['registration_start_date'], r['registration_end_date']) != tuple(regular):
            r['registration_start_date'], r['registration_end_date'] = regular
            fixed += 1
    sf += ['vacancy_registration_start_date', 'vacancy_registration_end_date']
    manifest['vacancy_found'] = {'qnet_rows': len(vacancy), 'matched': sum(1 for r in sched if r['vacancy_registration_start_date']),
                                 'registration_fixed_to_qnet_page': fixed}
    sched.sort(key=lambda r: (r['jmcd'], r['exam_start_date'], r['exam_stage']))
    manifest['tables']['exam_schedules.csv'] = write_csv('exam_schedules.csv', sf, sched)

    # 6) pass_rates: 실제 응시자 기준 연도·단계별 합격 인원.
    #   QNET_OFFICIAL : qnet_annual_pass_rates.csv (Q-Net 종목 상세 검정현황 표 + 국가전문 통계 게시판 첨부)
    #   QNET_STATS_API: qualification_statistics_2021_2025.csv 중 위에 없는 연도·단계만.
    #                   두 출처가 겹치는 1,927건이 응시자·합격자 모두 일치(불일치 0)해 applicants=응시자로 판단.
    #   QNET_PAGE     : Q-Net 종목 상세 '종목별 검정현황' 표 전 연도(2026-11-18 수집). 같은 연도·단계는 이 값을 먼저 쓴다.
    #   applicants    : 원서 접수자 수. 2026 국가기술자격 통계연보(2021~2025, 국가기술)에만 있음 → 나머지는 빈칸.
    pf = ['jmcd', 'exam_year', 'exam_stage', 'applicants', 'examinees', 'passed', 'source_type', 'source_url']
    rates, have = [], set()
    for code, pg in sorted(pages.items()):
        for t in pg.get('stats', []):
            for row in t['rows']:
                if not re.fullmatch(r'\d{4}', row[0]):
                    continue
                for stage, (n, k) in (('필기', row[1:3]), ('실기', row[4:6])):
                    n, k = won(n), won(k)
                    if n == '' or n <= 0:
                        continue
                    assert k != '' and 0 <= k <= n, (code, row)
                    rates.append({'jmcd': code, 'exam_year': row[0], 'exam_stage': stage, 'examinees': n, 'passed': k,
                                  'source_type': 'QNET_PAGE', 'source_url': f'https://www.q-net.or.kr/crf005.do?id=crf00503&jmCd={code}'})
                    have.add((code, row[0], stage))
    page_rows = len(rates)
    for r in read_csv(src / 'data/curated/pass_rates/qnet_annual_pass_rates.csv'):
        code = r['qualification_id'].split(':', 1)[1]
        if code in svc.master and int(r['examinees']) > 0 and (code, r['exam_year'], r['exam_stage']) not in have:  # 응시 0명 행 제외
            rates.append({'jmcd': code, 'exam_year': r['exam_year'], 'exam_stage': r['exam_stage'], 'examinees': r['examinees'],
                          'passed': r['passed'], 'source_type': 'QNET_OFFICIAL', 'source_url': r['source_url']})
            have.add((code, r['exam_year'], r['exam_stage']))
    overlap_checked = 0
    for r in read_csv(src / 'data/curated/qualification_statistics_2021_2025.csv'):
        code = r['jmcd'].zfill(4)
        if code not in svc.master:
            continue
        for stage, pre in (('필기', 'written'), ('실기', 'practical')):
            n, k = r.get(pre + '_applicants', '').strip(), r.get(pre + '_passers', '').strip()
            if not n or not k or int(n) <= 0:
                continue
            assert 0 <= int(k) <= int(n), (code, r['year'], stage)
            if (code, r['year'], stage) in have:
                overlap_checked += 1
                continue
            rates.append({'jmcd': code, 'exam_year': r['year'], 'exam_stage': stage, 'examinees': n, 'passed': k,
                          'source_type': 'QNET_STATS_API', 'source_url': 'data.go.kr Q-Net 종목별 검정현황(qualification_statistics_2021_2025.csv)'})
    import openpyxl
    wb = openpyxl.load_workbook(src / YEARBOOK, read_only=True, data_only=True)
    norm = lambda x: re.sub(r'[\s·.ㆍ()（）]', '', x)
    by_name = {norm(q['jmfldnm']): c for c, q in svc.master.items()}
    applicants, mismatch = {}, 0
    for ws in wb.worksheets[:6]:          # 2-1(기술사·기능장·기사·산업기사·기능사·서비스) 종목별 연도별 접수·응시·합격
        suffix = re.search(r'\((.+)\)', ws.title).group(1)
        suffix = '' if suffix == '서비스' else suffix
        name = None
        for row in ws.iter_rows(min_row=6, values_only=True):
            if row[0] not in (None, ''):
                name = str(row[0]).strip()
            if row[2] != '전체' or not name or name == '합계' or name.endswith('분야') or not re.fullmatch(r'\d{2}', str(row[1]).strip()):
                continue
            m = re.match(r'(.*?)(\(.*\))$', name)
            code = next((by_name[norm(c)] for c in [name + suffix, name] + ([m.group(1) + suffix + m.group(2)] if m else []) if norm(c) in by_name), None)
            if code:
                year = str(2000 + int(str(row[1]).strip()))
                for stage, (a, e) in (('필기', row[3:5]), ('실기', row[7:9])):
                    if isinstance(a, int) and a > 0:
                        applicants[(code, year, stage)] = (a, e)
    for r in rates:
        a = applicants.get((r['jmcd'], r['exam_year'], r['exam_stage']))
        r['applicants'] = a[0] if a else ''
        mismatch += bool(a and isinstance(a[1], int) and a[1] != int(r['examinees']))
    manifest['pass_rates_sources'] = {'QNET_PAGE': page_rows, 'with_applicants': sum(1 for r in rates if r['applicants']),
                                      'yearbook_examinee_mismatch': mismatch}
    rates.sort(key=lambda r: (r['jmcd'], r['exam_year'], r['exam_stage']))
    manifest['tables']['pass_rates.csv'] = write_csv('pass_rates.csv', pf, rates)
    manifest['pass_rates_overlap_rows_skipped'] = overlap_checked

    manifest['seconds'] = round(time.time() - t0, 1)
    tmp = OUT / 'MANIFEST.json.tmp'
    tmp.write_text(json.dumps(manifest, ensure_ascii=False, indent=2), encoding='utf-8'); os.replace(tmp, OUT / 'MANIFEST.json')
    print(json.dumps(manifest, ensure_ascii=False, indent=1))


if __name__ == '__main__':
    main()
