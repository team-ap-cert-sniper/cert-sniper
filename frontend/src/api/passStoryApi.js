import client from '@/api/client';

export const getPassStories = (params = {}) => client.get('/pass-stories', { params }).then((res) => res.data);
export const getPassStory = (storyId) => client.get(`/pass-stories/${storyId}`).then((res) => res.data);
export const createPassStory = (request) => client.post('/pass-stories', request).then((res) => res.data);
export const updatePassStory = (storyId, request) =>
  client.put(`/pass-stories/${storyId}`, request).then((res) => res.data);
export const deletePassStory = (storyId) => client.delete(`/pass-stories/${storyId}`).then((res) => res.data);
