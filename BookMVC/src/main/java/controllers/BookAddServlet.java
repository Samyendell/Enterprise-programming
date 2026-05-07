package controllers;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import db.BookDAO;
import models.Book;
import util.Validation;

// NOTE: handles adding new books
// GET shows the empty form, POST processes the submission
// validates input server-side, if errors exist re-shows the form with messages
// on success redirects to the book list (PRG pattern to avoid duplicate submissions)
@WebServlet("/books/add")
public class BookAddServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setAttribute("book", new Book());
		req.setAttribute("action", "Add");
		req.getRequestDispatcher("/WEB-INF/views/book-form.jsp").forward(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Book book = buildBookFromRequest(req);

		List<String> errors = Validation.validateBook(book);

		if (!errors.isEmpty()) {
			req.setAttribute("book", book);
			req.setAttribute("action", "Add");
			req.setAttribute("errors", errors);
			req.getRequestDispatcher("/WEB-INF/views/book-form.jsp").forward(req, resp);

			return;
		}

		try {
			BookDAO.getInstance().insertBook(book);
			resp.sendRedirect(req.getContextPath() + "/books?success=added");
		} catch (Exception e) {
			req.setAttribute("error", "Could not add book: " + e.getMessage());
			req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);

		}
	}

	static Book buildBookFromRequest(HttpServletRequest req) {
		Book b = new Book();
		b.setTitle(param(req, "title"));
		b.setAuthor(param(req, "author"));
		b.setDate(param(req, "date"));
		b.setGenres(param(req, "genres"));
		b.setCharacters(param(req, "characters"));
		b.setSynopsis(param(req, "synopsis"));
		return b;

	}

	private static String param(HttpServletRequest req, String string) {
		String s = req.getParameter(string);
		return s != null ? s.trim() : "";
	}
}