package observer;

// NOTE: observer interface - any class wanting to listen for book changes implements this
// decouples the DAO from the logging/notification logic
public interface BookEventListener {

	void onBookEvent(BookEvent event);
}