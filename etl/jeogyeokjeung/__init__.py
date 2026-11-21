"""저격증 추천·자격증 정보 서비스 로직.

표준 라이브러리만으로 data/*.csv 를 읽어 JSON 직렬화 가능한 dict를 돌려준다.
입력·출력 형식은 ../docs/API_CONTRACT.md(레포 루트), 테이블 정의는 docs/DATA_TABLES.md 를 따른다.
"""
from .service import JeogyeokjeungService, NotFoundError, ValidationError

__all__ = ['JeogyeokjeungService', 'NotFoundError', 'ValidationError']
__version__ = '2.0.0'
