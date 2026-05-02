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


public class BookDAO {
	
	private static volatile BookDAO instance;
	
	public static BookDAO getInstance() {
		if(instance == null) {
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
	
	private final List<BookEventListener> listeners = new ArrayList<>();
	
	public void register(BookEventListener listener) {
		listeners.add(listener);
	}
	
	private void notifyListeners(BookEvent event) {
		for (BookEventListener listener : listeners) {
			listener.onBookEvent(event);
		}
	}
	
	Book oneBook = null;
	Connection conn = null;
    Statement stmt = null;
	String user = "YOUR_MUDFOOT_USERNAME";
    String password = "YOUR_MUDFOOT_PASSWORD";
    // Note none default port used, 6306 not 3306
    String url = "jdbc:mysql://mudfoot.doc.stu.mmu.ac.uk:6306/" + user;

//	public BookDAO() {}

	private void openConnection(){
		// loading jdbc driver for mysql
		try{
		    Class.forName("com.mysql.jdbc.Driver").getDeclaredConstructor().newInstance();
		} catch(Exception e) { System.out.println(e); }

		// connecting to database
		try{
			// connection string for demos database, username demos, password demos
 			conn = DriverManager.getConnection(url, user, password);
		    stmt = conn.createStatement();
		} catch(SQLException se) { System.out.println(se); }	   
    }
	
	private void closeConnection(){
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Book getNextBook(ResultSet rs){
    	Book thisBook = null;
		try {
			
			thisBook = new Book(
					rs.getInt("id"),
					rs.getString("title"),
					rs.getString("author"),
					rs.getString("date"),
					rs.getString("genres"),
					rs.getString("characters"),
					rs.getString("synopsis"));
//					rs.getString("cover"));
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	return thisBook;		
	}
//	here !!!!!!!!!!!!!!
	
   public ArrayList<Book> getAllBooks(){
	   
		ArrayList<Book> allBooks = new ArrayList<Book>();
		openConnection();
		
	    // Create select statement and execute it
		try{
		    String selectSQL = "select * from books";
		    ResultSet rs1 = stmt.executeQuery(selectSQL);
	    // Retrieve the results
		    while(rs1.next()){
		    	oneBook = getNextBook(rs1);
		    	allBooks.add(oneBook);
		   }

		    stmt.close();
		    closeConnection();
		} catch(SQLException se) { System.out.println(se); }

	   return allBooks;
   }
   
   public Book getBookById(int id) {
	   openConnection();
	   Book book = null;
	   
	   try {
		   PreparedStatement ps = conn.prepareStatement(
				   "select * from books where id=?");
		   ps.setInt(1, id);
		   ResultSet rs = ps.executeQuery();
		   if (rs.next()) book = getNextBook(rs);
		   ps.close();
		   closeConnection();
	   } catch (SQLException se) { 
		   System.out.println(se); 
	   }
	   return book;
   }
   
   public ArrayList<Book> searchBooks(String search) {
	   ArrayList<Book> results = new ArrayList<>();
	   openConnection();
	   
	   try {
		   PreparedStatement ps = conn.prepareStatement(
				   "select * from books " +
				   "where title like ? or author like ? or genres like ? " +
				   "order by title");
		   String pattern = "%" + search + "%";
		   ps.setString(1, pattern);
		   ps.setString(2, pattern);
		   ps.setString(3, pattern);
		   ResultSet rs = ps.executeQuery();
		   while (rs.next()) results.add(getNextBook(rs));
		   ps.close();
		   closeConnection();		   
	   } catch (SQLException se) { 
		   System.out.println(se); 
	   }
	   return results;
   }
   
   public int insertBook(Book b) throws SQLException {
	   openConnection();
	   int generatedId = -1;
	   
	   PreparedStatement ps = conn.prepareStatement(
			   "insert into books (title, author, date, genres, characters,"
			   + " synopsis) values (?, ?, ?, ?, ?, ?)",
			   Statement.RETURN_GENERATED_KEYS);
	   
	   ps.setString(1, b.getTitle());
	   ps.setString(2, b.getAuthor());
	   ps.setString(3, b.getDate());
	   ps.setString(4, b.getGenres());
	   ps.setString(5, b.getCharacters());
	   ps.setString(6, b.getSynopsis());
	   ps.executeUpdate();
	   
	   ResultSet keys = ps.getGeneratedKeys();
	   if (keys.next()) generatedId = keys.getInt(1);
	   
	   ps.close();
	   closeConnection();
	   
	   b.setId(generatedId);
	   notifyListeners(new BookEvent(BookEvent.Type.CREATE, b));
	   
	   return generatedId;   
   }
   
   public int updateBook(Book b) throws SQLException {
	   openConnection();
	   
	   PreparedStatement ps = conn.prepareStatement(
			   "update books set title=?, author=?, date=?, "
			   + "genres=?, characters=?, synopsis=? where id=?");
	   
	   ps.setString(1, b.getTitle());
	   ps.setString(2, b.getAuthor());
	   ps.setString(3, b.getDate());
	   ps.setString(4, b.getGenres());
	   ps.setString(5, b.getCharacters());
	   ps.setString(6, b.getSynopsis());
	   ps.setInt(8, b.getId());
	   
	   int rows = ps.executeUpdate();
	   ps.close();
	   closeConnection();
	   
	   notifyListeners(new BookEvent(BookEvent.Type.UPDATE, b));
	   
	   return rows;
   }

   public int deleteBook(Book b) throws SQLException {
	   openConnection();
	   
	   PreparedStatement ps = conn.prepareStatement(
			   "delete from books where id=?");
	   ps.setInt(1, b.getId());
	   
	   int rows = ps.executeUpdate();
	   ps.close();
	   closeConnection();
	   
	   notifyListeners(new BookEvent(BookEvent.Type.DELETE, b));
	   
	   return rows;
   }
}
