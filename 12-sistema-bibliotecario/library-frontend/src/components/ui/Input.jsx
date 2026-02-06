export default function Input({ label, ...props }) {
  return (
    <div className="mb-4">
      <label className="block text-xs font-semibold text-gray-500 mb-1.5 uppercase tracking-wider">
        {label}
      </label>
      <input
        {...props}
        className="w-full px-3.5 py-2.5 border-[1.5px] border-gray-200 rounded-xl text-sm
                   bg-gray-50 outline-none transition-all duration-200
                   focus:border-gray-900 focus:bg-white"
      />
    </div>
  );
}