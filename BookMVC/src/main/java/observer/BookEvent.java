package observer;

import models.Book;

// NOTE: event object for the observer pattern
// wraps the change type and the book that was affected
// passed from the DAO to all registered listeners after each write operation
public class BookEvent {

	public enum Type {
		CREATE,
		DELETE,
		UPDATE
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