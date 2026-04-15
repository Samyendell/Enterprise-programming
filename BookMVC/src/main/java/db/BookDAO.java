package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import models.Book;


public class BookDAO {
	
	Book oneBook = null;
	Connection conn = null;
    Statement stmt = null;
	String user = "YOUR_MUDFOOT_USERNAME";
    String password = "YOUR_MUDFOOT_PASSWORD";
    // Note none default port used, 6306 not 3306
    String url = "jdbc:mysql://mudfoot.doc.stu.mmu.ac.uk:6306/" + user;

	public BookDAO() {}

	
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
    	Book thisBook=null;
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
   
//   public int insertBook(Book b) throws SQLException {
//
//	   // DO SOMETHING FANTASTIC HERE
//   }
//   
//   public int updateBook(Book b) throws SQLException {
//	   
//	   // DO SOMETHING FANTASTIC HERE
//   }
//
//   public int deleteBook(Book b) throws SQLException {
//	   // DO SOMETHING FANTASTIC HERE
//	   
//   }
   
}
