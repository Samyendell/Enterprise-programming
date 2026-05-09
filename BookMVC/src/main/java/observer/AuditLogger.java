package observer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*
 * This class is the subscriber of the observer pattern in the project.
 * consumes book event to provide data on the database action taking place
 */
public class AuditLogger implements BookEventListener {
	private static final DateTimeFormatter formattedDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@Override
	public void onBookEvent(BookEvent event) {
		String timestamp = LocalDateTime.now().format(formattedDateTime);
		models.Book b = event.getBook();

		System.out.printf("[AUDIT %s] %s — id=%d title='%s'%n", timestamp, event.getType().name(), b.getId(),
				b.getTitle());
	}
}