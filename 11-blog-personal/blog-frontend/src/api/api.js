import axios from 'axios';

const API_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Posts
export const getAllPosts = () => api.get('/posts');
export const getPostById = (id) => api.get(`/posts/${id}`);
export const createPost = (post) => api.post('/posts', post);
export const updatePost = (id, post) => api.put(`/posts/${id}`, post);
export const deletePost = (id) => api.delete(`/posts/${id}`);
export const searchPosts = (keyword) => api.get(`/posts/search?keyword=${keyword}`);

// Comments
export const getCommentsByPostId = (postId) => api.get(`/posts/${postId}/comments`);
export const createComment = (postId, comment) => api.post(`/posts/${postId}/comments`, comment);
export const deleteComment = (id) => api.delete(`/comments/${id}`);

export default api;