import { BookOpen, Users, HandCoins, Library } from "lucide-react";

const NAV_ITEMS = [
  { key: "books", label: "Libros", icon: BookOpen },
  { key: "members", label: "Socios", icon: Users },
  { key: "loans", label: "Préstamos", icon: HandCoins },
];

export default function Header({ activeTab, onTabChange }) {
  return (
    <header className="bg-white border-b border-gray-100 px-10 sticky top-0 z-50 backdrop-blur-lg">
      <div className="max-w-6xl mx-auto flex items-center justify-between h-16">
        {/* Logo */}
        <div className="flex items-center gap-3">
          <Library size={24} className="text-gray-900" />
          <span className="font-serif text-xl text-gray-900 tracking-tight">
            Biblioteca
          </span>
        </div>

        {/* Navegación */}
        <nav className="flex gap-1">
          {NAV_ITEMS.map((item) => (
            <button
              key={item.key}
              onClick={() => onTabChange(item.key)}
              className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200 cursor-pointer
                ${
                  activeTab === item.key
                    ? "bg-gray-900 text-white"
                    : "text-gray-500 hover:text-gray-900 hover:bg-gray-50"
                }`}
            >
              <item.icon size={18} />
              {item.label}
            </button>
          ))}
        </nav>
      </div>
    </header>
  );
}