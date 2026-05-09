import { useState, useEffect, useRef } from 'react';
import { Modal } from 'bootstrap';

export default function BookFormModal({ show, mode, initial, errors, onSave, onClose }) {
    const [form, setForm] = useState({ title: '', author: '', date: '', genres: '', characters: '', synopsis: '' });
    const modalRef = useRef(null);
    const bsModal = useRef(null);

    useEffect(() => {
        if (modalRef.current && !bsModal.current) {
            bsModal.current = new Modal(modalRef.current, { backdrop: 'static' });
        }
    }, []);

    useEffect(() => {
        if (show) {
            setForm(initial || { title: '', author: '', date: '', genres: '', characters: '', synopsis: '' });
            bsModal.current?.show();
        } else {
            bsModal.current?.hide();
        }
    }, [show, initial]);

    const handleChange = (field) => (e) => setForm({ ...form, [field]: e.target.value });
    const handleSubmit = (e) => { e.preventDefault(); onSave(form); };

    return (
        <div className="modal fade" ref={modalRef} tabIndex={-1} data-bs-backdrop="static">
            <div className="modal-dialog modal-lg">
                <div className="modal-content">
                    <div className="modal-header">
                        <h5 className="modal-title">{mode === 'edit' ? 'Edit Book' : 'Add Book'}</h5>
                        <button type="button" className="btn-close" onClick={onClose}></button>
                    </div>
                    <form onSubmit={handleSubmit}>
                        <div className="modal-body">
                            {errors && errors.length > 0 && (
                                <div className="alert alert-danger py-2">
                                    {errors.map((e, i) => <div key={i}>{e}</div>)}
                                </div>
                            )}
                            <div className="row g-3">
                                <div className="col-md-6">
                                    <label className="form-label">Title <span className="text-danger">*</span></label>
                                    <input type="text" className="form-control" maxLength={150} value={form.title} onChange={handleChange('title')} />
                                </div>
                                <div className="col-md-6">
                                    <label className="form-label">Author <span className="text-danger">*</span></label>
                                    <input type="text" className="form-control" maxLength={150} value={form.author} onChange={handleChange('author')} />
                                </div>
                                <div className="col-md-4">
                                    <label className="form-label">Date <span className="text-danger">*</span></label>
                                    <input type="text" className="form-control" maxLength={20} placeholder="e.g. 2003" value={form.date} onChange={handleChange('date')} />
                                </div>
                                <div className="col-md-8">
                                    <label className="form-label">Genres <span className="text-danger">*</span></label>
                                    <input type="text" className="form-control" maxLength={50} value={form.genres} onChange={handleChange('genres')} />
                                </div>
                                <div className="col-12">
                                    <label className="form-label">Characters <span className="text-danger">*</span></label>
                                    <input type="text" className="form-control" maxLength={200} placeholder="Comma-separated character names" value={form.characters} onChange={handleChange('characters')} />
                                </div>
                                <div className="col-12">
                                    <label className="form-label">Synopsis <span className="text-danger">*</span></label>
                                    <textarea className="form-control" rows={3} value={form.synopsis} onChange={handleChange('synopsis')} />
                                </div>
                            </div>
                        </div>
                        <div className="modal-footer">
                            <button type="button" className="btn btn-secondary" onClick={onClose}>Discard</button>
                            <button type="submit" className="btn text-white" style={{ background: 'var(--accent)' }}>
                                {mode === 'edit' ? 'Save Changes' : 'Submit'}
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
}
