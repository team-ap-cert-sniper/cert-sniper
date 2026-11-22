import client from '@/api/client';

export const getTargetExams = () => client.get('/target-exams').then((res) => res.data);
export const addTargetExam = (request) => client.post('/target-exams', request).then((res) => res.data);
export const updateTargetExam = (targetExamId, request) =>
  client.patch(`/target-exams/${targetExamId}`, request).then((res) => res.data);
export const deleteTargetExam = (targetExamId) =>
  client.delete(`/target-exams/${targetExamId}`).then((res) => res.data);
