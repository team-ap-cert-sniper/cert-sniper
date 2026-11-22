import client from '@/api/client';

export const getMyRank = () => client.get('/ranks/me').then((res) => res.data);
