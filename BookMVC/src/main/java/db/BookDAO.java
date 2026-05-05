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



/**
 * Data Access Object for the books table.
 *
 * ════════════════════════════════════════════════════════
 *  SWITCHING TO LIVE DATABASE — checklist:
 *
 *  1. Update the three constants below (user, password, url)
 *  2. In getAllBooks()  — swap the test block for the LIVE DB block
 *  3. In getBookById() — swap the test block for the LIVE DB block
 *  4. In searchBooks() — swap the test block for the LIVE DB block
 *  5. In insertBook()  — swap the test block for the LIVE DB block
 *  6. In updateBook()  — swap the test block for the LIVE DB block
 *  7. In deleteBook()  — swap the test block for the LIVE DB block
 *
 *  Each method has clearly marked // ── TEST DATA ── and
 *  // ── LIVE DB ── sections showing exactly what to swap.
 * ════════════════════════════════════════════════════════
 *
 * === Singleton pattern ===
 * One shared instance is created on first call to getInstance().
 * All servlets share the same DAO — no new BookDAO() anywhere.
 *
 * === Observer pattern ===
 * After every write operation the DAO notifies all registered
 * BookEventListener instances. AuditLogger is registered at startup.
 *
 * === SQL injection prevention ===
 * All queries that include user data use PreparedStatements.
 * The JDBC driver parameterises values before they reach MySQL.
 */
public class BookDAO {

    // ── Singleton ─────────────────────────────────────────────────────────────

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

    private BookDAO() {}

    // ── Observer ──────────────────────────────────────────────────────────────

    private final List<BookEventListener> listeners = new ArrayList<>();

    public void register(BookEventListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners(BookEvent event) {
        for (BookEventListener l : listeners) l.onBookEvent(event);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  DATABASE CONNECTION — update these three values for live DB
    // ═════════════════════════════════════════════════════════════════════════

    // ── STEP 1 WHEN GOING LIVE: replace these three values ───────────────────
    String user     = "YOUR_MUDFOOT_USERNAME";
    String password = "YOUR_MUDFOOT_PASSWORD";
    String url      = "jdbc:mysql://mudfoot.doc.stu.mmu.ac.uk:6306/" + user;
    // ─────────────────────────────────────────────────────────────────────────

    Connection conn = null;
    Statement  stmt = null;

    private void openConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver")
                 .getDeclaredConstructor()
                 .newInstance();
        } catch (Exception e) {
            System.out.println("Driver error: " + e);
        }
        try {
            conn = DriverManager.getConnection(url, user, password);
            stmt = conn.createStatement();
        } catch (SQLException se) {
            System.out.println("Connection error: " + se);
        }
    }

    private void closeConnection() {
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Book getNextBook(ResultSet rs) {
        try {
            return new Book(
                rs.getInt   ("id"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getString("date"),
                rs.getString("genres"),
                rs.getString("characters"),
                rs.getString("synopsis")
            );
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  IN-MEMORY TEST STORE
    //  Remove this entire block when switching to live DB
    // ═════════════════════════════════════════════════════════════════════════

    // ── TEST DATA: remove this block when going live ──────────────────────────
    private final ArrayList<Book> store = new ArrayList<Book>() {{
        add(new Book(1, "The Kite Runner",
                     "Khaled Hosseini", "2003",
                     "Fiction, Drama",
                     "Amir, Hassan, Baba",
                     "A story of friendship and redemption set in Afghanistan."));
        add(new Book(2, "1984",
                     "George Orwell", "1949",
                     "Dystopian, Political Fiction",
                     "Winston Smith, Julia, O'Brien",
                     "A totalitarian future society under constant surveillance."));
        add(new Book(3, "To Kill a Mockingbird",
                     "Harper Lee", "1960",
                     "Fiction, Legal Drama",
                     "Scout, Atticus, Boo Radley",
                     "Racial injustice in the American South."));
        add(new Book(4, "Clean Code",
                     "Robert C. Martin", "2008",
                     "Programming",
                     "",
                     "Principles and practices for writing readable code."));
        add(new Book(5, "Dune",
                     "Frank Herbert", "1965",
                     "Science Fiction",
                     "Paul Atreides, Lady Jessica, Baron Harkonnen",
                     "A desert planet holds the universe's most valuable resource."));
    }};

    private int nextId = 6;
    // ── END TEST DATA block ───────────────────────────────────────────────────

    // ═════════════════════════════════════════════════════════════════════════
    //  CRUD METHODS
    // ═════════════════════════════════════════════════════════════════════════

    // ── GET ALL ───────────────────────────────────────────────────────────────

    public ArrayList<Book> getAllBooks() {

        // ── TEST DATA: returns the in-memory list ─────────────────────────────
        return new ArrayList<>(store);
        // ── END TEST DATA ─────────────────────────────────────────────────────

        /*
         * ── LIVE DB: uncomment this block and remove the TEST DATA line above ──
         *
         * ArrayList<Book> allBooks = new ArrayList<>();
         * openConnection();
         * try {
         *     String sql = "SELECT * FROM books ORDER BY title";
         *     ResultSet rs = stmt.executeQuery(sql);
         *     while (rs.next()) allBooks.add(getNextBook(rs));
         *     stmt.close();
         *     closeConnection();
         * } catch (SQLException se) {
         *     System.out.println(se);
         * }
         * return allBooks;
         *
         * ── END LIVE DB ───────────────────────────────────────────────────────
         */
    }

    // ── GET BY ID ─────────────────────────────────────────────────────────────

    public Book getBookById(int id) {

        // ── TEST DATA: searches the in-memory list ────────────────────────────
        for (Book b : store) {
            if (b.getId() == id) return b;
        }
        return null;
        // ── END TEST DATA ─────────────────────────────────────────────────────

        /*
         * ── LIVE DB: uncomment this block and remove the TEST DATA block above ─
         *
         * openConnection();
         * Book book = null;
         * try {
         *     PreparedStatement ps = conn.prepareStatement(
         *         "SELECT * FROM books WHERE id = ?");
         *     ps.setInt(1, id);
         *     ResultSet rs = ps.executeQuery();
         *     if (rs.next()) book = getNextBook(rs);
         *     ps.close();
         *     closeConnection();
         * } catch (SQLException se) {
         *     System.out.println(se);
         * }
         * return book;
         *
         * ── END LIVE DB ───────────────────────────────────────────────────────
         */
    }

    // ── SEARCH ────────────────────────────────────────────────────────────────

    public ArrayList<Book> searchBooks(String searchStr) {

        // ── TEST DATA: searches the in-memory list ────────────────────────────
        ArrayList<Book> results = new ArrayList<>();
        String lower = searchStr.toLowerCase();
        for (Book b : store) {
            if (contains(b.getTitle(),      lower) ||
                contains(b.getAuthor(),     lower) ||
                contains(b.getGenres(),     lower)) {
                results.add(b);
            }
        }
        return results;
        // ── END TEST DATA ─────────────────────────────────────────────────────

        /*
         * ── LIVE DB: uncomment this block and remove the TEST DATA block above ─
         *
         * ArrayList<Book> results = new ArrayList<>();
         * openConnection();
         * try {
         *     PreparedStatement ps = conn.prepareStatement(
         *         "SELECT * FROM books " +
         *         "WHERE title LIKE ? OR author LIKE ? OR genres LIKE ? " +
         *         "ORDER BY title");
         *     String pattern = "%" + searchStr + "%";
         *     ps.setString(1, pattern);
         *     ps.setString(2, pattern);
         *     ps.setString(3, pattern);
         *     ResultSet rs = ps.executeQuery();
         *     while (rs.next()) results.add(getNextBook(rs));
         *     ps.close();
         *     closeConnection();
         * } catch (SQLException se) {
         *     System.out.println(se);
         * }
         * return results;
         *
         * ── END LIVE DB ───────────────────────────────────────────────────────
         */
    }

    // ── INSERT ────────────────────────────────────────────────────────────────

    public int insertBook(Book b) throws SQLException {

        // ── TEST DATA: adds to the in-memory list ─────────────────────────────
        b.setId(nextId++);
        store.add(b);
        notifyListeners(new BookEvent(BookEvent.Type.CREATE, b));
        return b.getId();
        // ── END TEST DATA ─────────────────────────────────────────────────────

        /*
         * ── LIVE DB: uncomment this block and remove the TEST DATA block above ─
         *
         * openConnection();
         * int generatedId = -1;
         * PreparedStatement ps = conn.prepareStatement(
         *     "INSERT INTO books " +
         *     "(title, author, date, genres, characters, synopsis, cover) " +
         *     "VALUES (?, ?, ?, ?, ?, ?, ?)",
         *     Statement.RETURN_GENERATED_KEYS);
         * ps.setString(1, b.getTitle());
         * ps.setString(2, b.getAuthor());
         * ps.setString(3, b.getDate());
         * ps.setString(4, b.getGenres());
         * ps.setString(5, b.getCharacters());
         * ps.setString(6, b.getSynopsis());
         * ps.setString(7, b.getCover());
         * ps.executeUpdate();
         * ResultSet keys = ps.getGeneratedKeys();
         * if (keys.next()) generatedId = keys.getInt(1);
         * ps.close();
         * closeConnection();
         * b.setId(generatedId);
         * notifyListeners(new BookEvent(BookEvent.Type.CREATED, b));
         * return generatedId;
         *
         * ── END LIVE DB ───────────────────────────────────────────────────────
         */
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    public int updateBook(Book b) throws SQLException {

        // ── TEST DATA: updates the in-memory list ─────────────────────────────
        for (int i = 0; i < store.size(); i++) {
            if (store.get(i).getId() == b.getId()) {
                store.set(i, b);
                notifyListeners(new BookEvent(BookEvent.Type.UPDATE, b));
                return 1;
            }
        }
        return 0;
        // ── END TEST DATA ─────────────────────────────────────────────────────

        /*
         * ── LIVE DB: uncomment this block and remove the TEST DATA block above ─
         *
         * openConnection();
         * PreparedStatement ps = conn.prepareStatement(
         *     "UPDATE books SET title=?, author=?, date=?, genres=?, " +
         *     "characters=?, synopsis=?, cover=? WHERE id=?");
         * ps.setString(1, b.getTitle());
         * ps.setString(2, b.getAuthor());
         * ps.setString(3, b.getDate());
         * ps.setString(4, b.getGenres());
         * ps.setString(5, b.getCharacters());
         * ps.setString(6, b.getSynopsis());
         * ps.setString(7, b.getCover());
         * ps.setInt   (8, b.getId());
         * int rows = ps.executeUpdate();
         * ps.close();
         * closeConnection();
         * notifyListeners(new BookEvent(BookEvent.Type.UPDATED, b));
         * return rows;
         *
         * ── END LIVE DB ───────────────────────────────────────────────────────
         */
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    public int deleteBook(Book b) throws SQLException {

        // ── TEST DATA: removes from the in-memory list ────────────────────────
        boolean removed = store.removeIf(book -> book.getId() == b.getId());
        if (removed) {
            notifyListeners(new BookEvent(BookEvent.Type.DELETE, b));
            return 1;
        }
        return 0;
        // ── END TEST DATA ─────────────────────────────────────────────────────

        /*
         * ── LIVE DB: uncomment this block and remove the TEST DATA block above ─
         *
         * openConnection();
         * PreparedStatement ps = conn.prepareStatement(
         *     "DELETE FROM books WHERE id = ?");
         * ps.setInt(1, b.getId());
         * int rows = ps.executeUpdate();
         * ps.close();
         * closeConnection();
         * notifyListeners(new BookEvent(BookEvent.Type.DELETED, b));
         * return rows;
         *
         * ── END LIVE DB ───────────────────────────────────────────────────────
         */
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    // ── TEST DATA only — remove this method when going live ───────────────────
    private boolean contains(String field, String term) {
        return field != null && field.toLowerCase().contains(term);
    }
    // ── END TEST DATA ─────────────────────────────────────────────────────────
}