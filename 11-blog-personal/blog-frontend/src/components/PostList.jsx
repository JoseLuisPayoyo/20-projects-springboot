import React from 'react';

const PostList = ({ posts, onSelectPost, onDeletePost }) => {
  return (
    <div className="space-y-4">
      {posts.length === 0 ? (
        <div className="text-center py-12 text-gray-400">
          No hay posts todavía. ¡Crea el primero!
        </div>
      ) : (
        posts.map((post) => (
          <div
            key={post.id}
            className="bg-white rounded-lg shadow-sm hover:shadow-md transition-shadow p-6 cursor-pointer border border-gray-100"
            onClick={() => onSelectPost(post)}
          >
            <div className="flex justify-between items-start mb-2">
              <h3 className="text-xl font-semibold text-gray-800 hover:text-blue-600 transition-colors">
                {post.title}
              </h3>
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  if (window.confirm('¿Eliminar este post?')) {
                    onDeletePost(post.id);
                  }
                }}
                className="text-red-500 hover:text-red-700 text-sm"
              >
                ✕
              </button>
            </div>
            <p className="text-gray-600 line-clamp-2 mb-3">{post.content}</p>
            <div className="flex items-center justify-between text-sm text-gray-500">
              <span className="font-medium">{post.author}</span>
              <span>{post.comments?.length || 0} comentarios</span>
            </div>
          </div>
        ))
      )}
    </div>
  );
};

export default PostList;