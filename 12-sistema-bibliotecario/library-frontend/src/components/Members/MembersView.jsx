import { useState, useEffect, useCallback } from "react";
import { Users, Plus, Search } from "lucide-react";
import Swal from "sweetalert2";
import { memberService } from "../../services/api";
import Modal from "../ui/Modal";
import EmptyState from "../ui/EmptyState";
import MemberTable from "./MemberTable";
import MemberForm from "./MemberForm";

export default function MembersView() {
  const [members, setMembers] = useState([]);
  const [search, setSearch] = useState("");
  const [modal, setModal] = useState(false);
  const [editing, setEditing] = useState(null);

  const load = useCallback(() => {
    memberService.getAll().then(setMembers).catch(() => {});
  }, []);

  useEffect(() => { load(); }, [load]);

  // ── Crear / Editar ──
  const openCreate = () => { setEditing(null); setModal(true); };
  const openEdit = (member) => { setEditing(member); setModal(true); };

  const handleSubmit = async (formData) => {
    try {
      if (editing) {
        await memberService.update(editing.id, formData);
        Swal.fire({ icon: "success", title: "Socio actualizado", toast: true, position: "top-end", showConfirmButton: false, timer: 2000 });
      } else {
        await memberService.create(formData);
        Swal.fire({ icon: "success", title: "Socio registrado", toast: true, position: "top-end", showConfirmButton: false, timer: 2000 });
      }
      setModal(false);
      load();
    } catch (err) {
      Swal.fire({ icon: "error", title: "Error", text: err.message || "Error de validación", confirmButtonColor: "#1a1a1a" });
    }
  };

  // ── Desactivar ──
  const handleDeactivate = async (member) => {
    const result = await Swal.fire({
      title: "¿Desactivar socio?",
      text: `${member.name} no podrá realizar nuevos préstamos.`,
      icon: "warning",
      showCancelButton: true,
      confirmButtonText: "Desactivar",
      cancelButtonText: "Cancelar",
      confirmButtonColor: "#d97706",
      cancelButtonColor: "#6b7280",
    });

    if (result.isConfirmed) {
      try {
        await memberService.deactivate(member.id);
        Swal.fire({ icon: "success", title: "Socio desactivado", toast: true, position: "top-end", showConfirmButton: false, timer: 2000 });
        load();
      } catch (err) {
        Swal.fire({ icon: "error", title: "Error", text: err.message, confirmButtonColor: "#1a1a1a" });
      }
    }
  };

  // ── Eliminar ──
  const handleDelete = async (member) => {
    const result = await Swal.fire({
      title: "¿Eliminar socio?",
      text: `"${member.name}" será eliminado permanentemente.`,
      icon: "warning",
      showCancelButton: true,
      confirmButtonText: "Eliminar",
      cancelButtonText: "Cancelar",
      confirmButtonColor: "#dc2626",
      cancelButtonColor: "#6b7280",
    });

    if (result.isConfirmed) {
      try {
        await memberService.delete(member.id);
        Swal.fire({ icon: "success", title: "Eliminado", toast: true, position: "top-end", showConfirmButton: false, timer: 2000 });
        load();
      } catch (err) {
        Swal.fire({ icon: "error", title: "No se puede eliminar", text: err.message, confirmButtonColor: "#1a1a1a" });
      }
    }
  };

  // ── Filtrado local ──
  const filtered = members.filter((m) =>
    m.name.toLowerCase().includes(search.toLowerCase()) ||
    m.email.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div>
      <div className="flex justify-between items-center mb-6 flex-wrap gap-3">
        <div className="relative flex-1 max-w-xs">
          <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Buscar por nombre o email..."
            className="w-full pl-9 pr-3.5 py-2.5 border-[1.5px] border-gray-200 rounded-xl text-sm bg-gray-50 outline-none focus:border-gray-900 focus:bg-white transition-all"
          />
        </div>
        <button
          onClick={openCreate}
          className="flex items-center gap-1.5 px-4 py-2.5 rounded-xl text-sm font-semibold bg-gray-900 text-white hover:bg-gray-800 transition-colors cursor-pointer"
        >
          <Plus size={18} /> Nuevo socio
        </button>
      </div>

      {filtered.length === 0 ? (
        <EmptyState icon={Users} message="No hay socios registrados" />
      ) : (
        <MemberTable
          members={filtered}
          onEdit={openEdit}
          onDeactivate={handleDeactivate}
          onDelete={handleDelete}
        />
      )}

      <Modal
        open={modal}
        onClose={() => setModal(false)}
        title={editing ? "Editar socio" : "Nuevo socio"}
      >
        <MemberForm
          initial={editing}
          onSubmit={handleSubmit}
          onCancel={() => setModal(false)}
        />
      </Modal>
    </div>
  );
}