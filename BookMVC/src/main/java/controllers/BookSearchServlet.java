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
import java.util.ArrayList;

@WebServlet("/books/search")
public class BookSearchServlet extends HttpServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String raw = req.getParameter("q");
		
		if (raw == null) {
			req.getRequestDispatcher("/WEB-INF/views/search.jsp").forward(req, resp);
			return;
		}
		
		String query = Validation.validateSearch(raw);
		if (query.isEmpty()) {
			req.setAttribute("error", "Please enter a search term");
			req.getRequestDispatcher("/WEB-INF/views/search.jsp").forward(req, resp);
			return;
		}
		
		try {
			ArrayList<Book> results = BookDAO.getInstance().searchBooks(query);
			req.setAttribute("results", results);
			req.setAttribute("query", raw);
			req.getRequestDispatcher("/WEB-INF/views/search.jsp").forward(req, resp);
		} catch (Exception e) {
			req.setAttribute("error", "Search failed: " + e.getMessage());
			req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
			
		}
	}
	
	
}