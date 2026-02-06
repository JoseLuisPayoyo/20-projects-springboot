// Badges de estado reutilizables para libros y préstamos
const STYLES = {
  available:  "bg-emerald-50 text-emerald-800 border-emerald-200",
  borrowed:   "bg-red-50 text-red-800 border-red-200",
  active:     "bg-blue-50 text-blue-800 border-blue-200",
  returned:   "bg-emerald-50 text-emerald-800 border-emerald-200",
  overdue:    "bg-red-50 text-red-800 border-red-200",
  activeUser: "bg-emerald-50 text-emerald-800 border-emerald-200",
  inactive:   "bg-red-50 text-red-800 border-red-200",
};

const LABELS = {
  available: "Disponible",
  borrowed: "Prestado",
  active: "Activo",
  returned: "Devuelto",
  overdue: "Vencido",
  activeUser: "Activo",
  inactive: "Inactivo",
};

export default function StatusBadge({ type }) {
  return (
    <span
      className={`px-2.5 py-0.5 rounded-full text-[11px] font-semibold uppercase tracking-wide border ${STYLES[type]}`}
    >
      {LABELS[type]}
    </span>
  );
}