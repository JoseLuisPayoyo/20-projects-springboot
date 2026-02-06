import { Pencil, Trash2, CirclePause } from "lucide-react";
import StatusBadge from "../ui/StatusBadge";

export default function MemberTable({ members, onEdit, onDeactivate, onDelete }) {
  return (
    <div className="overflow-x-auto">
      <table className="w-full text-sm">
        <thead>
          <tr>
            {["Nombre", "Email", "Teléfono", "Alta", "Estado", ""].map((h) => (
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
          {members.map((member) => (
            <tr key={member.id} className="hover:bg-gray-50/70 transition-colors">
              <td className="px-4 py-3.5 font-medium text-gray-900">{member.name}</td>
              <td className="px-4 py-3.5 text-gray-600">{member.email}</td>
              <td className="px-4 py-3.5 text-gray-500">{member.phone || "—"}</td>
              <td className="px-4 py-3.5 text-gray-500">{member.membershipDate}</td>
              <td className="px-4 py-3.5">
                <StatusBadge type={member.active ? "activeUser" : "inactive"} />
              </td>
              <td className="px-4 py-3.5 text-right">
                <div className="flex gap-1 justify-end">
                  <button
                    onClick={() => onEdit(member)}
                    title="Editar"
                    className="p-1.5 rounded-md text-gray-400 hover:bg-gray-100 hover:text-gray-900 transition-all cursor-pointer"
                  >
                    <Pencil size={15} />
                  </button>
                  {member.active && (
                    <button
                      onClick={() => onDeactivate(member)}
                      title="Desactivar"
                      className="p-1.5 rounded-md text-gray-400 hover:bg-amber-50 hover:text-amber-600 transition-all cursor-pointer"
                    >
                      <CirclePause size={15} />
                    </button>
                  )}
                  <button
                    onClick={() => onDelete(member)}
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