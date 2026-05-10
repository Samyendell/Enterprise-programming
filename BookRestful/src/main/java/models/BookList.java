package models;

import java.util.ArrayList;
import java.util.Collection;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/*
 * Wrapper class to facilitate XML serialisation of book collections 
 */
@XmlRootElement(name = "books")
public class BookList {

	private Collection<Book> books = new ArrayList<>();

	public BookList() {
	}

	public BookList(Collection<Book> books) {
		this.books = books;
	}

	@XmlElement(name = "book")
	public Collection<Book> getBooks() {
		return books;
	}

	public void setBooks(Collection<Book> books) {
		this.books = books;
	}
}