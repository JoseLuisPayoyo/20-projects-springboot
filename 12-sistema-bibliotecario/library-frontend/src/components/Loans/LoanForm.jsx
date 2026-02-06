import { useState } from "react";
export default function LoanForm({ books, members, onSubmit, onCancel }) {
  const [bookId, setBookId] = useState("");
  const [memberId, setMemberId] = useState("");

  // Solo libros disponibles y socios activos
  const availableBooks = books.filter((b) => b.available);
  const activeMembers = members.filter((m) => m.active);

  const canSubmit = bookId && memberId;

  return (
    <div>
      {/* Select de libros */}
      <div className="mb-4">
        <label className="block text-xs font-semibold text-gray-500 mb-1.5 uppercase tracking-wider">
          Libro
        </label>
        <select
          value={bookId}
          onChange={(e) => setBookId(e.target.value)}
          className="w-full px-3.5 py-2.5 border-[1.5px] border-gray-200 rounded-xl text-sm
                     bg-gray-50 outline-none focus:border-gray-900 focus:bg-white transition-all cursor-pointer"
        >
          <option value="">Seleccionar libro disponible...</option>
          {availableBooks.map((b) => (
            <option key={b.id} value={b.id}>
              {b.title} — {b.author}
            </option>
          ))}
        </select>
        {availableBooks.length === 0 && (
          <p className="mt-1.5 text-xs text-red-600">No hay libros disponibles</p>
        )}
      </div>

      {/* Select de socios */}
      <div className="mb-4">
        <label className="block text-xs font-semibold text-gray-500 mb-1.5 uppercase tracking-wider">
          Socio
        </label>
        <select
          value={memberId}
          onChange={(e) => setMemberId(e.target.value)}
          className="w-full px-3.5 py-2.5 border-[1.5px] border-gray-200 rounded-xl text-sm
                     bg-gray-50 outline-none focus:border-gray-900 focus:bg-white transition-all cursor-pointer"
        >
          <option value="">Seleccionar socio activo...</option>
          {activeMembers.map((m) => (
            <option key={m.id} value={m.id}>
              {m.name} — {m.email}
            </option>
          ))}
        </select>
        {activeMembers.length === 0 && (
          <p className="mt-1.5 text-xs text-red-600">No hay socios activos</p>
        )}
      </div>

      <div className="flex gap-2.5 justify-end mt-2">
        <button
          onClick={onCancel}
          className="px-4 py-2.5 rounded-xl text-sm font-semibold border-[1.5px] border-gray-200 text-gray-700 hover:bg-gray-50 transition-colors cursor-pointer"
        >
          Cancelar
        </button>
        <button
          onClick={() => canSubmit && onSubmit(parseInt(bookId), parseInt(memberId))}
          className={`px-4 py-2.5 rounded-xl text-sm font-semibold bg-gray-900 text-white transition-colors cursor-pointer
                      ${canSubmit ? "hover:bg-gray-800" : "opacity-50 cursor-not-allowed"}`}
        >
          Crear préstamo
        </button>
      </div>
    </div>
  );
}