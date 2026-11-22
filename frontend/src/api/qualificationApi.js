import client from '@/api/client';

export const getQualificationDetail = (jmcd) => client.get(`/qualifications/${jmcd}`).then((res) => res.data);
export const getTrends = (params = {}) => client.get('/trends', { params }).then((res) => res.data);
