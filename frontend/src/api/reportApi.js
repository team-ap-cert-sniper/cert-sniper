import client from '@/api/client';

export const reportPassStory = (storyId, request) => client.post(`/pass-stories/${storyId}/reports`, request).then((res) => res.data);
export const getMyReports = (params = {}) => client.get('/users/me/reports', { params }).then((res) => res.data);
