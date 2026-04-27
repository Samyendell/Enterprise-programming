package controllers;

import db.BookDAO;
import models.Book;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/books/delete")
public class BookDeleteServlet extends HttpServlet {
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			int id = Integer.parseInt(req.getParameter("id"));
			Book book = BookDAO.getInstance().getBookById(id);
			if (book == null) {
				req.setAttribute("error", "Book not found.");
				req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
				return;
			}
			BookDAO.getInstance().deleteBook(book);
			resp.sendRedirect(req.getContextPath() + "/books?success=deleted");
		} catch (NumberFormatException e) {
			req.setAttribute("error", "Invalid book id.");
			req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
		} catch (Exception e) {
			req.setAttribute("error", "Could not delete book: " + e.getMessage());
			req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
		}
	}
}