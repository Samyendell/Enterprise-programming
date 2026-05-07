import SortHeader from './SortHeader';
import BookRow from './BookRow';
import Pagination from './Pagination';

/** Books table with sortable headers, loading/error states, and pagination */
export default function BookTable({
  books, loading, error, format, onEdit, onDelete, onView,
  sortField, sortOrder, onSort,
  currentPage, totalPages, totalBooks, onPageChange,
}) {
  return (
    <div className="card border-0 shadow-sm">
      <div className="card-header bg-white d-flex justify-content-between align-items-center py-3">
        <h6 className="mb-0">
          All Books <span className="text-muted fw-normal ms-1">({totalBooks})</span>
        </h6>
        <span className="format-pill">{format.toUpperCase()}</span>
      </div>
      <div className="table-responsive">
        <table className="table table-hover align-middle mb-0">
          <thead className="tbl-header">
            <tr>
              <SortHeader label="#" field="id" sortField={sortField} sortOrder={sortOrder} onSort={onSort} />
              <SortHeader label="Title" field="title" sortField={sortField} sortOrder={sortOrder} onSort={onSort} />
              <SortHeader label="Author" field="author" sortField={sortField} sortOrder={sortOrder} onSort={onSort} />
              <SortHeader label="Date" field="date" sortField={sortField} sortOrder={sortOrder} onSort={onSort} />
              <SortHeader label="Genres" field="genres" sortField={sortField} sortOrder={sortOrder} onSort={onSort} />
              <th>Characters</th>
              <th>Synopsis</th>
              <th className="text-center" style={{ width: '140px' }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan={8} className="text-center text-muted py-4">Loading...</td></tr>
            ) : error ? (
              <tr><td colSpan={8} className="text-center text-danger py-4">{error}</td></tr>
            ) : books.length === 0 ? (
              <tr><td colSpan={8} className="text-center text-muted py-4">No books found.</td></tr>
            ) : (
              books.map((b) => <BookRow key={b.id} book={b} onEdit={onEdit} onDelete={onDelete} onView={onView} />)
            )}
          </tbody>
        </table>
      </div>
      <Pagination currentPage={currentPage} totalPages={totalPages} totalBooks={totalBooks} onPageChange={onPageChange} />
    </div>
  );
}
