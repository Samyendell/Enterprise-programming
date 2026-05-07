import SortHeader from './SortHeader';
import BookRow from './BookRow';
import Pagination from './Pagination';

/**
 * BookTable — the main data table displaying all books.
 *
 * NOTE: this is a presentational component — it receives all data
 * and callbacks as props and doesn't manage any state itself.
 * It composes SortHeader (for clickable column headers), BookRow
 * (for each book record), and Pagination (for page navigation).
 * The format pill badge shows which data format is currently active.
 *
 * Props:
 *   books, loading, error  - data state from the parent
 *   format                 - current format for the format pill
 *   onEdit, onDelete, onView - callbacks forwarded to each BookRow
 *   sortField, sortOrder, onSort - sorting state and handler
 *   currentPage, totalPages, totalBooks, onPageChange - pagination
 */
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
