import { Undo2 } from "lucide-react";
import StatusBadge from "../ui/StatusBadge";

export default function LoanTable({ loans, onReturn }) {
  return (
    <div className="overflow-x-auto">
      <table className="w-full text-sm">
        <thead>
          <tr>
            {["Libro", "Socio", "Préstamo", "Vencimiento", "Devolución", "Estado", ""].map((h) => (
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
          {loans.map((loan) => {
            // Mapeo de status del backend al tipo del badge
            const badgeType =
              loan.status === "ACTIVE" ? "active"
              : loan.status === "RETURNED" ? "returned"
              : "overdue";

            return (
              <tr key={loan.id} className="hover:bg-gray-50/70 transition-colors">
                <td className="px-4 py-3.5 font-medium text-gray-900">
                  {loan.book?.title || `Libro #${loan.book?.id}`}
                </td>
                <td className="px-4 py-3.5 text-gray-600">
                  {loan.member?.name || `Socio #${loan.member?.id}`}
                </td>
                <td className="px-4 py-3.5 text-gray-500">{loan.loanDate}</td>
                <td className="px-4 py-3.5 text-gray-500">{loan.dueDate}</td>
                <td className="px-4 py-3.5 text-gray-500">{loan.returnDate || "—"}</td>
                <td className="px-4 py-3.5">
                  <StatusBadge type={badgeType} />
                </td>
                <td className="px-4 py-3.5 text-right">
                  {loan.status === "ACTIVE" && (
                    <button
                      onClick={() => onReturn(loan)}
                      className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold
                                 text-emerald-700 border-[1.5px] border-emerald-200
                                 hover:bg-emerald-50 transition-all cursor-pointer"
                    >
                      <Undo2 size={14} /> Devolver
                    </button>
                  )}
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}