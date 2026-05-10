package util;

/*
 * This file implements the factory pattern to creating instances of the
 * different book formatter strategies. This help to centralise the logic
 * for creating a formatter and shields the rest of the project from the
 * implementation details on which data format is used. defaults to JSON.
 */
public class BookFormatterFactory {

	public static BookFormatter getFormatter(String format) {
		if (format == null)
			return new JsonBookFormatter();

		switch (format.toLowerCase()) {
		case "xml":
			return new XmlBookFormatter();
		case "text":
			return new TextBookFormatter();
		default:
			return new JsonBookFormatter();
		}
	}
}