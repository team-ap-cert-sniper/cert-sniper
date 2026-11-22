import client from '@/api/client';

export const getMyProfile = () => client.get('/users/me').then((res) => res.data);
export const updateMyProfile = (request) => client.patch('/users/me', request).then((res) => res.data);
export const requestWithdrawal = (request) => client.post('/users/me/withdrawal', request).then((res) => res.data);
export const cancelWithdrawal = () => client.delete('/users/me/withdrawal').then((res) => res.data);
