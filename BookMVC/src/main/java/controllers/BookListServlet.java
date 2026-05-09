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
import java.util.Comparator;

/*
 * This controller is responsible for displaying the books from the database. 
 * It just has a GET that retrieves all the data from the database and then 
 * sends the data onwards in the format requested by the url parameters 
 * (sorting, pages)
*/
@WebServlet("/books")
public class BookListServlet extends HttpServlet {

	private static final int DEFAULT_PAGE_SIZE = 5;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			ArrayList<Book> books = BookDAO.getInstance().getAllBooks();

			String sortField = req.getParameter("sort");
			String sortOrder = req.getParameter("order");
			if (sortField == null)
				sortField = "title";
			if (sortOrder == null)
				sortOrder = "asc";
			books = sortBooks(books, sortField, sortOrder);

			int totalBooks = books.size();
			int pageSize = parseIntOrDefault(req.getParameter("size"), DEFAULT_PAGE_SIZE);
			int totalPages = Math.max(1, (int) Math.ceil((double) totalBooks / pageSize));
			int page = parseIntOrDefault(req.getParameter("page"), 1);
			if (page < 1)
				page = 1;
			if (page > totalPages)
				page = totalPages;

			int fromIndex = (page - 1) * pageSize;
			int toIndex = Math.min(fromIndex + pageSize, totalBooks);
			ArrayList<Book> pageBooks = new ArrayList<>(books.subList(fromIndex, toIndex));

			req.setAttribute("books", pageBooks);
			req.setAttribute("totalBooks", totalBooks);
			req.setAttribute("currentPage", page);
			req.setAttribute("totalPages", totalPages);
			req.setAttribute("pageSize", pageSize);
			req.setAttribute("sortField", sortField);
			req.setAttribute("sortOrder", sortOrder);

			req.getRequestDispatcher("/WEB-INF/views/books-list.jsp").forward(req, resp);
		} catch (Exception e) {
			req.setAttribute("error", "Could not load books: " + e.getMessage());
			req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
		}
	}

	private ArrayList<Book> sortBooks(ArrayList<Book> books, String field, String order) {
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
		if ("desc".equalsIgnoreCase(order)) {
			comp = comp.reversed();
		}
		books.sort(comp);
		return books;
	}

	private int parseIntOrDefault(String s, int defaultVal) {
		if (s == null)
			return defaultVal;
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return defaultVal;
		}
	}
}