// 서버가 HTML을 허용 태그만 남기며 줄바꿈을 공백으로 합치므로, 입력 텍스트의 줄바꿈은 <br>로 보낸다
export function textToHtml(text) {
  return text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/\n/g, '<br>');
}

// 수정 화면용: 저장된 HTML → textarea 텍스트
export function htmlToText(html) {
  const withBreaks = (html ?? '').replace(/<br\s*\/?>\n?/gi, '\n');
  return new DOMParser().parseFromString(withBreaks, 'text/html').body.textContent;
}

export function errorMessage(error, byCode = {}) {
  const data = error?.response?.data;
  return byCode[data?.code] ?? data?.message ?? '요청을 처리하지 못했습니다. 잠시 후 다시 시도해 주세요.';
}
