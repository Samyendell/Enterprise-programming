import { useEffect, useRef } from 'react';
import { Modal } from 'bootstrap';

/**
 * BookDetailModal — read-only modal showing all fields for a single book.
 *
 * NOTE: this modal opens when the user clicks a row in the table. It
 * displays the full untruncated data for every field so the user can
 * read long character lists and synopses. The footer provides quick
 * access to Remove and Modify actions without going back to the table.
 *
 * The modal DOM is always rendered (not conditionally removed) so that
 * Bootstrap's Modal JS instance can be created once on mount. Content
 * inside the body is conditionally rendered based on the `book` prop.
 *
 * Props:
 *   book     - the book object to display (or null)
 *   show     - boolean controlling modal visibility
 *   onClose  - callback to close the modal
 *   onEdit   - callback(id) to open the edit modal for this book
 *   onDelete - callback(id) to delete this book
 */
export default function BookDetailModal({ book, show, onClose, onEdit, onDelete }) {
  const modalRef = useRef(null);
  const bsModal = useRef(null);

  useEffect(() => {
    if (modalRef.current && !bsModal.current) {
      bsModal.current = new Modal(modalRef.current);
      // Sync close when user clicks backdrop or presses Escape
      modalRef.current.addEventListener('hidden.bs.modal', () => onClose());
    }
  }, []);

  useEffect(() => {
    if (show && book) {
      bsModal.current?.show();
    } else {
      bsModal.current?.hide();
    }
  }, [show, book]);

  return (
    <div className="modal fade" ref={modalRef} tabIndex={-1}>
      <div className="modal-dialog modal-lg">
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title">{book?.title || ''}</h5>
            <button type="button" className="btn-close" onClick={onClose}></button>
          </div>
          <div className="modal-body">
            {book && (
              <dl className="row mb-0">
                <dt className="col-sm-3">ID</dt>
                <dd className="col-sm-9">{book.id}</dd>

                <dt className="col-sm-3">Title</dt>
                <dd className="col-sm-9">{book.title}</dd>

                <dt className="col-sm-3">Author</dt>
                <dd className="col-sm-9">{book.author}</dd>

                <dt className="col-sm-3">Date</dt>
                <dd className="col-sm-9">{book.date}</dd>

                <dt className="col-sm-3">Genres</dt>
                <dd className="col-sm-9">{book.genres}</dd>

                <dt className="col-sm-3">Characters</dt>
                <dd className="col-sm-9" style={{ whiteSpace: 'pre-wrap' }}>{book.characters}</dd>

                <dt className="col-sm-3">Synopsis</dt>
                <dd className="col-sm-9" style={{ whiteSpace: 'pre-wrap' }}>{book.synopsis}</dd>
              </dl>
            )}
          </div>
          <div className="modal-footer">
            <button className="btn btn-sm btn-del" onClick={() => { onClose(); if (book) onDelete(book.id); }}>Remove</button>
            <button className="btn btn-sm btn-edit" onClick={() => { onClose(); if (book) onEdit(book.id); }}>Modify</button>
            <button className="btn btn-sm btn-secondary" onClick={onClose}>Close</button>
          </div>
        </div>
      </div>
    </div>
  );
}
