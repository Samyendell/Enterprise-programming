package controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import db.BookDAO;
import models.Book;
import util.BookFormatter;
import util.BookFormatterFactory;
import util.JsonBookFormatter;
import util.TextBookFormatter;
import util.XmlBookFormatter;
import util.Validation;

// NOTE: single servlet handles all REST operations at /api/books
// maps HTTP methods to CRUD: GET=read, POST=create, PUT=update, DELETE=remove
// format is picked using ?format= param or Accept header, defaults to JSON
// uses singleton for the DAO and factory pattern for choosing formatters
// no format-specific logic in here - that's all in the formatter classes
@WebServlet("/api/books")
public class BookApiServlet extends HttpServlet {

    // NOTE: CORS headers let the React frontend call this API from a different
    // origin/port
    // browsers block cross-origin requests by default so these headers are needed

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        addCors(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private void addCors(HttpServletResponse resp) {
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept");
        resp.setHeader("Access-Control-Expose-Headers", "X-Total-Count, X-Total-Pages, X-Current-Page, X-Page-Size");
    }

    // ── GET ───────────────────────────────────────────────────────────────────
    // NOTE: supports ?id= for single book, ?search= for searching,
    // ?sort=title|author|date|genres&order=asc|desc for sorting,
    // ?page=1&size=5 for pagination

    private static final int DEFAULT_PAGE_SIZE = 10;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        addCors(resp);
        BookFormatter fmt = resolveFormatter(req);
        PrintWriter out = resp.getWriter();

        try {
            String idParam = req.getParameter("id");
            String searchParam = req.getParameter("search");

            if (idParam != null) {
                // Single book by id
                Book book = BookDAO.getInstance().getBookById(Integer.parseInt(idParam));
                if (book == null) {
                    sendError(resp, out, 404, "No book found with id " + idParam, fmt);
                    return;
                }
                resp.setContentType(fmt.getContentType());
                out.print(fmt.formatBook(book));

            } else {
                // NOTE: get list (all or search), then sort, then paginate
                ArrayList<Book> books;
                if (searchParam != null) {
                    String query = Validation.validateSearch(searchParam);
                    books = BookDAO.getInstance().searchBooks(query);
                } else {
                    books = BookDAO.getInstance().getAllBooks();
                }

                // NOTE: sort the results
                String sortField = req.getParameter("sort");
                String sortOrder = req.getParameter("order");
                sortBooks(books, sortField != null ? sortField : "title",
                        sortOrder != null ? sortOrder : "asc");

                // NOTE: paginate - if ?page= is provided, slice the list
                int totalBooks = books.size();
                String pageParam = req.getParameter("page");
                String sizeParam = req.getParameter("size");

                if (pageParam != null) {
                    int pageSize = parseIntOrDefault(sizeParam, DEFAULT_PAGE_SIZE);
                    int totalPages = Math.max(1, (int) Math.ceil((double) totalBooks / pageSize));
                    int page = parseIntOrDefault(pageParam, 1);
                    if (page < 1)
                        page = 1;
                    if (page > totalPages)
                        page = totalPages;

                    int from = (page - 1) * pageSize;
                    int to = Math.min(from + pageSize, totalBooks);
                    books = new ArrayList<>(books.subList(from, to));

                    // NOTE: add pagination headers so the client knows total/page info
                    resp.setHeader("X-Total-Count", String.valueOf(totalBooks));
                    resp.setHeader("X-Total-Pages", String.valueOf(totalPages));
                    resp.setHeader("X-Current-Page", String.valueOf(page));
                    resp.setHeader("X-Page-Size", String.valueOf(pageSize));
                }

                resp.setContentType(fmt.getContentType());
                out.print(fmt.formatBooks(books));
            }

        } catch (NumberFormatException e) {
            sendError(resp, out, 400, "id must be a valid integer.", fmt);
        } catch (Exception e) {
            sendError(resp, out, 500, "Server error: " + e.getMessage(), fmt);
        }
    }

    // ── POST ──────────────────────────────────────────────────────────────────

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        addCors(resp);
        BookFormatter fmt = resolveFormatter(req);
        PrintWriter out = resp.getWriter();

        try {
            Book book = parseBody(req, fmt);
            if (book == null) {
                sendError(resp, out, 400, "Request body required.", fmt);
                return;
            }

            List<String> errors = Validation.validateBook(book);
            if (!errors.isEmpty()) {
                sendError(resp, out, 422, String.join("; ", errors), fmt);
                return;
            }

            int newId = BookDAO.getInstance().insertBook(book);
            book.setId(newId);
            resp.setStatus(201); // 201 Created
            resp.setContentType(fmt.getContentType());
            out.print(fmt.formatBook(book));

        } catch (Exception e) {
            sendError(resp, out, 400, "Could not parse request: " + e.getMessage(), fmt);
        }
    }

    // ── PUT ───────────────────────────────────────────────────────────────────

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        addCors(resp);
        BookFormatter fmt = resolveFormatter(req);
        PrintWriter out = resp.getWriter();

        try {
            String idParam = req.getParameter("id");
            if (idParam == null) {
                sendError(resp, out, 400, "?id parameter required for PUT.", fmt);
                return;
            }
            int id = Integer.parseInt(idParam);
            Book existing = BookDAO.getInstance().getBookById(id);
            if (existing == null) {
                sendError(resp, out, 404, "No book found with id " + id, fmt);
                return;
            }

            Book book = parseBody(req, fmt);
            if (book == null) {
                sendError(resp, out, 400, "Request body required.", fmt);
                return;
            }
            book.setId(id); // URL id takes precedence over any id in the body

            List<String> errors = Validation.validateBook(book);
            if (!errors.isEmpty()) {
                sendError(resp, out, 422, String.join("; ", errors), fmt);
                return;
            }

            BookDAO.getInstance().updateBook(book);
            resp.setContentType(fmt.getContentType());
            out.print(fmt.formatBook(book));

        } catch (NumberFormatException e) {
            sendError(resp, out, 400, "id must be a valid integer.", fmt);
        } catch (Exception e) {
            sendError(resp, out, 400, "Could not parse request: " + e.getMessage(), fmt);
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        addCors(resp);
        BookFormatter fmt = resolveFormatter(req);
        PrintWriter out = resp.getWriter();

        try {
            String idParam = req.getParameter("id");
            if (idParam == null) {
                sendError(resp, out, 400, "?id parameter required for DELETE.", fmt);
                return;
            }
            int id = Integer.parseInt(idParam);
            Book book = BookDAO.getInstance().getBookById(id);
            if (book == null) {
                sendError(resp, out, 404, "No book found with id " + id, fmt);
                return;
            }

            BookDAO.getInstance().deleteBook(book);
            resp.setStatus(204); // 204 No Content — successful delete, no body

        } catch (NumberFormatException e) {
            sendError(resp, out, 400, "id must be a valid integer.", fmt);
        } catch (Exception e) {
            sendError(resp, out, 500, "Server error: " + e.getMessage(), fmt);
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    // NOTE: factory pattern - picks the right formatter based on what the client
    // asked for
    // checks query param first, then Accept header, falls back to JSON
    private BookFormatter resolveFormatter(HttpServletRequest req) {
        String param = req.getParameter("format");
        if (param != null)
            return BookFormatterFactory.getFormatter(param);

        String accept = req.getHeader("Accept");
        if (accept != null) {
            if (accept.contains("application/xml") || accept.contains("text/xml"))
                return BookFormatterFactory.getFormatter("xml");
            if (accept.contains("text/plain"))
                return BookFormatterFactory.getFormatter("text");
        }
        return BookFormatterFactory.getFormatter("json");
    }

    // NOTE: works out which format the body is in and parses accordingly
    // checks for XML first (starts with <), then JSON (starts with { or [),
    // otherwise treats as pipe-delimited text
    // this means clients can POST/PUT in any of the three supported formats
    private Book parseBody(HttpServletRequest req, BookFormatter fmt) throws Exception {
        String body = req.getReader().lines().collect(Collectors.joining("\n")).trim();
        if (body.isEmpty())
            return null;
        if (body.startsWith("<"))
            return XmlBookFormatter.fromXml(body);
        if (body.startsWith("{") || body.startsWith("["))
            return JsonBookFormatter.fromJson(body);
        // NOTE: falls through to text parsing - pipe-delimited format
        return TextBookFormatter.fromText(body);
    }

    // NOTE: sends error messages in the same format the client requested
    // (JSON/XML/text)
    private void sendError(HttpServletResponse resp, PrintWriter out,
            int status, String message, BookFormatter fmt) {
        resp.setStatus(status);
        String ct = fmt.getContentType();
        resp.setContentType(ct);

        if (ct.contains("xml")) {
            out.printf("<error><code>%d</code><message>%s</message></error>%n",
                    status, escXml(message));
        } else if (ct.contains("plain")) {
            out.printf("ERROR %d: %s%n", status, message);
        } else {
            out.printf("{\"error\":{\"code\":%d,\"message\":\"%s\"}}%n",
                    status, message.replace("\"", "\\\""));
        }
    }

    // NOTE: sorts the book list by field name, asc or desc
    private void sortBooks(ArrayList<Book> books, String field, String order) {
        Comparator<Book> comp;
        switch (field.toLowerCase()) {
            case "author":
                comp = Comparator.comparing(b -> b.getAuthor() != null ? b.getAuthor().toLowerCase() : "");
                break;
            case "date":
                comp = Comparator.comparing(b -> b.getDate() != null ? b.getDate() : "");
                break;
            case "genres":
                comp = Comparator.comparing(b -> b.getGenres() != null ? b.getGenres().toLowerCase() : "");
                break;
            case "id":
                comp = Comparator.comparingInt(Book::getId);
                break;
            default:
                comp = Comparator.comparing(b -> b.getTitle() != null ? b.getTitle().toLowerCase() : "");
                break;
        }
        if ("desc".equalsIgnoreCase(order))
            comp = comp.reversed();
        books.sort(comp);
    }

    // NOTE: safely parse int with fallback
    private int parseIntOrDefault(String s, int def) {
        if (s == null)
            return def;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private String escXml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}