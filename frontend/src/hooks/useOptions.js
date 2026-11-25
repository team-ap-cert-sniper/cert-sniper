import { useEffect, useState } from 'react';
import { getOptions } from '@/api/optionApi';

// 선택 목록 조회. params가 null이면 조회하지 않음. 응답 전에는 null, 실패하면 빈 목록
export default function useOptions(kind, params) {
  const key = params ? JSON.stringify(params) : null;
  const [state, setState] = useState({ key: null, items: [] });

  useEffect(() => {
    if (!key) return undefined;
    let ignore = false;
    getOptions(kind, JSON.parse(key))
      .then((data) => {
        if (!ignore) setState({ key, items: data.items });
      })
      .catch(() => {
        if (!ignore) setState({ key, items: [] });
      });
    return () => {
      ignore = true;
    };
  }, [kind, key]);

  if (!key) return [];
  return state.key === key ? state.items : null;
}
