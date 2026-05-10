package observer;

/*
 * This interface defines the contract for any class that wants to listen
 * for database changes.
 */
public interface BookEventListener {

	void onBookEvent(BookEvent event);
}