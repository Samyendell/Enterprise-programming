package observer;

import models.Book;

/*
 * This class is the event object of the observer pattern in the project.
 * It encapsulates the action and data on a database update.  
 */
public class BookEvent {

	public enum Type {
		CREATE, DELETE, UPDATE
	}

	private final Type type;
	private final Book book;

	public BookEvent(Type type, Book book) {
		this.type = type;
		this.book = book;
	}

	public Type getType() {
		return type;
	}

	public Book getBook() {
		return book;
	}
}