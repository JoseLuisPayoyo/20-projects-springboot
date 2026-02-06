import { useState, useEffect, useCallback } from "react";
import { BookOpen, Plus, Search } from "lucide-react";
import Swal from "sweetalert2";
import { bookService } from "../../services/api";
import Modal from "../ui/Modal";
import EmptyState from "../ui/EmptyState";
import BookTable from "./BookTable";
import BookForm from "./BookForm";

export default function BooksView() {
  const [books, setBooks] = useState([]);
  const [search, setSearch] = useState("");
  const [modal, setModal] = useState(false);
  const [editing, setEditing] = useState(null); // null = crear, objeto = editar

  const load = useCallback(() => {
    bookService.getAll().then(setBooks).catch(() => {});
  }, []);

  useEffect(() => { load(); }, [load]);

  // ── Crear / Editar ──
  const openCreate = () => { setEditing(null); setModal(true); };
  const openEdit = (book) => { setEditing(book); setModal(true); };

  const handleSubmit = async (formData) => {
    try {
      if (editing) {
        await bookService.update(editing.id, formData);
        Swal.fire({ icon: "success", title: "Libro actualizado", toast: true, position: "top-end", showConfirmButton: false, timer: 2000 });
      } else {
        await bookService.create(formData);
        Swal.fire({ icon: "success", title: "Libro creado", toast: true, position: "top-end", showConfirmButton: false, timer: 2000 });
      }
      setModal(false);
      load();
    } catch (err) {
      Swal.fire({ icon: "error", title: "Error", text: err.message || "Error de validación", confirmButtonColor: "#1a1a1a" });
    }
  };

  // ── Eliminar ──
  const handleDelete = async (book) => {
    const result = await Swal.fire({
      title: "¿Eliminar libro?",
      text: `"${book.title}" será eliminado permanentemente.`,
      icon: "warning",
      showCancelButton: true,
      confirmButtonText: "Eliminar",
      cancelButtonText: "Cancelar",
      confirmButtonColor: "#dc2626",
      cancelButtonColor: "#6b7280",
    });

    if (result.isConfirmed) {
      try {
        await bookService.delete(book.id);
        Swal.fire({ icon: "success", title: "Eliminado", toast: true, position: "top-end", showConfirmButton: false, timer: 2000 });
        load();
      } catch (err) {
        Swal.fire({ icon: "error", title: "No se puede eliminar", text: err.message, confirmButtonColor: "#1a1a1a" });
      }
    }
  };

  // ── Filtrado local por título, autor o ISBN ──
  const filtered = books.filter((b) =>
    b.title.toLowerCase().includes(search.toLowerCase()) ||
    b.author.toLowerCase().includes(search.toLowerCase()) ||
    b.isbn.includes(search)
  );

  return (
    <div>
      {/* Barra de búsqueda + botón crear */}
      <div className="flex justify-between items-center mb-6 flex-wrap gap-3">
        <div className="relative flex-1 max-w-xs">
          <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Buscar por título, autor o ISBN..."
            className="w-full pl-9 pr-3.5 py-2.5 border-[1.5px] border-gray-200 rounded-xl text-sm bg-gray-50 outline-none focus:border-gray-900 focus:bg-white transition-all"
          />
        </div>
        <button
          onClick={openCreate}
          className="flex items-center gap-1.5 px-4 py-2.5 rounded-xl text-sm font-semibold bg-gray-900 text-white hover:bg-gray-800 transition-colors cursor-pointer"
        >
          <Plus size={18} /> Nuevo libro
        </button>
      </div>

      {/* Tabla o estado vacío */}
      {filtered.length === 0 ? (
        <EmptyState icon={BookOpen} message="No hay libros registrados" />
      ) : (
        <BookTable books={filtered} onEdit={openEdit} onDelete={handleDelete} />
      )}

      {/* Modal crear/editar */}
      <Modal
        open={modal}
        onClose={() => setModal(false)}
        title={editing ? "Editar libro" : "Nuevo libro"}
      >
        <BookForm
          initial={editing}
          onSubmit={handleSubmit}
          onCancel={() => setModal(false)}
        />
      </Modal>
    </div>
  );
}