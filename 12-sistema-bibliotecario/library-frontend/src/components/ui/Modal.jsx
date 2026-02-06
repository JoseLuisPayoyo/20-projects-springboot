import { X } from "lucide-react";

export default function Modal({ open, onClose, title, children }) {
  if (!open) return null;

  return (
    // Overlay: clic fuera cierra el modal
    <div
      onClick={onClose}
      className="fixed inset-0 bg-black/30 backdrop-blur-sm flex items-center justify-center z-50 animate-fade-in"
    >
      {/* stopPropagation evita que el clic en el contenido cierre el modal */}
      <div
        onClick={(e) => e.stopPropagation()}
        className="bg-white rounded-2xl p-8 w-full max-w-md shadow-2xl animate-slide-up"
      >
        <div className="flex justify-between items-center mb-6">
          <h2 className="font-serif text-xl text-gray-900">{title}</h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors cursor-pointer"
          >
            <X size={20} />
          </button>
        </div>
        {children}
      </div>
    </div>
  );
}