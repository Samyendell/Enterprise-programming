import { useState } from 'react';

const MAX_LEN = 60;

/** Truncate text that exceeds MAX_LEN characters */
function truncate(text) {
  if (!text || text.length <= MAX_LEN) return text || '';
  return text.substring(0, MAX_LEN) + '…';
}

/** Single row in the book table — clickable to view details, with inline delete confirmation */
export default function BookRow({ book, onEdit, onDelete, onView }) {
  const [confirming, setConfirming] = useState(false);

  if (confirming) {
    return (
      <tr className="confirm-row">
        <td colSpan={8} className="py-2 px-3">
          <div className="d-flex justify-content-between align-items-center">
            <span>Delete <strong>{book.title}</strong>?</span>
            <div className="d-flex gap-2">
              <button className="btn btn-sm btn-outline-secondary" onClick={() => setConfirming(false)}>
                Go Back
              </button>
              <button className="btn btn-sm btn-danger" onClick={() => { setConfirming(false); onDelete(book.id); }}>
                Confirm Removal
              </button>
            </div>
          </div>
        </td>
      </tr>
    );
  }

  return (
    <tr className="clickable-row" onClick={() => onView(book)} style={{ cursor: 'pointer' }}>
      <td>{book.id}</td>
      <td><strong>{book.title}</strong></td>
      <td>{truncate(book.author)}</td>
      <td>{book.date}</td>
      <td><span className="genre-tag">{truncate(book.genres)}</span></td>
      <td className="text-muted small">{truncate(book.characters)}</td>
      <td className="synopsis-cell text-muted small">{truncate(book.synopsis)}</td>
      <td className="text-center text-nowrap" onClick={(e) => e.stopPropagation()}>
        <button className="btn btn-sm btn-del me-1" onClick={() => setConfirming(true)}>Remove</button>
        <button className="btn btn-sm btn-edit" onClick={() => onEdit(book.id)}>Modify</button>
      </td>
    </tr>
  );
}
