package controllers;

import db.BookDAO;
import models.Book;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

@WebServlet("/books")
public class BookListServlet extends HttpServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			ArrayList<Book> books = BookDAO.getInstance().getAllBooks();
			req.setAttribute("books", books);
			req.getRequestDispatcher("/WEBS-INF/views/books-list.jsp").forward(req, resp);
		} catch (Exception e) {
			req.setAttribute("error", "Could not load books: " + e.getMessage());
			req.getRequestDispatcher("/WEBS-INF/views/error.jsp").forward(req, resp);
		}
	}
}