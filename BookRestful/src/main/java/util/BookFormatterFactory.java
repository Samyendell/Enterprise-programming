package util;

/**
 * Factory for BookFormatter instances.
 *
 * === Factory pattern ===
 * The servlet calls BookFormatterFactory.getFormatter(format) and receives
 * a BookFormatter without knowing which concrete class it is.
 * The servlet no longer contains switch/if-else blocks for format handling —
 * that decision is entirely encapsulated here.
 *
 * To add a CSV format: create CsvBookFormatter implements BookFormatter,
 * then add case "csv" below. Nothing else changes.
 */
public class BookFormatterFactory {

    /**
     * Returns the appropriate BookFormatter for the requested format.
     * Defaults to JSON if the format string is null or unrecognised.
     *
     * @param format one of "json", "xml", "text" (case-insensitive)
     */
    public static BookFormatter getFormatter(String format) {
        if (format == null) return new JsonBookFormatter();

        switch (format.toLowerCase()) {
            case "xml":  return new XmlBookFormatter();
            case "text": return new TextBookFormatter();
            default:     return new JsonBookFormatter();
        }
    }
}