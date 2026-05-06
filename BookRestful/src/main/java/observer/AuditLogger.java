package observer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// NOTE: observer pattern - this is a concrete listener
// gets notified whenever a book is created, updated, or deleted
// logs the event type, timestamp, and book details to the console
// could be swapped for a file logger or database logger without changing the DAO
public class AuditLogger implements BookEventListener {
	private static final DateTimeFormatter formattedDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@Override
	public void onBookEvent(BookEvent event) {
		String timestamp = LocalDateTime.now().format(formattedDateTime);
		models.Book b = event.getBook();

		System.out.printf("[AUDIT %s] %s — id=%d title='%s'%n",
				timestamp,
				event.getType().name(),
				b.getId(),
				b.getTitle());
	}
}