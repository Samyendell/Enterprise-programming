/**
 * Book API Service
 *
 * Handles all communication with the BookRestful API.
 * Supports JSON, XML and Text format negotiation via ?format= param and Accept header.
 * Pagination info is read from response headers (X-Total-Count, X-Total-Pages, etc.)
 */

const API = 'http://localhost:8080/BookRestful/api/books';

const ACCEPT = {
    json: 'application/json',
    xml: 'application/xml',
    text: 'text/plain',
};

/* ── Parsers ── */

function parseBooksXml(xml) {
    const doc = new DOMParser().parseFromString(xml, 'application/xml');
    return Array.from(doc.querySelectorAll('book')).map(xmlNodeToBook);
}

function parseBookXml(xml) {
    const doc = new DOMParser().parseFromString(xml, 'application/xml');
    const n = doc.querySelector('book');
    return n ? xmlNodeToBook(n) : null;
}

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

function parseBooksText(text) {
    return text.trim().split('\n').filter((l) => l.trim()).map(parseBookText).filter(Boolean);
}

function parseBookText(line) {
    const p = line.split('|');
    if (p.length < 7) return null;
    return {
        id: parseInt(p[0]) || 0, title: p[1], author: p[2], date: p[3],
        genres: p[4], characters: p[5], synopsis: p[6],
    };
}

function parseBooks(text, fmt) {
    try {
        if (fmt === 'json') return JSON.parse(text);
        if (fmt === 'xml') return parseBooksXml(text);
        if (fmt === 'text') return parseBooksText(text);
    } catch (e) { console.error('Parse error:', e); }
    return [];
}

function parseBook(text, fmt) {
    try {
        if (fmt === 'json') return JSON.parse(text);
        if (fmt === 'xml') return parseBookXml(text);
        if (fmt === 'text') return parseBookText(text.split('\n')[0]);
    } catch (e) { console.error('Parse error:', e); }
    return null;
}

/** Build the POST/PUT body — XML when xml format selected, JSON otherwise */
function buildBody(book, fmt) {
    if (fmt === 'xml') {
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
    return JSON.stringify(book);
}

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

/* ── API calls ── */

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

async function getBook(id, format) {
    const url = `${API}?id=${id}&format=${format}`;
    const res = await fetch(url, { headers: { Accept: ACCEPT[format] } });
    const text = await res.text();
    return { book: parseBook(text, format), rawText: text, rawMethod: 'GET ' + url };
}

async function createBook(book, format) {
    const url = `${API}?format=${format}`;
    const body = buildBody(book, format);
    const ctype = format === 'xml' ? 'application/xml' : 'application/json';
    const res = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': ctype, Accept: ACCEPT[format] },
        body,
    });
    const text = await res.text();
    return { ok: res.ok || res.status === 201, text, rawMethod: 'POST ' + url };
}

async function updateBook(id, book, format) {
    const url = `${API}?id=${id}&format=${format}`;
    const body = buildBody(book, format);
    const ctype = format === 'xml' ? 'application/xml' : 'application/json';
    const res = await fetch(url, {
        method: 'PUT',
        headers: { 'Content-Type': ctype, Accept: ACCEPT[format] },
        body,
    });
    const text = await res.text();
    return { ok: res.ok, text, rawMethod: 'PUT ' + url };
}

async function deleteBook(id, format) {
    const url = `${API}?id=${id}&format=${format}`;
    const res = await fetch(url, { method: 'DELETE' });
    const text = res.status === 204 ? '204 No Content' : await res.text();
    return { ok: res.ok || res.status === 204, text, rawMethod: 'DELETE ' + url };
}

export const bookService = { getBooks, getBook, createBook, updateBook, deleteBook, extractErr };
