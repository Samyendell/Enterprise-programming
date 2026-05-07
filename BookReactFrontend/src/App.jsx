import { useState, useEffect, useCallback } from 'react';
import NavBar from './components/NavBar';
import SearchBar from './components/SearchBar';
import BookTable from './components/BookTable';
import RawViewer from './components/RawViewer';
import BookFormModal from './components/BookFormModal';
import BookDetailModal from './components/BookDetailModal';
import { bookService } from './services/bookService';
import { validateBook } from './utils/validation';
import './App.css';

/**
 * App - Root component for the Book Library React frontend.
 *
 * Architecture:
 *   - App holds all application state (books, format, search query, modals)
 *   - Child components are pure/presentational where possible
 *   - API calls go through bookService.js using plain fetch() with async/await
 *   - Format negotiation uses both ?format= query param and Accept header,
 *     demonstrating HTTP content negotiation
 *   - Separate parser functions per format (JSON, XML, Text) keep parsing
 *     logic isolated - adding a new format only means a new parser
 *   - Client-side validation mirrors server-side Validation.validateBook()
 */

const PAGE_SIZE = 10;

export default function App() {
  // NOTE: core data state — books array and the currently selected response format
  const [books, setBooks] = useState([]);
  const [format, setFormat] = useState('json');
  const [loading, setLoading] = useState(true);
  const [tableErr, setTableErr] = useState('');

  // NOTE: raw API response displayed in the RawViewer panel
  const [rawMethod, setRawMethod] = useState('');
  const [rawText, setRawText] = useState('');

  // NOTE: sorting state — which column and direction
  const [sortField, setSortField] = useState('title');
  const [sortOrder, setSortOrder] = useState('asc');

  // NOTE: pagination state — read from server response headers
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [totalBooks, setTotalBooks] = useState(0);

  // NOTE: search query persisted so it survives page/sort changes
  const [searchQuery, setSearchQuery] = useState('');

  // NOTE: modal state for add/edit form
  const [modalShow, setModalShow] = useState(false);
  const [modalMode, setModalMode] = useState('add');
  const [modalInit, setModalInit] = useState(null);
  const [modalErrors, setModalErrors] = useState([]);
  const [editId, setEditId] = useState(null);

  // NOTE: detail modal state — opens when user clicks a table row
  const [detailBook, setDetailBook] = useState(null);
  const [detailShow, setDetailShow] = useState(false);

  // NOTE: fetches a page of books from the API with current sort/search/format.
  // useCallback ensures this function is recreated when the format changes,
  // so the effect below re-fetches whenever the user switches format.
  const loadBooks = useCallback(async (page, sort, order, search) => {
    setLoading(true);
    setTableErr('');
    try {
      const result = await bookService.getBooks(format, sort, order, page, PAGE_SIZE, search);
      setBooks(result.books);
      setTotalBooks(result.totalCount);
      setTotalPages(result.totalPages);
      setCurrentPage(result.currentPage);
      setRawText(result.rawText);
      setRawMethod(result.rawMethod);
    } catch (err) {
      setTableErr('Could not connect to API: ' + err.message);
    }
    setLoading(false);
  }, [format]);

  // NOTE: re-fetch books whenever the format changes (JSON/XML/Text)
  useEffect(() => { loadBooks(currentPage, sortField, sortOrder, searchQuery); }, [format]);

  // NOTE: resets search and goes back to page 1 showing all books
  const loadAll = () => {
    setSearchQuery('');
    setCurrentPage(1);
    loadBooks(1, sortField, sortOrder, '');
  };

  // NOTE: called by SortHeader when user clicks a column to sort
  const handleSort = (field, order) => {
    setSortField(field);
    setSortOrder(order);
    setCurrentPage(1);
    loadBooks(1, field, order, searchQuery);
  };

  // NOTE: called by Pagination when user clicks a page number
  const handlePageChange = (page) => {
    setCurrentPage(page);
    loadBooks(page, sortField, sortOrder, searchQuery);
  };

  // NOTE: triggers a search query — resets to page 1
  const handleSearch = (q) => {
    if (!q.trim()) { loadAll(); return; }
    setSearchQuery(q);
    setCurrentPage(1);
    loadBooks(1, sortField, sortOrder, q);
  };

  // NOTE: opens the add-book modal with empty form fields
  const openAdd = () => {
    setModalMode('add');
    setEditId(null);
    setModalInit({ title: '', author: '', date: '', genres: '', characters: '', synopsis: '' });
    setModalErrors([]);
    setModalShow(true);
  };

  // NOTE: fetches the book by ID from the API, then opens the edit modal
  // with the book's current values pre-filled in the form
  const openEdit = async (id) => {
    try {
      const result = await bookService.getBook(id, format);
      setRawText(result.rawText);
      setRawMethod(result.rawMethod);
      if (!result.book) { setTableErr('Could not load book for editing.'); return; }
      setModalMode('edit');
      setEditId(id);
      setModalInit({
        title: result.book.title || '', author: result.book.author || '',
        date: result.book.date || '', genres: result.book.genres || '',
        characters: result.book.characters || '', synopsis: result.book.synopsis || '',
      });
      setModalErrors([]);
      setModalShow(true);
    } catch (err) {
      setTableErr('Error loading book: ' + err.message);
    }
  };

  // NOTE: validates form data client-side, then sends POST (add) or PUT (edit)
  // to the API in the currently selected format (JSON, XML, or Text)
  const handleSave = async (formData) => {
    const book = {
      id: editId || 0,
      title: formData.title.trim(), author: formData.author.trim(),
      date: formData.date.trim(), genres: formData.genres.trim(),
      characters: formData.characters.trim(), synopsis: formData.synopsis.trim(),
    };
    const errs = validateBook(book);
    if (errs.length) { setModalErrors(errs); return; }

    const isEdit = modalMode === 'edit';
    try {
      const result = isEdit
        ? await bookService.updateBook(editId, book, format)
        : await bookService.createBook(book, format);
      setRawText(result.text);
      setRawMethod(result.rawMethod);
      if (result.ok) {
        setModalShow(false);
        loadBooks(currentPage, sortField, sortOrder, searchQuery);
      } else {
        setModalErrors(['Server: ' + bookService.extractErr(result.text, format)]);
      }
    } catch (err) {
      setModalErrors(['Network error: ' + err.message]);
    }
  };

  // NOTE: sends DELETE request — no confirmation here because BookRow
  // handles inline confirmation before calling this
  const handleDelete = async (id) => {
    try {
      const result = await bookService.deleteBook(id, format);
      setRawText(result.text);
      setRawMethod(result.rawMethod);
      loadBooks(currentPage, sortField, sortOrder, searchQuery);
    } catch (err) {
      setTableErr('Delete failed: ' + err.message);
    }
  };

  return (
    <div>
      <NavBar format={format} onFormatChange={setFormat} onAddClick={openAdd} />
      <div className="container-fluid px-4">
        <div className="row g-4">
          <div className="col-xl-8">
            <SearchBar onSearch={handleSearch} onClear={loadAll} />
            <BookTable
              books={books} loading={loading} error={tableErr}
              format={format} onEdit={openEdit} onDelete={handleDelete}
              onView={(book) => { setDetailBook(book); setDetailShow(true); }}
              sortField={sortField} sortOrder={sortOrder} onSort={handleSort}
              currentPage={currentPage} totalPages={totalPages}
              totalBooks={totalBooks} onPageChange={handlePageChange}
            />
          </div>
          <div className="col-xl-4">
            <RawViewer method={rawMethod} text={rawText} />
          </div>
        </div>
      </div>
      <BookFormModal
        show={modalShow} mode={modalMode} initial={modalInit}
        errors={modalErrors} onSave={handleSave}
        onClose={() => setModalShow(false)}
      />
      <BookDetailModal
        book={detailBook} show={detailShow}
        onClose={() => setDetailShow(false)}
        onEdit={openEdit} onDelete={handleDelete}
      />
    </div>
  );
}
