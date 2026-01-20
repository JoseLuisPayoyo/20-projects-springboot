import React from 'react';
import CommentList from './CommentList';
import CommentForm from './CommentForm';

const PostDetail = ({ post, comments, onBack, onAddComment, onDeleteComment }) => {
  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center gap-4">
        <button
          onClick={onBack}
          className="text-gray-600 hover:text-gray-800 font-medium"
        >
          ← Volver
        </button>
      </div>

      {/* Post */}
      <div className="bg-white rounded-lg shadow-sm p-6 border border-gray-100">
        <h2 className="text-2xl font-bold text-gray-800 mb-4">{post.title}</h2>
        <p className="text-gray-700 whitespace-pre-wrap mb-4">{post.content}</p>
        <div className="text-sm text-gray-500 border-t pt-4">
          <span className="font-medium">Por {post.author}</span>
          <span className="mx-2">•</span>
          <span>{new Date(post.createdAt).toLocaleDateString('es-ES')}</span>
        </div>
      </div>

      {/* Comment Form */}
      <CommentForm onSubmit={onAddComment} />

      {/* Comments */}
      <div>
        <h3 className="text-lg font-semibold text-gray-800 mb-4">
          Comentarios ({comments.length})
        </h3>
        <CommentList comments={comments} onDelete={onDeleteComment} />
      </div>
    </div>
  );
};

export default PostDetail;