import React from 'react';

const CommentList = ({ comments, onDelete }) => {
  if (comments.length === 0) {
    return (
      <div className="text-center py-8 text-gray-400 bg-white rounded-lg border border-gray-100">
        No hay comentarios todavía. ¡Sé el primero!
      </div>
    );
  }

  return (
    <div className="space-y-3">
      {comments.map((comment) => (
        <div
          key={comment.id}
          className="bg-white rounded-lg shadow-sm p-4 border border-gray-100 hover:shadow-md transition-shadow"
        >
          <div className="flex justify-between items-start mb-2">
            <p className="text-gray-700">{comment.content}</p>
            <button
              onClick={() => {
                if (window.confirm('¿Eliminar este comentario?')) {
                  onDelete(comment.id);
                }
              }}
              className="text-red-500 hover:text-red-700 text-sm ml-4"
            >
              ✕
            </button>
          </div>
          <div className="flex items-center justify-between text-sm text-gray-500">
            <span className="font-medium">{comment.author}</span>
            <span>{new Date(comment.createdAt).toLocaleDateString('es-ES')}</span>
          </div>
        </div>
      ))}
    </div>
  );
};

export default CommentList;