import client from '@/api/client';

export const getDday = (scheduleId, examDate) => client.get(`/exam-schedules/${scheduleId}/dday`, { params: { exam_date: examDate } }).then((res) => res.data);
