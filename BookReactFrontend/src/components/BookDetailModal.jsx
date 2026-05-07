import { useEffect, useRef } from 'react';
import { Modal } from 'bootstrap';

/** Read-only modal showing all fields for a single book */
export default function BookDetailModal({ book, show, onClose, onEdit, onDelete }) {
  const modalRef = useRef(null);
  const bsModal = useRef(null);

  useEffect(() => {
    if (modalRef.current && !bsModal.current) {
      bsModal.current = new Modal(modalRef.current);
    }
  }, []);

  useEffect(() => {
    if (show && book) bsModal.current?.show();
    else bsModal.current?.hide();
  }, [show, book]);

  if (!book) return null;

  return (
    <div className="modal fade" ref={modalRef} tabIndex={-1} onClick={(e) => { if (e.target === modalRef.current) onClose(); }}>
      <div className="modal-dialog modal-lg">
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title">{book.title}</h5>
            <button type="button" className="btn-close" onClick={onClose}></button>
          </div>
          <div className="modal-body">
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
          </div>
          <div className="modal-footer">
            <button className="btn btn-sm btn-del" onClick={() => { onClose(); onDelete(book.id); }}>Remove</button>
            <button className="btn btn-sm btn-edit" onClick={() => { onClose(); onEdit(book.id); }}>Modify</button>
            <button className="btn btn-secondary" onClick={onClose}>Close</button>
          </div>
        </div>
      </div>
    </div>
  );
}
