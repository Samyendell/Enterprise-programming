export default function Pagination({ currentPage, totalPages, totalBooks, onPageChange }) {
    if (totalPages <= 1) return null;

    const WINDOW = 5;
    let start = Math.max(1, currentPage - Math.floor(WINDOW / 2));
    let end = Math.min(totalPages, start + WINDOW - 1);
    if (end - start < WINDOW - 1) start = Math.max(1, end - WINDOW + 1);
    const pages = [];
    for (let i = start;i <= end;i++) pages.push(i);

    return (
        <div className="card-footer bg-white d-flex justify-content-between align-items-center py-2">
            <small className="text-muted">
                Page {currentPage} of {totalPages} ({totalBooks} books total)
            </small>
            <nav>
                <ul className="pagination pagination-sm mb-0">
                    <li className={`page-item ${currentPage <= 1 ? 'disabled' : ''}`}>
                        <a className="page-link" href="#" onClick={(e) => { e.preventDefault(); onPageChange(currentPage - 1); }}>
                            Previous
                        </a>
                    </li>
                    {start > 1 && (
                        <>
                            <li className="page-item">
                                <a className="page-link" href="#" onClick={(e) => { e.preventDefault(); onPageChange(1); }}>1</a>
                            </li>
                            {start > 2 && <li className="page-item disabled"><span className="page-link">…</span></li>}
                        </>
                    )}
                    {pages.map((p) => (
                        <li key={p} className={`page-item ${p === currentPage ? 'active' : ''}`}>
                            <a className="page-link" href="#" onClick={(e) => { e.preventDefault(); onPageChange(p); }}>
                                {p}
                            </a>
                        </li>
                    ))}
                    {end < totalPages && (
                        <>
                            {end < totalPages - 1 && <li className="page-item disabled"><span className="page-link">…</span></li>}
                            <li className="page-item">
                                <a className="page-link" href="#" onClick={(e) => { e.preventDefault(); onPageChange(totalPages); }}>{totalPages}</a>
                            </li>
                        </>
                    )}
                    <li className={`page-item ${currentPage >= totalPages ? 'disabled' : ''}`}>
                        <a className="page-link" href="#" onClick={(e) => { e.preventDefault(); onPageChange(currentPage + 1); }}>
                            Next
                        </a>
                    </li>
                </ul>
            </nav>
        </div>
    );
}
