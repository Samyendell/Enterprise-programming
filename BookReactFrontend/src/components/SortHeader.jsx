/** Sortable column header — clicking toggles asc/desc */
export default function SortHeader({ label, field, sortField, sortOrder, onSort }) {
  const active = sortField === field;
  const arrow = active ? (sortOrder === 'asc' ? '\u25B2' : '\u25BC') : '';
  return (
    <th>
      <a
        href="#"
        onClick={(e) => {
          e.preventDefault();
          onSort(field, active && sortOrder === 'asc' ? 'desc' : 'asc');
        }}
      >
        {label} {arrow && <span className="sort-arrow">{arrow}</span>}
      </a>
    </th>
  );
}
