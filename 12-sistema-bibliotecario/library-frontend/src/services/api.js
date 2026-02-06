const API_BASE = "http://localhost:8080/api";

// Helper centralizado para todas las peticiones al backend
const request = async (path, options = {}) => {
  const res = await fetch(`${API_BASE}${path}`, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });

  // DELETE devuelve 204 sin body
  if (res.status === 204) return null;

  const data = await res.json();

  // Si el backend devuelve error (400, 404, 409...), lanzamos para capturar con catch
  if (!res.ok) throw data;

  return data;
};

// ── Books ──
export const bookService = {
  getAll: () => request("/books"),
  getById: (id) => request(`/books/${id}`),
  getAvailable: () => request("/books/available"),
  getByAuthor: (author) => request(`/books/author/${author}`),
  getByGenre: (genre) => request(`/books/genre/${genre}`),
  getByIsbn: (isbn) => request(`/books/isbn/${isbn}`),
  create: (book) => request("/books", { method: "POST", body: JSON.stringify(book) }),
  update: (id, book) => request(`/books/${id}`, { method: "PUT", body: JSON.stringify(book) }),
  delete: (id) => request(`/books/${id}`, { method: "DELETE" }),
};

// ── Members ──
export const memberService = {
  getAll: () => request("/members"),
  getById: (id) => request(`/members/${id}`),
  getLoans: (id) => request(`/members/${id}/loans`),
  create: (member) => request("/members", { method: "POST", body: JSON.stringify(member) }),
  update: (id, member) => request(`/members/${id}`, { method: "PUT", body: JSON.stringify(member) }),
  deactivate: (id) => request(`/members/${id}/deactivate`, { method: "PATCH" }),
  delete: (id) => request(`/members/${id}`, { method: "DELETE" }),
};

// ── Loans ──
export const loanService = {
  getAll: () => request("/loans"),
  getById: (id) => request(`/loans/${id}`),
  getActive: () => request("/loans/active"),
  getOverdue: () => request("/loans/overdue"),
  getByBook: (bookId) => request(`/loans/book/${bookId}`),
  create: (bookId, memberId) => request("/loans", { method: "POST", body: JSON.stringify({ bookId, memberId }) }),
  returnBook: (id) => request(`/loans/${id}/return`, { method: "PATCH" }),
};