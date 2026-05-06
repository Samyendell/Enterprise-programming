package observer;

import models.Book;

// NOTE: observer pattern - this is the event object passed to listeners
// holds the type of change (CREATE/UPDATE/DELETE) and the affected book
// immutable once created - fields are final
public class BookEvent {

	// NOTE: enum restricts event types to only valid operations
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