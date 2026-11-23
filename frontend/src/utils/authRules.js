// 회원가입·비밀번호 재설정·변경 공통 입력 규칙 (백엔드 AuthRules.java와 같은 기준)

export const ALLOWED_EMAIL_DOMAINS = [
  'naver.com',
  'daum.net',
  'hanmail.net',
  'kakao.com',
  'nate.com',
  'gmail.com',
  'outlook.com',
  'hotmail.com',
  'icloud.com',
];

const LOCAL_PART = /^[a-z0-9._-]{1,64}$/;
// 8~20자, 영문 대문자·소문자·숫자·특수문자 각 1개 이상, 공백·한글 불가
const PASSWORD = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9])[\x21-\x7E]{8,20}$/;

/** 앞뒤 공백 제거 + 소문자 */
export const normalizeEmail = (email) => email.trim().toLowerCase();

/** 정규화된 이메일이 규칙(형식 + 허용 도메인 9개)에 맞는지 */
export function isValidEmail(email) {
  if (!email || email.length > 254) return false;
  const parts = email.split('@');
  if (parts.length !== 2) return false;
  const [local, domain] = parts;
  if (!LOCAL_PART.test(local)) return false;
  if (local.startsWith('.') || local.endsWith('.') || local.includes('..')) return false;
  return ALLOWED_EMAIL_DOMAINS.includes(domain);
}

export const isValidPassword = (password) => PASSWORD.test(password);

export const isValidName = (name) => name.trim().length >= 2 && name.trim().length <= 4;

// 닉네임: 완성형 한글(가~힣)·영문·숫자만 (특수문자·이모지·공백 불가)
const NICKNAME_CHARS = /^[가-힣A-Za-z0-9]+$/;
// 한글 자음·모음 낱자 (ㄱ~ㅎ, ㅏ~ㅣ, 옛 자모 포함)
const HANGUL_JAMO = /[\u1100-\u11FF\u3131-\u318E]/;

/** 닉네임 규칙 위반 문구 (통과하면 null). 앞뒤 공백은 지우고 검사 */
export function nicknameError(nickname) {
  const value = nickname.trim();
  if (!value) return MESSAGES.nicknameRequired;
  if (HANGUL_JAMO.test(value)) return MESSAGES.nicknameJamo;
  if (!NICKNAME_CHARS.test(value)) return MESSAGES.nicknameChars;
  if (value.length < 2 || value.length > 10) return MESSAGES.nicknameLength;
  return null;
}

export const isValidNickname = (nickname) => nicknameError(nickname) === null;

export const MESSAGES = {
  emailInvalid: '올바른 이메일 형식으로 입력해 주세요.',
  emailSent: '인증번호가 이메일로 발송되었습니다.',
  codeRequired: '인증번호를 입력해 주세요.',
  codeExpired: '인증번호가 만료되었습니다. 인증번호를 다시 발송해 주세요.',
  codeMismatch: '인증번호가 일치하지 않습니다.',
  emailVerified: '이메일 인증이 완료되었습니다.',
  emailNotVerified: '이메일 인증을 완료해 주세요.',
  spamNotice: '메일이 오지 않으면 스팸함을 확인해 주세요.',
  passwordRule: '영문 대소문자, 숫자, 특수문자를 조합하여 입력해주세요.(8~20자)',
  passwordMismatch: '비밀번호가 일치하지 않습니다.',
  passwordMatch: '비밀번호가 일치합니다.',
  nameRequired: '이름을 입력해주세요.',
  nameLength: '이름은 2~4자로 입력해 주세요.',
  nicknameRequired: '닉네임을 입력해주세요.',
  nicknameJamo: '자음·모음만 따로 사용할 수 없어요.',
  nicknameChars: '한글, 영문, 숫자만 사용할 수 있어요. (특수문자·이모지·공백 불가)',
  nicknameLength: '닉네임은 2~10자로 입력해 주세요.',
  nicknameNotChecked: '닉네임 중복 확인을 진행해 주세요.',
  nicknameTaken: '이미 사용 중인 닉네임입니다.',
  nicknameAvailable: '사용 가능한 닉네임입니다.',
  schoolIncomplete: "대학교·전공·학년을 모두 선택하거나 '대학교 다닌 적 없음'을 체크해 주세요.",
  requiredMissing: '필수 항목을 모두 입력해 주세요.',
};
