import { useState } from 'react';

/**
 * SearchBar — text input for searching books by title, author or genre.
 *
 * NOTE: uses controlled input with local state for the query string.
 * Supports both button click and Enter key to trigger the search.
 * The "Reset" button clears the input and reloads all books.
 *
 * Props:
 *   onSearch - callback with the search query string
 *   onClear  - callback to reset search and reload all books
 */
export default function SearchBar({ onSearch, onClear }) {
  const [q, setQ] = useState('');
  // NOTE: allow searching by pressing Enter without clicking the button
  const handleKey = (e) => { if (e.key === 'Enter') onSearch(q); };
  return (
    <div className="input-group mb-3">
      <input
        type="text"
        className="form-control"
        placeholder="Search by title, author or genre..."
        maxLength={200}
        value={q}
        onChange={(e) => setQ(e.target.value)}
        onKeyUp={handleKey}
      />
      <button className="btn btn-outline-secondary" onClick={() => onSearch(q)}>Go</button>
      <button className="btn btn-outline-secondary" onClick={() => { setQ(''); onClear(); }}>Reset</button>
    </div>
  );
}
