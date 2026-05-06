package observer;

// NOTE: observer pattern - interface that any listener must implement
// the DAO calls onBookEvent on all registered listeners after a write
// new listeners just implement this and register with the DAO
public interface BookEventListener {

	void onBookEvent(BookEvent event);
}