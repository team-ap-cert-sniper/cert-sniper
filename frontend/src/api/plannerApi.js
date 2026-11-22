import client from '@/api/client';

export const getPlanner = (from, to) => client.get('/planner', { params: { from, to } }).then((res) => res.data);
export const changeMode = (mode) => client.patch('/planner/mode', null, { params: { mode } }).then((res) => res.data);
export const createBar = (request) => client.post('/planner/bars', request).then((res) => res.data);
export const updateBar = (barId, request) => client.patch(`/planner/bars/${barId}`, request).then((res) => res.data);
export const deleteBar = (barId) => client.delete(`/planner/bars/${barId}`).then((res) => res.data);
export const createMilestone = (barId, request) => client.post(`/planner/bars/${barId}/milestones`, request).then((res) => res.data);
export const updateMilestone = (milestoneId, request) => client.patch(`/planner/milestones/${milestoneId}`, request).then((res) => res.data);
export const deleteMilestone = (milestoneId) => client.delete(`/planner/milestones/${milestoneId}`).then((res) => res.data);
export const createPersonalSchedule = (request) => client.post('/planner/personal-schedules', request).then((res) => res.data);
export const updatePersonalSchedule = (scheduleId, request) => client.patch(`/planner/personal-schedules/${scheduleId}`, request).then((res) => res.data);
export const deletePersonalSchedule = (scheduleId) => client.delete(`/planner/personal-schedules/${scheduleId}`).then((res) => res.data);
export const getTodaySchedule = () => client.get('/planner/today').then((res) => res.data);
export const getDailyIdiom = () => client.get('/daily-idiom').then((res) => res.data);
