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

// NOTE: data access object - all database/storage operations go through here
// singleton pattern - only one instance exists, shared across all servlets
// observer pattern - notifies registered listeners (like AuditLogger) after writes
// all queries use PreparedStatement to prevent SQL injection
public class BookDAO {

	// NOTE: volatile + double-checked locking for thread-safe singleton
	private static volatile BookDAO instance;

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

	// NOTE: observer pattern - list of listeners that get notified on changes
	private final List<BookEventListener> listeners = new ArrayList<>();

	// NOTE: servlets or startup code can register new listeners here
	public void register(BookEventListener listener) {
		listeners.add(listener);
	}

	// NOTE: called after every insert/update/delete to notify all observers
	private void notifyListeners(BookEvent event) {
		for (BookEventListener listener : listeners) {
			listener.onBookEvent(event);
		}
	}

	// NOTE: update these three values to match your Mudfoot credentials
	private final String user = "yendells";
	private final String password = "CemVegDov1";
	private final String url = "jdbc:mysql://mudfoot.doc.stu.mmu.ac.uk:6306/" + user;

	// NOTE: opens a fresh connection - called at the start of each method
	private Connection openConnection() throws SQLException {
		try {
			Class.forName("com.mysql.jdbc.Driver").getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			System.out.println("Driver load error: " + e);
		}
		return DriverManager.getConnection(url, user, password);
	}

	// NOTE: maps a ResultSet row to a Book object
	private Book getNextBook(ResultSet rs) throws SQLException {
		return new Book(
				rs.getInt("id"),
				rs.getString("title"),
				rs.getString("author"),
				rs.getString("date"),
				rs.getString("genres"),
				rs.getString("characters"),
				rs.getString("synopsis"));
	}

	// NOTE: returns every row in the books table
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

	// NOTE: finds a single book by its primary key
	public Book getBookById(int id) {
		try (Connection conn = openConnection();
				PreparedStatement ps = conn.prepareStatement(
						"SELECT * FROM books WHERE id = ?")) {
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

	// NOTE: searches title, author, and genres using LIKE
	// the search string is already sanitised by Validation.validateSearch before
	// reaching here
	public ArrayList<Book> searchBooks(String search) {
		ArrayList<Book> results = new ArrayList<>();
		String pattern = "%" + search + "%";
		try (Connection conn = openConnection();
				PreparedStatement ps = conn.prepareStatement(
						"SELECT * FROM books WHERE title LIKE ? OR author LIKE ? OR genres LIKE ?")) {
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

	// NOTE: inserts a new book row and returns the auto-generated id
	public int insertBook(Book b) throws SQLException {
		try (Connection conn = openConnection();
				PreparedStatement ps = conn.prepareStatement(
						"INSERT INTO books (title, author, date, genres, characters, synopsis) " +
								"VALUES (?, ?, ?, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS)) {
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

	// NOTE: updates all editable columns for the book matching the id
	public int updateBook(Book b) throws SQLException {
		try (Connection conn = openConnection();
				PreparedStatement ps = conn.prepareStatement(
						"UPDATE books SET title=?, author=?, date=?, genres=?, " +
								"characters=?, synopsis=? WHERE id=?")) {
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

	// NOTE: deletes the row with the matching id
	public int deleteBook(Book b) throws SQLException {
		try (Connection conn = openConnection();
				PreparedStatement ps = conn.prepareStatement(
						"DELETE FROM books WHERE id = ?")) {
			ps.setInt(1, b.getId());
			int rows = ps.executeUpdate();
			notifyListeners(new BookEvent(BookEvent.Type.DELETE, b));
			return rows;
		}
	}
}
