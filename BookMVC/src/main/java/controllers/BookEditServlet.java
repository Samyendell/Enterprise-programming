package controllers;

import db.BookDAO;
import models.Book;
import util.Validation;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/books/edit")
public class BookEditServlet extends HttpServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			int id = Integer.parseInt(req.getParameter("id"));
			Book book = BookDAO.getInstance().getBookById(id);
			if (book == null) {
				req.setAttribute("error", "Book not found.");
				req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
				return;
			}
			req.setAttribute("book", book);
			req.setAttribute("action", "Edit");
			req.getRequestDispatcher("/WEB-INF/views/book-form.jsp").forward(req, resp);
		} catch (NumberFormatException e) {
			req.setAttribute("error", "Invalid book id.");
			req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
		}
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			int id = Integer.parseInt(req.getParameter("id"));
			Book book = BookAddServlet.buildBookFromRequest(req);
			book.setId(id);
			
			List<String> errors = Validation.validateBook(book);
			if (!errors.isEmpty()) {
				req.setAttribute("book", book);
				req.setAttribute("action", "Edit");
				req.setAttribute("errors", errors);
				req.getRequestDispatcher("/WEB-INF/views/book-form.jsp").forward(req, resp);
				return;
			}
			
			BookDAO.getInstance().updateBook(book);
			resp.sendRedirect(req.getContextPath() + "/books?success-update");
		} catch (NumberFormatException e) {
			req.setAttribute("error", "Invalid book id.");
			req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
		} catch (Exception e) {
			req.setAttribute("error", "Could not update book: " + e.getMessage());
			req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
		}
	}
}