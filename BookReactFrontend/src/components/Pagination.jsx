/** Pagination controls */
export default function Pagination({ currentPage, totalPages, totalBooks, onPageChange }) {
  if (totalPages <= 1) return null;
  const pages = [];
  for (let i = 1; i <= totalPages; i++) pages.push(i);
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
          {pages.map((p) => (
            <li key={p} className={`page-item ${p === currentPage ? 'active' : ''}`}>
              <a className="page-link" href="#" onClick={(e) => { e.preventDefault(); onPageChange(p); }}>
                {p}
              </a>
            </li>
          ))}
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
