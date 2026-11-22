import client from '@/api/client';

export const getMemo = (targetExamId) => client.get(`/target-exams/${targetExamId}/memo`).then((res) => res.data);
export const saveMemo = (targetExamId, request) =>
  client.put(`/target-exams/${targetExamId}/memo`, request).then((res) => res.data);
export const deleteMemo = (targetExamId) => client.delete(`/target-exams/${targetExamId}/memo`).then((res) => res.data);
