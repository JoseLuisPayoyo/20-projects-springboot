import { useState, useEffect, useCallback } from "react";
import { HandCoins, Plus } from "lucide-react";
import Swal from "sweetalert2";
import { loanService, bookService, memberService } from "../../services/api";
import Modal from "../ui/Modal";
import EmptyState from "../ui/EmptyState";
import LoanTable from "./LoanTable";
import LoanForm from "./LoanForm";

// Filtros disponibles para préstamos
const FILTERS = [
  { key: "all", label: "Todos" },
  { key: "active", label: "Activos" },
  { key: "returned", label: "Devueltos" },
  { key: "overdue", label: "Vencidos" },
];

export default function LoansView() {
  const [loans, setLoans] = useState([]);
  const [books, setBooks] = useState([]);
  const [members, setMembers] = useState([]);
  const [filter, setFilter] = useState("all");
  const [modal, setModal] = useState(false);

  const load = useCallback(() => {
    loanService.getAll().then(setLoans).catch(() => {});
    bookService.getAll().then(setBooks).catch(() => {});
    memberService.getAll().then(setMembers).catch(() => {});
  }, []);

  useEffect(() => { load(); }, [load]);

  // ── Crear préstamo ──
  const handleCreate = async (bookId, memberId) => {
    try {
      await loanService.create(bookId, memberId);
      Swal.fire({ icon: "success", title: "Préstamo creado", toast: true, position: "top-end", showConfirmButton: false, timer: 2000 });
      setModal(false);
      load();
    } catch (err) {
      Swal.fire({ icon: "error", title: "Error", text: err.message, confirmButtonColor: "#1a1a1a" });
    }
  };

  // ── Devolver libro ──
  const handleReturn = async (loan) => {
    const result = await Swal.fire({
      title: "¿Devolver libro?",
      html: `<strong>${loan.book?.title}</strong> será marcado como disponible.`,
      icon: "question",
      showCancelButton: true,
      confirmButtonText: "Devolver",
      cancelButtonText: "Cancelar",
      confirmButtonColor: "#065f46",
      cancelButtonColor: "#6b7280",
    });

    if (result.isConfirmed) {
      try {
        await loanService.returnBook(loan.id);
        Swal.fire({ icon: "success", title: "Libro devuelto", toast: true, position: "top-end", showConfirmButton: false, timer: 2000 });
        load();
      } catch (err) {
        Swal.fire({ icon: "error", title: "Error", text: err.message, confirmButtonColor: "#1a1a1a" });
      }
    }
  };

  // ── Filtrado por estado ──
  const filtered = loans.filter((l) => {
    if (filter === "active") return l.status === "ACTIVE";
    if (filter === "returned") return l.status === "RETURNED";
    if (filter === "overdue") return l.status === "OVERDUE";
    return true;
  });

  // ── Stats ──
  const stats = {
    total: loans.length,
    active: loans.filter((l) => l.status === "ACTIVE").length,
    returned: loans.filter((l) => l.status === "RETURNED").length,
  };

  return (
    <div>
      {/* Tarjetas de estadísticas */}
      <div className="grid grid-cols-3 gap-4 mb-6">
        {[
          { label: "Total", value: stats.total, color: "text-gray-900" },
          { label: "Activos", value: stats.active, color: "text-blue-800" },
          { label: "Devueltos", value: stats.returned, color: "text-emerald-800" },
        ].map((s) => (
          <div
            key={s.label}
            className="bg-gray-50 rounded-xl px-5 py-4 border border-gray-100"
          >
            <div className="text-[11px] text-gray-500 uppercase tracking-wider font-semibold mb-1">
              {s.label}
            </div>
            <div className={`text-3xl font-serif ${s.color}`}>
              {s.value}
            </div>
          </div>
        ))}
      </div>

      {/* Filtros + botón crear */}
      <div className="flex justify-between items-center mb-6 flex-wrap gap-3">
        <div className="flex gap-1.5">
          {FILTERS.map((f) => (
            <button
              key={f.key}
              onClick={() => setFilter(f.key)}
              className={`px-3.5 py-1.5 rounded-lg text-xs font-medium border-[1.5px] transition-all cursor-pointer
                ${
                  filter === f.key
                    ? "bg-gray-900 text-white border-gray-900"
                    : "bg-white text-gray-500 border-gray-200 hover:border-gray-300"
                }`}
            >
              {f.label}
            </button>
          ))}
        </div>
        <button
          onClick={() => setModal(true)}
          className="flex items-center gap-1.5 px-4 py-2.5 rounded-xl text-sm font-semibold bg-gray-900 text-white hover:bg-gray-800 transition-colors cursor-pointer"
        >
          <Plus size={18} /> Nuevo préstamo
        </button>
      </div>

      {/* Tabla o estado vacío */}
      {filtered.length === 0 ? (
        <EmptyState icon={HandCoins} message="No hay préstamos registrados" />
      ) : (
        <LoanTable loans={filtered} onReturn={handleReturn} />
      )}

      {/* Modal crear préstamo */}
      <Modal
        open={modal}
        onClose={() => setModal(false)}
        title="Nuevo préstamo"
      >
        <LoanForm
          books={books}
          members={members}
          onSubmit={handleCreate}
          onCancel={() => setModal(false)}
        />
      </Modal>
    </div>
  );
}