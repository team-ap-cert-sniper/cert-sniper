import client from '@/api/client';

export const createAiPlan = (request) => client.post('/ai-planner', request).then((res) => res.data);
