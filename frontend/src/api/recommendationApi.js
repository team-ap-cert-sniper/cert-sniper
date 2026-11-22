import client from '@/api/client';

export const recommend = (request) => client.post('/recommendations', request).then((res) => res.data);
export const recommendByCondition = (request) =>
  client.post('/recommendations/conditions', request).then((res) => res.data);
