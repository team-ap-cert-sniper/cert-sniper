import client from '@/api/client';

export const getFavorites = () => client.get('/favorites').then((res) => res.data);
export const addFavorite = (jmcd) => client.post(`/favorites/${jmcd}`).then((res) => res.data);
export const removeFavorite = (jmcd) => client.delete(`/favorites/${jmcd}`).then((res) => res.data);
