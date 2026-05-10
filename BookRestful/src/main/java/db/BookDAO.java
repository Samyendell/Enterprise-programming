package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import models.Book;
import observer.AuditLogger;
import observer.BookEvent;
import observer.BookEventListener;

/*
 * This Data Access Object contains all the database operations for my MVC application.
 * This file uses the singleton design pattern to ensure only one instance of DAO can exist.
 * It also uses the observer pattern to implement a logger listening for a create, 
 * update or delete.
 */
public class BookDAO {

	/*
	 * volatile used to ensure there is only one instance when multiple users use
	 * the site
	 */
	private static volatile BookDAO instance;

	/*
	 * gets the unique shared instance of BookDAO. Synchronized used to ensure 2
	 * different users don't check if BookDAO exists at once. The second null check
	 * after synchronized is to ensure an instance wasn't created while waiting at
	 * synchronized
	 */
	public static BookDAO getInstance() {
		if (instance == null) {
			synchronized (BookDAO.class) {
				if (instance == null) {
					instance = new BookDAO();
					instance.register(new AuditLogger());
				}
			}
		}
		return instance;
	}

	private BookDAO() {
	}

	private final List<BookEventListener> listeners = new ArrayList<>();

	public void register(BookEventListener listener) {
		listeners.add(listener);
	}

	private void notifyListeners(BookEvent event) {
		for (BookEventListener listener : listeners) {
			listener.onBookEvent(event);
		}
	}

	private final String user = "yendells";
	private final String password = "CemVegDov1";
	private final String url = "jdbc:mysql://mudfoot.doc.stu.mmu.ac.uk:6306/" + user;

	private Connection openConnection() throws SQLException {
		try {
			Class.forName("com.mysql.jdbc.Driver").getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			System.out.println("Driver load error: " + e);
		}
		return DriverManager.getConnection(url, user, password);
	}

	private Book getNextBook(ResultSet rs) throws SQLException {
		return new Book(rs.getInt("id"), rs.getString("title"), rs.getString("author"), rs.getString("date"),
				rs.getString("genres"), rs.getString("characters"), rs.getString("synopsis"));
	}

	public ArrayList<Book> getAllBooks() {
		ArrayList<Book> books = new ArrayList<>();
		try (Connection conn = openConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM books")) {
			while (rs.next())
				books.add(getNextBook(rs));
		} catch (SQLException se) {
			System.out.println("getAllBooks error: " + se);
		}
		return books;
	}

	public Book getBookById(int id) {
		try (Connection conn = openConnection();
				PreparedStatement ps = conn.prepareStatement("SELECT * FROM books WHERE id = ?")) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return getNextBook(rs);
			}
		} catch (SQLException se) {
			System.out.println("getBookById error: " + se);
		}
		return null;
	}

	public ArrayList<Book> searchBooks(String search) {
		ArrayList<Book> results = new ArrayList<>();
		String pattern = "%" + search + "%";
		try (Connection conn = openConnection();
				PreparedStatement ps = conn
						.prepareStatement("SELECT * FROM books WHERE title LIKE ? OR author LIKE ? OR genres LIKE ?")) {
			ps.setString(1, pattern);
			ps.setString(2, pattern);
			ps.setString(3, pattern);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.add(getNextBook(rs));
			}
		} catch (SQLException se) {
			System.out.println("searchBooks error: " + se);
		}
		return results;
	}

	public int insertBook(Book b) throws SQLException {
		try (Connection conn = openConnection();
				PreparedStatement ps = conn
						.prepareStatement("INSERT INTO books (title, author, date, genres, characters, synopsis) "
								+ "VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, b.getTitle());
			ps.setString(2, b.getAuthor());
			ps.setString(3, b.getDate());
			ps.setString(4, b.getGenres());
			ps.setString(5, b.getCharacters());
			ps.setString(6, b.getSynopsis());
			ps.executeUpdate();
			try (ResultSet keys = ps.getGeneratedKeys()) {
				if (keys.next()) {
					int generatedId = keys.getInt(1);
					b.setId(generatedId);
					notifyListeners(new BookEvent(BookEvent.Type.CREATE, b));
					return generatedId;
				}
			}
		}
		return -1;
	}

	public int updateBook(Book b) throws SQLException {
		try (Connection conn = openConnection();
				PreparedStatement ps = conn.prepareStatement("UPDATE books SET title=?, author=?, date=?, genres=?, "
						+ "characters=?, synopsis=? WHERE id=?")) {
			ps.setString(1, b.getTitle());
			ps.setString(2, b.getAuthor());
			ps.setString(3, b.getDate());
			ps.setString(4, b.getGenres());
			ps.setString(5, b.getCharacters());
			ps.setString(6, b.getSynopsis());
			ps.setInt(7, b.getId());
			int rows = ps.executeUpdate();
			notifyListeners(new BookEvent(BookEvent.Type.UPDATE, b));
			return rows;
		}
	}

	public int deleteBook(Book b) throws SQLException {
		try (Connection conn = openConnection();
				PreparedStatement ps = conn.prepareStatement("DELETE FROM books WHERE id = ?")) {
			ps.setInt(1, b.getId());
			int rows = ps.executeUpdate();
			notifyListeners(new BookEvent(BookEvent.Type.DELETE, b));
			return rows;
		}
	}
}
