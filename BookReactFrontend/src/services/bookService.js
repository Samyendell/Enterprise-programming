/**
 * Book API Service
 *
 * Handles all communication with the BookRestful API.
 * Supports JSON, XML and Text format negotiation via ?format= param and Accept header.
 * Pagination info is read from custom response headers (X-Total-Count, X-Total-Pages, etc.)
 *
 * NOTE: this service demonstrates HTTP content negotiation — the client tells the
 * server which format it wants via the Accept header and ?format= query param,
 * and the server responds in that format. The same approach is used for POST/PUT
 * requests where the Content-Type header tells the server what format the body is in.
 */

// NOTE: base URL for the RESTful API — update this if deploying to a different host
const API = 'http://localhost:8080/BookRestful/api/books';

// NOTE: maps format names to their MIME types for the Accept header
const ACCEPT = {
    json: 'application/json',
    xml: 'application/xml',
    text: 'text/plain',
};

// NOTE: maps format names to their Content-Type for POST/PUT request bodies
const CONTENT_TYPE = {
    json: 'application/json',
    xml: 'application/xml',
    text: 'text/plain',
};

/* ── Parsers ──
 * NOTE: separate parser for each format keeps parsing logic isolated.
 * Adding a new format (e.g. CSV) only means writing a new parser function.
 */

/** Parse an XML string containing multiple <book> elements into an array */
function parseBooksXml(xml) {
    const doc = new DOMParser().parseFromString(xml, 'application/xml');
    return Array.from(doc.querySelectorAll('book')).map(xmlNodeToBook);
}

/** Parse an XML string containing a single <book> element */
function parseBookXml(xml) {
    const doc = new DOMParser().parseFromString(xml, 'application/xml');
    const n = doc.querySelector('book');
    return n ? xmlNodeToBook(n) : null;
}

/** Extract book fields from a single XML <book> DOM node */
function xmlNodeToBook(node) {
    const g = (tag) => node.querySelector(tag)?.textContent || '';
    return {
        id: parseInt(g('id')) || 0,
        title: g('title'),
        author: g('author'),
        date: g('date'),
        genres: g('genres'),
        characters: g('characters'),
        synopsis: g('synopsis'),
    };
}

/** Parse pipe-delimited text (one book per line) into an array of book objects */
function parseBooksText(text) {
    return text.trim().split('\n').filter((l) => l.trim()).map(parseBookText).filter(Boolean);
}

/** Parse a single pipe-delimited line: id|title|author|date|genres|characters|synopsis */
function parseBookText(line) {
    const p = line.split('|');
    if (p.length < 7) return null;
    return {
        id: parseInt(p[0]) || 0, title: p[1], author: p[2], date: p[3],
        genres: p[4], characters: p[5], synopsis: p[6],
    };
}

/**
 * Dispatch to the correct parser based on the selected format.
 * Returns an array of book objects regardless of which format was received.
 */
function parseBooks(text, fmt) {
    try {
        if (fmt === 'json') return JSON.parse(text);
        if (fmt === 'xml') return parseBooksXml(text);
        if (fmt === 'text') return parseBooksText(text);
    } catch (e) { console.error('Parse error:', e); }
    return [];
}

/** Parse a single book response — dispatches to the correct format parser */
function parseBook(text, fmt) {
    try {
        if (fmt === 'json') return JSON.parse(text);
        if (fmt === 'xml') return parseBookXml(text);
        if (fmt === 'text') return parseBookText(text.split('\n')[0]);
    } catch (e) { console.error('Parse error:', e); }
    return null;
}

/**
 * Build the POST/PUT request body in the selected format.
 * NOTE: all 3 formats are supported for sending data to the server:
 *   - JSON: standard JSON object
 *   - XML: well-formed XML document with escaped special characters
 *   - Text: pipe-delimited string (id|title|author|date|genres|characters|synopsis)
 * This demonstrates full round-trip data exchange in every format.
 */
function buildBody(book, fmt) {
    if (fmt === 'xml') {
        // NOTE: escape XML special characters to prevent malformed XML
        const esc = (s) =>
            String(s ?? '').replace(/&/g, '&amp;').replace(/</g, '&lt;')
                .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
        return `<?xml version="1.0" encoding="UTF-8"?>
<book>
  <id>${book.id}</id>
  <title>${esc(book.title)}</title>
  <author>${esc(book.author)}</author>
  <date>${esc(book.date)}</date>
  <genres>${esc(book.genres)}</genres>
  <characters>${esc(book.characters)}</characters>
  <synopsis>${esc(book.synopsis)}</synopsis>
</book>`;
    }
    if (fmt === 'text') {
        // NOTE: pipe-delimited format matching the server's text parser
        // format: id|title|author|date|genres|characters|synopsis
        return `${book.id}|${book.title}|${book.author}|${book.date}|${book.genres}|${book.characters}|${book.synopsis}`;
    }
    // NOTE: default to JSON — the most common exchange format
    return JSON.stringify(book);
}

/** Extract a human-readable error message from an API error response */
function extractErr(text, fmt) {
    try {
        if (fmt === 'json') return JSON.parse(text)?.error?.message || text;
        if (fmt === 'xml') {
            const doc = new DOMParser().parseFromString(text, 'application/xml');
            return doc.querySelector('message')?.textContent || text;
        }
    } catch (_) { }
    return text;
}

/* ── API calls ──
 * NOTE: each function returns the raw response text alongside parsed data
 * so the RawViewer component can display exactly what the server sent back.
 */

/**
 * Fetch a paginated, sorted, optionally filtered list of books.
 * Pagination metadata is read from custom response headers set by the server.
 */
async function getBooks(format, sort, order, page, size, search) {
    let url = `${API}?format=${format}&sort=${sort}&order=${order}&page=${page}&size=${size}`;
    if (search) url += `&search=${encodeURIComponent(search)}`;

    const res = await fetch(url, { headers: { Accept: ACCEPT[format] } });
    const text = await res.text();
    return {
        books: parseBooks(text, format),
        totalCount: parseInt(res.headers.get('X-Total-Count')) || 0,
        totalPages: parseInt(res.headers.get('X-Total-Pages')) || 1,
        currentPage: parseInt(res.headers.get('X-Current-Page')) || 1,
        rawText: text,
        rawMethod: 'GET ' + url,
    };
}

/** Fetch a single book by ID in the selected format */
async function getBook(id, format) {
    const url = `${API}?id=${id}&format=${format}`;
    const res = await fetch(url, { headers: { Accept: ACCEPT[format] } });
    const text = await res.text();
    return { book: parseBook(text, format), rawText: text, rawMethod: 'GET ' + url };
}

/** Create a new book (POST) — sends body in the currently selected format */
async function createBook(book, format) {
    const url = `${API}?format=${format}`;
    const body = buildBody(book, format);
    // NOTE: Content-Type tells the server what format the request body is in
    const ctype = CONTENT_TYPE[format];
    const res = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': ctype, Accept: ACCEPT[format] },
        body,
    });
    const text = await res.text();
    return { ok: res.ok || res.status === 201, text, rawMethod: 'POST ' + url };
}

/** Update an existing book (PUT) — sends body in the currently selected format */
async function updateBook(id, book, format) {
    const url = `${API}?id=${id}&format=${format}`;
    const body = buildBody(book, format);
    // NOTE: Content-Type tells the server what format the request body is in
    const ctype = CONTENT_TYPE[format];
    const res = await fetch(url, {
        method: 'PUT',
        headers: { 'Content-Type': ctype, Accept: ACCEPT[format] },
        body,
    });
    const text = await res.text();
    return { ok: res.ok, text, rawMethod: 'PUT ' + url };
}

/** Delete a book by ID — DELETE method, server returns 204 No Content on success */
async function deleteBook(id, format) {
    const url = `${API}?id=${id}&format=${format}`;
    const res = await fetch(url, { method: 'DELETE' });
    const text = res.status === 204 ? '204 No Content' : await res.text();
    return { ok: res.ok || res.status === 204, text, rawMethod: 'DELETE ' + url };
}

export const bookService = { getBooks, getBook, createBook, updateBook, deleteBook, extractErr };
