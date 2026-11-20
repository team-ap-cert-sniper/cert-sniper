"""Ver.9 jg_service 빈 DB에 넣을 초기 적재 SQL을 만든다 (CSV 10종, 사자성어 포함)."""
import argparse
import csv
from pathlib import Path
import sys


def read_rows(directory, name):
    with (directory / f'{name}.csv').open(encoding='utf-8-sig', newline='') as source:
        return list(csv.DictReader(source))


def fields(row, names):
    return [row[name] for name in names.split(',')]


def sql_value(value):
    if value is None or value == '':
        return 'NULL'
    if isinstance(value, int):
        return str(value)
    return "CONVERT(X'%s' USING utf8mb4)" % value.encode('utf-8').hex()


def insert(output, table, columns, records):
    prefix = f'INSERT INTO {table} ({columns}) VALUES\n'
    batch = []
    size = 0
    for record in records:
        values = '(' + ','.join(map(sql_value, record)) + ')'
        if batch and size + len(values) > 128_000:
            output.write(prefix + ',\n'.join(batch) + ';\n')
            batch = []
            size = 0
        batch.append(values)
        size += len(values) + 2
    if batch:
        output.write(prefix + ',\n'.join(batch) + ';\n')


def write_load_sql(data, output):
    schools = read_rows(data, 'schools')
    colleges = read_rows(data, 'faculties')
    majors = read_rows(data, 'majors')
    school_ids = {row['school_id']: index for index, row in enumerate(schools, 1)}
    college_ids = {row['faculty_scope_id']: index for index, row in enumerate(colleges, 1)}
    major_ids = {row['major_id']: index for index, row in enumerate(majors, 1)}
    memberships = read_rows(data, 'faculty_majors')
    major_colleges = {row['major_id']: college_ids[row['faculty_scope_id']] for row in memberships}
    if len(major_colleges) != len(memberships):
        raise ValueError('An academic major has more than one college.')
    # 서비스와 같은 규칙: 학부 이름과 같은 전공 행 = 합산 항목(aggregate_college_id),
    # 그런 행이 없는 학부 = '(학부 전체)' 목록 항목(colleges.is_list_item = 1)
    by_name = {}
    for row in sorted(majors, key=lambda r: r['major_id']):
        by_name.setdefault((row['school_id'], row['major_name']), row['major_id'])
    members = {}
    for row in memberships:
        members.setdefault(row['faculty_scope_id'], set()).add(row['major_id'])
    aggregate, list_items = {}, set()
    for row in sorted(colleges, key=lambda r: r['faculty_scope_id']):
        scope = row['faculty_scope_id']
        same_name = by_name.get((row['school_id'], row['faculty_name']))
        if same_name and members.get(scope, set()) - {same_name}:
            aggregate.setdefault(same_name, college_ids[scope])
        elif not same_name and members.get(scope):
            list_items.add(scope)
    oversized = [row['major_id'] for row in majors if len(row['category_large']) > 30]
    if oversized:
        raise ValueError(f'{len(oversized)} category_large values exceed academic_field VARCHAR(30).')

    output.write("USE jg_service;\nSET NAMES utf8mb4;\n"
                 "SET SESSION sql_mode = 'STRICT_ALL_TABLES,NO_ENGINE_SUBSTITUTION';\n"
                 "START TRANSACTION;\n")
    insert(output, 'universities', 'id,source_univ_code,name,campus_name,school_type',
           ([school_ids[r['school_id']], *fields(r, 'school_id,school_name,campus,school_type')] for r in schools))
    insert(output, 'colleges', 'id,university_id,source_college_code,name,is_list_item',
           ([college_ids[r['faculty_scope_id']], school_ids[r['school_id']],
             *fields(r, 'faculty_scope_id,faculty_name'), int(r['faculty_scope_id'] in list_items)] for r in colleges))
    insert(output, 'majors',
           'id,university_id,source_major_code,name,parent_name,degree_course,academic_field,discipline,'
           'college_id,aggregate_college_id',
           ([major_ids[r['major_id']], school_ids[r['school_id']],
             *fields(r, 'major_id,major_name,parent_name,degree_course,category_large,discipline'),
             major_colleges.get(r['major_id']), aggregate.get(r['major_id'])] for r in majors))
    insert(output, 'major_aliases', 'major_id,alias,alias_type',
           ([major_ids[r['major_id']], old, 'MERGED_CODE']
            for r in majors for old in r['merged_major_ids'].split(';') if old))

    qualification_fields = (
        'jmcd,eng_name,qualgbcd,qualgbnm,seriescd,seriesnm,obligfldcd,obligfldnm,mdobligfldcd,mdobligfldnm,'
        'related_ministry,summary,summary_basis,overview,job_description,career_outlook,conducting_agency,'
        'related_majors,exam_method,exam_subjects,eligibility,eligibility_required,eligibility_summary,'
        'eligibility_rule_key,fee_written,fee_practical,fee_note,schedule_type'
    )
    insert(output, 'qualifications', qualification_fields + ',jmfldnm,exam_standard',
           (fields(r, qualification_fields + ',name,pass_criteria') for r in read_rows(data, 'qualifications')))

    link_fields = 'jmcd,link_rank,basis,link_basis,recommendation_type'
    insert(output, 'major_qualification_links', 'major_id,' + link_fields,
           ([major_ids[r['major_id']], *fields(r, link_fields)] for r in read_rows(data, 'major_qualifications')))

    schedule_fields = (
        'jmcd,exam_round,exam_stage,exam_start_date,exam_end_date,registration_start_date,registration_end_date,'
        'pass_announcement_date,source_type,source_url,source_note,vacancy_registration_start_date,'
        'vacancy_registration_end_date'
    )
    insert(output, 'qualification_exam_schedules', schedule_fields,
           (fields(r, schedule_fields) for r in read_rows(data, 'exam_schedules')))
    insert(output, 'qualification_statistics_yearly',
           'jmcd,year,exam_stage,applicants,examinees,passers,data_source,source_url',
           (fields(r, 'jmcd,exam_year,exam_stage,applicants,examinees,passed,source_type,source_url')
            for r in read_rows(data, 'pass_rates')))
    rule_fields = 'rule_key,seq,condition_type,condition_text'
    insert(output, 'qualification_eligibility_rules', rule_fields,
           (fields(r, rule_fields) for r in read_rows(data, 'eligibility_rules')))
    idiom_fields = 'idiom,hanja,meaning,display_order'
    insert(output, 'daily_idioms', idiom_fields,
           ([*fields(r, 'idiom,hanja,meaning'), int(r['display_order'])] for r in read_rows(data, 'daily_idioms')))
    output.write('COMMIT;\n')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--data-dir', type=Path, default=Path(__file__).resolve().parents[1] / 'data')
    parser.add_argument('--output', type=Path, help='Write UTF-8 SQL to this file instead of stdout.')
    args = parser.parse_args()
    if args.output is None:
        write_load_sql(args.data_dir, sys.stdout)
    else:
        with args.output.open('w', encoding='utf-8', newline='\n') as output:
            write_load_sql(args.data_dir, output)


if __name__ == '__main__':
    main()
