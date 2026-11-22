import client from '@/api/client';

export const register = (request) => client.post('/auth/register', request).then((res) => res.data);
export const checkNickname = (nickname) => client.get('/auth/nickname-check', { params: { nickname } }).then((res) => res.data);
export const sendEmailCode = (request) => client.post('/auth/email-code', request).then((res) => res.data);
export const verifyEmailCode = (request) => client.post('/auth/email-verify', request).then((res) => res.data);
export const login = (request) => client.post('/auth/login', request).then((res) => res.data);
export const logout = (request) => client.post('/auth/logout', request).then((res) => res.data);
export const resetPassword = (request) => client.post('/auth/password-reset', request).then((res) => res.data);
export const changePassword = (request) => client.patch('/auth/password', request).then((res) => res.data);
