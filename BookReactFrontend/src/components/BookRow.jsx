import { useState } from 'react';

/** Single row in the book table — handles inline delete confirmation */
export default function BookRow({ book, onEdit, onDelete }) {
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
    <tr>
      <td>{book.id}</td>
      <td><strong>{book.title}</strong></td>
      <td>{book.author}</td>
      <td>{book.date}</td>
      <td><span className="genre-tag">{book.genres}</span></td>
      <td className="text-muted small">{book.characters}</td>
      <td className="synopsis-cell text-muted small" title={book.synopsis || ''}>
        {book.synopsis}
      </td>
      <td className="text-center text-nowrap">
        <button className="btn btn-sm btn-del me-1" onClick={() => setConfirming(true)}>Remove</button>
        <button className="btn btn-sm btn-edit" onClick={() => onEdit(book.id)}>Modify</button>
      </td>
    </tr>
  );
}
