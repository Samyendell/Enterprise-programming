package util;

// NOTE: factory pattern - creates the right formatter based on a format string
// the servlet calls getFormatter() and gets back a BookFormatter without knowing the concrete class
// all the format selection logic is in one place here
// to support a new format: add a new case and a new formatter class
public class BookFormatterFactory {

    // NOTE: switch on the format string, defaults to JSON for anything unrecognised
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