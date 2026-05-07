/**
 * SortHeader — clickable column header that toggles sort direction.
 *
 * NOTE: when the user clicks a header, it tells the parent which field
 * and direction to sort by. The parent then requests a new sorted page
 * from the API. An arrow (▲/▼) indicates the currently active sort.
 *
 * Props:
 *   label     - display text for the column
 *   field     - API field name to sort by (e.g. 'title', 'author')
 *   sortField - currently active sort field
 *   sortOrder - current sort direction ('asc' or 'desc')
 *   onSort    - callback(field, order) when clicked
 */
export default function SortHeader({ label, field, sortField, sortOrder, onSort }) {
  const active = sortField === field;
  // NOTE: show ▲ for ascending, ▼ for descending, nothing if not active
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
