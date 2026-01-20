import React, { useState } from 'react';

const CommentForm = ({ onSubmit }) => {
  const [formData, setFormData] = useState({
    content: '',
    author: '',
  });

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit(formData);
    setFormData({ content: '', author: '' });
  };

  return (
    <form onSubmit={handleSubmit} className="bg-white rounded-lg shadow-sm p-6 border border-gray-100">
      <h4 className="text-md font-semibold mb-4 text-gray-800">Añadir comentario</h4>
      
      <div className="space-y-4">
        <div>
          <textarea
            required
            minLength={3}
            rows={3}
            value={formData.content}
            onChange={(e) => setFormData({ ...formData, content: e.target.value })}
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition resize-none"
            placeholder="Escribe tu comentario..."
          />
        </div>

        <div className="flex gap-3">
          <input
            type="text"
            required
            minLength={3}
            value={formData.author}
            onChange={(e) => setFormData({ ...formData, author: e.target.value })}
            className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition"
            placeholder="Tu nombre..."
          />
          <button
            type="submit"
            className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition-colors font-medium"
          >
            Comentar
          </button>
        </div>
      </div>
    </form>
  );
};

export default CommentForm;