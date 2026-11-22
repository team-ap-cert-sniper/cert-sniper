import client from '@/api/client';

export const getNotices = (params = {}) => client.get('/notices', { params }).then((res) => res.data);
export const getNotice = (noticeId) => client.get(`/notices/${noticeId}`).then((res) => res.data);
export const getFaqs = () => client.get('/faqs').then((res) => res.data);
export const createInquiry = (request) => client.post('/inquiries', request).then((res) => res.data);
export const getMyInquiries = (params = {}) => client.get('/inquiries', { params }).then((res) => res.data);
export const getMyInquiry = (inquiryId) => client.get(`/inquiries/${inquiryId}`).then((res) => res.data);
