import { useState, useEffect } from "react";
import Input from "../ui/Input";

export default function MemberForm({ initial, onSubmit, onCancel }) {
  const [form, setForm] = useState({
    name: "",
    email: "",
    phone: "",
  });

  useEffect(() => {
    if (initial) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setForm({
        name: initial.name,
        email: initial.email,
        phone: initial.phone || "",
      });
    }
  }, [initial]);

  return (
    <div>
      <Input
        label="Nombre"
        value={form.name}
        onChange={(e) => setForm({ ...form, name: e.target.value })}
        placeholder="Jose Luis Martínez"
      />
      <Input
        label="Email"
        type="email"
        value={form.email}
        onChange={(e) => setForm({ ...form, email: e.target.value })}
        placeholder="joseluis@biblioteca.com"
      />
      <Input
        label="Teléfono (opcional)"
        value={form.phone}
        onChange={(e) => setForm({ ...form, phone: e.target.value })}
        placeholder="+34 612 345 678"
      />
      <div className="flex gap-2.5 justify-end mt-2">
        <button
          onClick={onCancel}
          className="px-4 py-2.5 rounded-xl text-sm font-semibold border-[1.5px] border-gray-200 text-gray-700 hover:bg-gray-50 transition-colors cursor-pointer"
        >
          Cancelar
        </button>
        <button
          onClick={() => onSubmit(form)}
          className="px-4 py-2.5 rounded-xl text-sm font-semibold bg-gray-900 text-white hover:bg-gray-800 transition-colors cursor-pointer"
        >
          {initial ? "Guardar cambios" : "Registrar socio"}
        </button>
      </div>
    </div>
  );
}