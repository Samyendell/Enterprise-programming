package util;

import models.Book;
import java.util.ArrayList;

/**
 * Strategy interface for formatting Book data into a specific output format.
 *
 * Part of the Factory pattern — BookFormatterFactory returns an implementation
 * of this interface based on the requested format string.
 *
 * Adding a new format (e.g. CSV) requires only:
 *   1. A new class implementing this interface.
 *   2. One new case in BookFormatterFactory.
 * No existing code changes — Open/Closed Principle.
 */
public interface BookFormatter {

    /** Serialises a single book to the target format. */
    String formatBook(Book b) throws Exception;

    /** Serialises a list of books to the target format. */
    String formatBooks(ArrayList<Book> books) throws Exception;

    /** Returns the MIME type to set on the HTTP response. */
    String getContentType();
}