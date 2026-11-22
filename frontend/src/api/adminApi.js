import client from '@/api/client';

export const getDashboard = () => client.get('/admin/dashboard').then((res) => res.data);
export const getUsers = (params = {}) => client.get('/admin/users', { params }).then((res) => res.data);
export const getUser = (userId) => client.get(`/admin/users/${userId}`).then((res) => res.data);
export const correctUser = (userId, request) => client.patch(`/admin/users/${userId}`, request).then((res) => res.data);
export const suspendUser = (userId, request) =>
  client.post(`/admin/users/${userId}/suspension`, request).then((res) => res.data);
export const unsuspendUser = (userId) => client.delete(`/admin/users/${userId}/suspension`).then((res) => res.data);
export const createNotice = (request) => client.post('/admin/notices', request).then((res) => res.data);
export const updateNotice = (noticeId, request) =>
  client.put(`/admin/notices/${noticeId}`, request).then((res) => res.data);
export const deleteNotice = (noticeId) => client.delete(`/admin/notices/${noticeId}`).then((res) => res.data);
export const getInquiriesForAdmin = (params = {}) => client.get('/admin/inquiries', { params }).then((res) => res.data);
export const answerInquiry = (inquiryId, request) =>
  client.post(`/admin/inquiries/${inquiryId}/answer`, request).then((res) => res.data);
export const getReports = (params = {}) => client.get('/admin/reports', { params }).then((res) => res.data);
export const reviewReport = (reportId, request) =>
  client.patch(`/admin/reports/${reportId}`, request).then((res) => res.data);
export const reReviewPassStory = (storyId, request) =>
  client.patch(`/admin/pass-stories/${storyId}/re-review`, request).then((res) => res.data);
export const getPassStoriesForAdmin = (params = {}) =>
  client.get('/admin/pass-stories', { params }).then((res) => res.data);
