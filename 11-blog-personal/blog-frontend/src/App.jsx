import React, { useState, useEffect } from 'react';
import PostList from './components/PostList';
import PostForm from './components/PostForm';
import PostDetail from './components/PostDetail';
import {
  getAllPosts,
  createPost,
  deletePost,
  getCommentsByPostId,
  createComment,
  deleteComment,
  searchPosts,
} from './api/api';

function App() {
  const [posts, setPosts] = useState([]);
  const [selectedPost, setSelectedPost] = useState(null);
  const [comments, setComments] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(false);

  // Cargar posts al inicio
  useEffect(() => {
    loadPosts();
  }, []);

  // Cargar comentarios cuando se selecciona un post
  useEffect(() => {
    if (selectedPost) {
      loadComments(selectedPost.id);
    }
  }, [selectedPost]);

  const loadPosts = async () => {
    try {
      setLoading(true);
      const response = await getAllPosts();
      setPosts(response.data);
    } catch (error) {
      console.error('Error al cargar posts:', error);
      alert('Error al cargar los posts');
    } finally {
      setLoading(false);
    }
  };

  const loadComments = async (postId) => {
    try {
      const response = await getCommentsByPostId(postId);
      setComments(response.data);
    } catch (error) {
      console.error('Error al cargar comentarios:', error);
    }
  };

  const handleCreatePost = async (postData) => {
    try {
      await createPost(postData);
      setShowForm(false);
      loadPosts();
      alert('Post creado exitosamente');
    } catch (error) {
      console.error('Error al crear post:', error);
      alert('Error al crear el post: ' + (error.response?.data?.message || error.message));
    }
  };

  const handleDeletePost = async (id) => {
    try {
      await deletePost(id);
      loadPosts();
      if (selectedPost?.id === id) {
        setSelectedPost(null);
      }
      alert('Post eliminado');
    } catch (error) {
      console.error('Error al eliminar post:', error);
      alert('Error al eliminar el post');
    }
  };

  const handleAddComment = async (commentData) => {
    try {
      await createComment(selectedPost.id, commentData);
      loadComments(selectedPost.id);
      alert('Comentario añadido');
    } catch (error) {
      console.error('Error al añadir comentario:', error);
      alert('Error al añadir comentario: ' + (error.response?.data?.message || error.message));
    }
  };

  const handleDeleteComment = async (id) => {
    try {
      await deleteComment(id);
      loadComments(selectedPost.id);
      alert('Comentario eliminado');
    } catch (error) {
      console.error('Error al eliminar comentario:', error);
      alert('Error al eliminar el comentario');
    }
  };

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!searchTerm.trim()) {
      loadPosts();
      return;
    }

    try {
      setLoading(true);
      const response = await searchPosts(searchTerm);
      setPosts(response.data);
    } catch (error) {
      console.error('Error en búsqueda:', error);
      alert('Error al buscar');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-4xl mx-auto px-4 py-6">
          <h1 className="text-3xl font-bold text-gray-900">Blog Personal</h1>
          <p className="text-gray-500 mt-1">Comparte tus pensamientos con el mundo</p>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-4xl mx-auto px-4 py-8">
        {selectedPost ? (
          // Vista de detalle del post
          <PostDetail
            post={selectedPost}
            comments={comments}
            onBack={() => setSelectedPost(null)}
            onAddComment={handleAddComment}
            onDeleteComment={handleDeleteComment}
          />
        ) : (
          // Vista de lista de posts
          <>
            {/* Search & Actions */}
            <div className="mb-6 space-y-4">
              <form onSubmit={handleSearch} className="flex gap-3">
                <input
                  type="text"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  placeholder="Buscar posts..."
                  className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition"
                />
                <button
                  type="submit"
                  className="px-6 py-2 bg-gray-800 text-white rounded-lg hover:bg-gray-900 transition-colors font-medium"
                >
                  Buscar
                </button>
                {searchTerm && (
                  <button
                    type="button"
                    onClick={() => {
                      setSearchTerm('');
                      loadPosts();
                    }}
                    className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
                  >
                    Limpiar
                  </button>
                )}
              </form>

              <button
                onClick={() => setShowForm(!showForm)}
                className="w-full bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700 transition-colors font-medium"
              >
                {showForm ? 'Cancelar' : '+ Nuevo Post'}
              </button>
            </div>

            {/* Post Form */}
            {showForm && (
              <div className="mb-6">
                <PostForm onSubmit={handleCreatePost} onCancel={() => setShowForm(false)} />
              </div>
            )}

            {/* Loading */}
            {loading && (
              <div className="text-center py-12">
                <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
              </div>
            )}

            {/* Post List */}
            {!loading && (
              <PostList
                posts={posts}
                onSelectPost={setSelectedPost}
                onDeletePost={handleDeletePost}
              />
            )}
          </>
        )}
      </main>

      {/* Footer */}
      <footer className="bg-white border-t border-gray-200 mt-12">
        <div className="max-w-4xl mx-auto px-4 py-6 text-center text-gray-500 text-sm">
          Blog Personal - Proyecto 11 Spring Boot + React
        </div>
      </footer>
    </div>
  );
}

export default App;