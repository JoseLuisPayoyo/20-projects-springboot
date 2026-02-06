import { Pencil, Trash2 } from "lucide-react";
import StatusBadge from "../ui/StatusBadge";

export default function BookTable({ books, onEdit, onDelete }) {
  return (
    <div className="overflow-x-auto">
      <table className="w-full text-sm">
        <thead>
          <tr>
            {["Título", "Autor", "ISBN", "Género", "Año", "Estado", ""].map((h) => (
              <th
                key={h}
                className="text-left px-4 py-3 text-gray-500 text-[11px] font-semibold uppercase tracking-wider border-b-[1.5px] border-gray-100"
              >
                {h}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {books.map((book) => (
            <tr key={book.id} className="hover:bg-gray-50/70 transition-colors">
              <td className="px-4 py-3.5 font-medium text-gray-900">{book.title}</td>
              <td className="px-4 py-3.5 text-gray-600">{book.author}</td>
              <td className="px-4 py-3.5 text-gray-500 font-mono text-xs">{book.isbn}</td>
              <td className="px-4 py-3.5 text-gray-600">{book.genre}</td>
              <td className="px-4 py-3.5 text-gray-500">{book.publishedYear}</td>
              <td className="px-4 py-3.5">
                <StatusBadge type={book.available ? "available" : "borrowed"} />
              </td>
              <td className="px-4 py-3.5 text-right">
                <div className="flex gap-1 justify-end">
                  <button
                    onClick={() => onEdit(book)}
                    title="Editar"
                    className="p-1.5 rounded-md text-gray-400 hover:bg-gray-100 hover:text-gray-900 transition-all cursor-pointer"
                  >
                    <Pencil size={15} />
                  </button>
                  <button
                    onClick={() => onDelete(book)}
                    title="Eliminar"
                    className="p-1.5 rounded-md text-gray-400 hover:bg-red-50 hover:text-red-600 transition-all cursor-pointer"
                  >
                    <Trash2 size={15} />
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}