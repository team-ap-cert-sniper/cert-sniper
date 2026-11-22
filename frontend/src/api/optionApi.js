import client from '@/api/client';

export const getOptions = (kind, params = {}) => client.get(`/options/${kind}`, { params }).then((res) => res.data);
