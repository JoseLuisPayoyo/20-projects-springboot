import { useState, useEffect } from "react";
import Input from "../ui/Input";

// Reutilizable para crear y editar — recibe datos iniciales si es edición
export default function BookForm({ initial, onSubmit, onCancel }) {
  const [form, setForm] = useState({
    title: "",
    author: "",
    isbn: "",
    genre: "",
    publishedYear: "",
  });

  // Si recibe initial (edición), carga los datos en el form
  useEffect(() => {
    if (initial) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setForm({
        title: initial.title,
        author: initial.author,
        isbn: initial.isbn,
        genre: initial.genre,
        publishedYear: initial.publishedYear,
      });
    }
  }, [initial]);

  const handleSubmit = () => {
    onSubmit({
      ...form,
      publishedYear: parseInt(form.publishedYear),
    });
  };

  return (
    <div>
      <Input
        label="Título"
        value={form.title}
        onChange={(e) => setForm({ ...form, title: e.target.value })}
        placeholder="Cien años de soledad"
      />
      <Input
        label="Autor"
        value={form.author}
        onChange={(e) => setForm({ ...form, author: e.target.value })}
        placeholder="Gabriel García Márquez"
      />
      <Input
        label="ISBN"
        value={form.isbn}
        onChange={(e) => setForm({ ...form, isbn: e.target.value })}
        placeholder="978-0-06-088328-7"
      />
      <div className="grid grid-cols-2 gap-3">
        <Input
          label="Género"
          value={form.genre}
          onChange={(e) => setForm({ ...form, genre: e.target.value })}
          placeholder="Novela"
        />
        <Input
          label="Año"
          type="number"
          value={form.publishedYear}
          onChange={(e) => setForm({ ...form, publishedYear: e.target.value })}
          placeholder="1967"
        />
      </div>
      <div className="flex gap-2.5 justify-end mt-2">
        <button
          onClick={onCancel}
          className="px-4 py-2.5 rounded-xl text-sm font-semibold border-[1.5px] border-gray-200 text-gray-700 hover:bg-gray-50 transition-colors cursor-pointer"
        >
          Cancelar
        </button>
        <button
          onClick={handleSubmit}
          className="px-4 py-2.5 rounded-xl text-sm font-semibold bg-gray-900 text-white hover:bg-gray-800 transition-colors cursor-pointer"
        >
          {initial ? "Guardar cambios" : "Crear libro"}
        </button>
      </div>
    </div>
  );
}