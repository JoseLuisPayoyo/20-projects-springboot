export default function EmptyState({ icon: Icon, message }) {
  return (
    <div className="text-center py-16 text-gray-400">
      <div className="mb-3 opacity-40 flex justify-center">
        <Icon size={32} />
      </div>
      <p className="text-sm">{message}</p>
    </div>
  );
}