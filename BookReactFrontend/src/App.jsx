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
  const [books, setBooks] = useState([]);
  const [format, setFormat] = useState('json');
  const [loading, setLoading] = useState(true);
  const [tableErr, setTableErr] = useState('');
  const [rawMethod, setRawMethod] = useState('');
  const [rawText, setRawText] = useState('');

  const [sortField, setSortField] = useState('title');
  const [sortOrder, setSortOrder] = useState('asc');

  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [totalBooks, setTotalBooks] = useState(0);

  const [searchQuery, setSearchQuery] = useState('');

  const [modalShow, setModalShow] = useState(false);
  const [modalMode, setModalMode] = useState('add');
  const [modalInit, setModalInit] = useState(null);
  const [modalErrors, setModalErrors] = useState([]);
  const [editId, setEditId] = useState(null);

  const [detailBook, setDetailBook] = useState(null);
  const [detailShow, setDetailShow] = useState(false);

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

  useEffect(() => { loadBooks(currentPage, sortField, sortOrder, searchQuery); }, [format]);

  const loadAll = () => {
    setSearchQuery('');
    setCurrentPage(1);
    loadBooks(1, sortField, sortOrder, '');
  };

  const handleSort = (field, order) => {
    setSortField(field);
    setSortOrder(order);
    setCurrentPage(1);
    loadBooks(1, field, order, searchQuery);
  };

  const handlePageChange = (page) => {
    setCurrentPage(page);
    loadBooks(page, sortField, sortOrder, searchQuery);
  };

  const handleSearch = (q) => {
    if (!q.trim()) { loadAll(); return; }
    setSearchQuery(q);
    setCurrentPage(1);
    loadBooks(1, sortField, sortOrder, q);
  };

  const openAdd = () => {
    setModalMode('add');
    setEditId(null);
    setModalInit({ title: '', author: '', date: '', genres: '', characters: '', synopsis: '' });
    setModalErrors([]);
    setModalShow(true);
  };

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
