package controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
import util.XmlBookFormatter;
import util.Validation;

/**
 * RESTful API endpoint for Book resources.
 *
 * URL: /api/books
 *
 * HTTP method → CRUD:
 *   GET    → getAllBooks | getBookById | searchBooks
 *   POST   → insertBook
 *   PUT    → updateBook
 *   DELETE → deleteBook
 *
 * Format selection (priority order):
 *   1. ?format=json|xml|text  query parameter
 *   2. Accept request header
 *   3. Default: JSON
 *
 * Uses the Singleton (BookDAO.getInstance()) and Factory
 * (BookFormatterFactory.getFormatter()) patterns.
 * The servlet contains no format-specific code — all of that is
 * in the formatter classes selected by the factory.
 */
@WebServlet("/api/books")
public class BookApiServlet extends HttpServlet {

    // ── CORS ─────────────────────────────────────────────────────────────────
    // Allows the JS frontend to call this API even if ports differ during dev.

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        addCors(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private void addCors(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin",  "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept");
    }

    // ── GET ───────────────────────────────────────────────────────────────────

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        addCors(resp);
        BookFormatter fmt = resolveFormatter(req);
        PrintWriter   out = resp.getWriter();

        try {
            String idParam     = req.getParameter("id");
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

            } else if (searchParam != null) {
                // Search
                String query = Validation.validateSearch(searchParam);
                ArrayList<Book> results = BookDAO.getInstance().searchBooks(query);
                resp.setContentType(fmt.getContentType());
                out.print(fmt.formatBooks(results));

            } else {
                // All books
                ArrayList<Book> books = BookDAO.getInstance().getAllBooks();
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
        PrintWriter   out = resp.getWriter();

        try {
            Book book = parseBody(req, fmt);
            if (book == null) {
                sendError(resp, out, 400, "Request body required.", fmt);
                return;
            }

            List<String> errors = ValidationUtil.validateBook(book);
            if (!errors.isEmpty()) {
                sendError(resp, out, 422, String.join("; ", errors), fmt);
                return;
            }

            int newId = BookDAO.getInstance().insertBook(book);
            book.setId(newId);
            resp.setStatus(201);   // 201 Created
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
        PrintWriter   out = resp.getWriter();

        try {
            String idParam = req.getParameter("id");
            if (idParam == null) {
                sendError(resp, out, 400, "?id parameter required for PUT.", fmt);
                return;
            }
            int  id   = Integer.parseInt(idParam);
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
            book.setId(id);   // URL id takes precedence over any id in the body

            List<String> errors = ValidationUtil.validateBook(book);
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
        PrintWriter   out = resp.getWriter();

        try {
            String idParam = req.getParameter("id");
            if (idParam == null) {
                sendError(resp, out, 400, "?id parameter required for DELETE.", fmt);
                return;
            }
            int  id   = Integer.parseInt(idParam);
            Book book = BookDAO.getInstance().getBookById(id);
            if (book == null) {
                sendError(resp, out, 404, "No book found with id " + id, fmt);
                return;
            }

            BookDAO.getInstance().deleteBook(book);
            resp.setStatus(204);   // 204 No Content — successful delete, no body

        } catch (NumberFormatException e) {
            sendError(resp, out, 400, "id must be a valid integer.", fmt);
        } catch (Exception e) {
            sendError(resp, out, 500, "Server error: " + e.getMessage(), fmt);
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Resolves the requested format via the Factory.
     * Priority: ?format= param → Accept header → default JSON.
     */
    private BookFormatter resolveFormatter(HttpServletRequest req) {
        String param = req.getParameter("format");
        if (param != null) return BookFormatterFactory.getFormatter(param);

        String accept = req.getHeader("Accept");
        if (accept != null) {
            if (accept.contains("application/xml") || accept.contains("text/xml"))
                return BookFormatterFactory.getFormatter("xml");
            if (accept.contains("text/plain"))
                return BookFormatterFactory.getFormatter("text");
        }
        return BookFormatterFactory.getFormatter("json");
    }

    /**
     * Parses a Book from the request body.
     * Detects XML by checking whether the body starts with '<'.
     * Everything else is treated as JSON.
     */
    private Book parseBody(HttpServletRequest req, BookFormatter fmt) throws Exception {
        String body = req.getReader().lines().collect(Collectors.joining("\n")).trim();
        if (body.isEmpty()) return null;
        if (body.startsWith("<")) return XmlBookFormatter.fromXml(body);
        return JsonBookFormatter.fromJson(body);
    }

    /** Writes a structured error in whatever format was requested. */
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

    private String escXml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}