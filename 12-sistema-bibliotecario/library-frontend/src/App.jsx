import { useState } from "react";
import Header from "./components/Layout/Header";
import BooksView from "./components/Books/BooksView";
import MembersView from "./components/Members/MembersView";
import LoansView from "./components/Loans/LoansView";

export default function App() {
  const [tab, setTab] = useState("books");

  const TITLES = {
    books: { title: "Libros", subtitle: "Gestiona el catálogo de la biblioteca" },
    members: { title: "Socios", subtitle: "Administra los socios registrados" },
    loans: { title: "Préstamos", subtitle: "Control de préstamos y devoluciones" },
  };

  return (
    <div className="min-h-screen bg-[#fefefe]">
      <Header activeTab={tab} onTabChange={setTab} />

      <main className="max-w-6xl mx-auto px-10 py-8">
        {/* Título de sección */}
        <div className="mb-7">
          <h1 className="font-serif text-3xl text-gray-900 mb-1">
            {TITLES[tab].title}
          </h1>
          <p className="text-sm text-gray-400">
            {TITLES[tab].subtitle}
          </p>
        </div>

        {/* Contenido */}
        <div className="bg-white rounded-2xl border border-gray-100 p-6 shadow-[0_1px_3px_rgba(0,0,0,0.04)]">
          {tab === "books" && <BooksView />}
          {tab === "members" && <MembersView />}
          {tab === "loans" && <LoansView />}
        </div>
      </main>

      {/* Footer */}
      <footer className="text-center py-6 text-gray-300 text-xs">
        Proyecto 12 · Sistema de Biblioteca · Spring Boot + React
      </footer>
    </div>
  );
}